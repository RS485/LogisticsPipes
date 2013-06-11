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
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.satpipe.SatPipeNext;
import logisticspipes.network.packets.satpipe.SatPipePrev;
import logisticspipes.network.packets.satpipe.SatPipeSetID;
import logisticspipes.pipes.PipeItemsSatelliteLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.request.RequestTree;
import logisticspipes.utils.ItemIdentifierStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import buildcraft.core.network.TileNetworkData;
import cpw.mods.fml.common.network.Player;

public class BaseLogicSatellite extends BaseRoutingLogic implements IRequireReliableTransport {

	public static HashSet<BaseLogicSatellite> AllSatellites = new HashSet<BaseLogicSatellite>();

	// called only on server shutdown
	public static void cleanup() {
		AllSatellites.clear();
	}
	protected final LinkedList<ItemIdentifierStack> _lostItems = new LinkedList<ItemIdentifierStack>();

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
		if(MainProxy.isClient(this.worldObj)) return satelliteId;
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
		if(MainProxy.isClient()) return;
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
			final ModernPacket packet = PacketHandler.getPacket(SatPipeNext.class).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord);
			MainProxy.sendPacketToServer(packet.getPacket());
		} else {
			final ModernPacket packet = PacketHandler.getPacket(SatPipeSetID.class).setSatID(satelliteId).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord);
			MainProxy.sendPacketToPlayer(packet.getPacket(), (Player)player);
		}
		updateWatchers();
	}

	public void setPrevId(EntityPlayer player) {
		satelliteId = findId(-1);
		ensureAllSatelliteStatus();
		if (MainProxy.isClient(player.worldObj)) {
			final ModernPacket packet = PacketHandler.getPacket(SatPipePrev.class).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord);
			MainProxy.sendPacketToServer(packet.getPacket());
		} else {
			final ModernPacket packet = PacketHandler.getPacket(SatPipeSetID.class).setSatID(satelliteId).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord);
			MainProxy.sendPacketToPlayer(packet.getPacket(),(Player) player);
		}
		updateWatchers();
	}

	private void updateWatchers() {
		for(EntityPlayer player : ((PipeItemsSatelliteLogistics)this.container.pipe).localModeWatchers) {
			final ModernPacket packet = PacketHandler.getPacket(SatPipeSetID.class).setSatID(satelliteId).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord);
			MainProxy.sendPacketToPlayer(packet.getPacket(),(Player) player);
		}
	}

	@Override
	public void destroy() {
		if(MainProxy.isClient(this.worldObj)) return;
		if (AllSatellites.contains(this)) {
			AllSatellites.remove(this);
		}
	}

	@Override
	public void onWrenchClicked(EntityPlayer entityplayer) {
		if (MainProxy.isServer(entityplayer.worldObj)) {
			// Send the satellite id when opening gui
			final ModernPacket packet = PacketHandler.getPacket(SatPipeSetID.class).setSatID(satelliteId).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord);
			MainProxy.sendPacketToPlayer(packet.getPacket(), (Player)entityplayer);
			entityplayer.openGui(LogisticsPipes.instance, GuiIDs.GUI_SatelitePipe_ID, worldObj, xCoord, yCoord, zCoord);

		}
	}

	@Override
	public void throttledUpdateEntity() {
		super.throttledUpdateEntity();
		if (_lostItems.isEmpty()) {
			return;
		}
		final Iterator<ItemIdentifierStack> iterator = _lostItems.iterator();
		while (iterator.hasNext()) {
			ItemIdentifierStack stack = iterator.next();
			int received = RequestTree.requestPartial(stack, (CoreRoutedPipe) container.pipe);
			if(received > 0) {
				if(received == stack.stackSize) {
					iterator.remove();
				} else {
					stack.stackSize -= received;
				}
			}
		}
	}

	@Override
	public void itemLost(ItemIdentifierStack item) {
		_lostItems.add(item);
	}

	@Override
	public void itemArrived(ItemIdentifierStack item) {
	}

	public void setSatelliteId(int integer) {
		satelliteId = integer;
	}
}
