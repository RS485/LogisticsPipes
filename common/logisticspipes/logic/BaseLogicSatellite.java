/**
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.logic;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.main.GuiIDs;
import logisticspipes.main.LogisticsManager;
import logisticspipes.main.LogisticsRequest;
import logisticspipes.main.RoutedPipe;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketCoordinates;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.NBTTagCompound;
import buildcraft.core.network.TileNetworkData;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class BaseLogicSatellite extends BaseRoutingLogic implements IRequireReliableTransport {

	public static HashSet<BaseLogicSatellite> AllSatellites = new HashSet<BaseLogicSatellite>();

	protected final LinkedList<ItemIdentifier> _lostItems = new LinkedList<ItemIdentifier>();

	@TileNetworkData
	public int satelliteId;

	public BaseLogicSatellite() {
		throttleTime = 40;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		satelliteId = nbttagcompound.getInteger("satelliteid");
		ensureAllSatelliteStatus();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger("satelliteid", satelliteId);
		super.writeToNBT(nbttagcompound);
	}

	protected int findId(int increment) {
		int potentialId = satelliteId;
		boolean conflict = true;
		while (conflict) {
			potentialId += increment;
			if (potentialId < 0) {
				return 0;
			}
			conflict = false;
			for (final BaseLogicSatellite sat : AllSatellites) {
				if (sat.satelliteId == potentialId) {
					conflict = true;
					break;
				}
			}
		}
		return potentialId;
	}

	protected void ensureAllSatelliteStatus() {
		if (satelliteId == 0 && AllSatellites.contains(this)) {
			AllSatellites.remove(this);
		}
		if (satelliteId != 0 && !AllSatellites.contains(this)) {
			AllSatellites.add(this);
		}
	}

	public void setNextId(EntityPlayer player) {
		satelliteId = findId(1);
		ensureAllSatelliteStatus();
		if (MainProxy.isClient(player.worldObj)) {
			final PacketCoordinates packet = new PacketCoordinates(NetworkConstants.SATELLITE_PIPE_NEXT, xCoord, yCoord, zCoord);
			PacketDispatcher.sendPacketToServer(packet.getPacket());
		} else {
			final PacketPipeInteger packet = new PacketPipeInteger(NetworkConstants.SATELLITE_PIPE_SATELLITE_ID, xCoord, yCoord, zCoord, satelliteId);
			PacketDispatcher.sendPacketToPlayer(packet.getPacket(), (Player)player);
		}
	}

	public void setPrevId(EntityPlayer player) {
		satelliteId = findId(-1);
		ensureAllSatelliteStatus();
		if (MainProxy.isClient(player.worldObj)) {
			final PacketCoordinates packet = new PacketCoordinates(NetworkConstants.SATELLITE_PIPE_PREV, xCoord, yCoord, zCoord);
			PacketDispatcher.sendPacketToServer(packet.getPacket());
		} else {
			final PacketPipeInteger packet = new PacketPipeInteger(NetworkConstants.SATELLITE_PIPE_SATELLITE_ID, xCoord, yCoord, zCoord, satelliteId);
			PacketDispatcher.sendPacketToPlayer(packet.getPacket(),(Player) player);
		}
	}

	@Override
	public void destroy() {
		if (AllSatellites.contains(this)) {
			AllSatellites.remove(this);
		}
	}

	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
		if (MainProxy.isServer(entityplayer.worldObj)) {
			// Send the satellite id when opening gui
			final PacketPipeInteger packet = new PacketPipeInteger(NetworkConstants.SATELLITE_PIPE_SATELLITE_ID, xCoord, yCoord, zCoord, satelliteId);
			PacketDispatcher.sendPacketToPlayer(packet.getPacket(), (Player)entityplayer);
			entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_SatelitePipe_ID, worldObj, xCoord, yCoord, zCoord);

		}
	}

	@Override
	public void throttledUpdateEntity() {
		super.throttledUpdateEntity();
		if (_lostItems.isEmpty()) {
			return;
		}

		final Iterator<ItemIdentifier> iterator = _lostItems.iterator();
		while (iterator.hasNext()) {
			final LogisticsRequest request = new LogisticsRequest(iterator.next(), 1, getRoutedPipe());
			if (LogisticsManager.Request(request, ((RoutedPipe) container.pipe).getRouter().getIRoutersByCost(), null)) {
				iterator.remove();
			}
		}
	}

	@Override
	public void itemLost(ItemIdentifier item) {
		_lostItems.add(item);
	}

	@Override
	public void itemArrived(ItemIdentifier item) {
	}

	public void setSatelliteId(int integer) {
		satelliteId = integer;
	}
}
