/**
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.logic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.routing.IRequestLiquid;
import logisticspipes.interfaces.routing.IRequireReliableLiquidTransport;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketCoordinates;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.pipes.PipeLiquidSatelliteLogistics;
import logisticspipes.proxy.MainProxy;
import logisticspipes.request.RequestTree;
import logisticspipes.utils.LiquidIdentifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import buildcraft.core.network.TileNetworkData;
import cpw.mods.fml.common.network.Player;

public class BaseLogicLiquidSatellite extends BaseRoutingLogic implements IRequireReliableLiquidTransport {

	public static HashSet<BaseLogicLiquidSatellite> AllSatellites = new HashSet<BaseLogicLiquidSatellite>();

	// called only on server shutdown
	public static void cleanup() {
		AllSatellites.clear();
	}
	
	protected final Map<LiquidIdentifier, Integer> _lostItems = new HashMap<LiquidIdentifier, Integer>();

	@TileNetworkData
	public int satelliteId;

	public BaseLogicLiquidSatellite() {
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
			for (final BaseLogicLiquidSatellite sat : AllSatellites) {
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
			final PacketCoordinates packet = new PacketCoordinates(NetworkConstants.SATELLITE_PIPE_NEXT, xCoord, yCoord, zCoord);
			MainProxy.sendPacketToServer(packet.getPacket());
		} else {
			final PacketPipeInteger packet = new PacketPipeInteger(NetworkConstants.SATELLITE_PIPE_SATELLITE_ID, xCoord, yCoord, zCoord, satelliteId);
			MainProxy.sendPacketToPlayer(packet.getPacket(), (Player)player);
		}
		updateWatchers();
	}

	public void setPrevId(EntityPlayer player) {
		satelliteId = findId(-1);
		ensureAllSatelliteStatus();
		if (MainProxy.isClient(player.worldObj)) {
			final PacketCoordinates packet = new PacketCoordinates(NetworkConstants.SATELLITE_PIPE_PREV, xCoord, yCoord, zCoord);
			MainProxy.sendPacketToServer(packet.getPacket());
		} else {
			final PacketPipeInteger packet = new PacketPipeInteger(NetworkConstants.SATELLITE_PIPE_SATELLITE_ID, xCoord, yCoord, zCoord, satelliteId);
			MainProxy.sendPacketToPlayer(packet.getPacket(),(Player) player);
		}
		updateWatchers();
	}

	
	private void updateWatchers() {
		for(EntityPlayer player : ((PipeLiquidSatelliteLogistics)this.container.pipe).localModeWatchers) {
			final PacketPipeInteger packet = new PacketPipeInteger(NetworkConstants.SATELLITE_PIPE_SATELLITE_ID, xCoord, yCoord, zCoord, satelliteId);
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
			final PacketPipeInteger packet = new PacketPipeInteger(NetworkConstants.SATELLITE_PIPE_SATELLITE_ID, xCoord, yCoord, zCoord, satelliteId);
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
		final Iterator<Entry<LiquidIdentifier, Integer>> iterator = _lostItems.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<LiquidIdentifier, Integer> stack = iterator.next();
			int received = RequestTree.requestLiquidPartial(stack.getKey(), stack.getValue(), (IRequestLiquid) this.getRoutedPipe(), null);
			
			if(received > 0) {
				if(received == stack.getValue()) {
					iterator.remove();
				} else {
					stack.setValue(stack.getValue() - received);
				}
			}
		}
	}

	public void setSatelliteId(int integer) {
		satelliteId = integer;
	}

	@Override
	public void itemLost(LiquidIdentifier item, int amount) {
		if(_lostItems.containsKey(item)) {
			_lostItems.put(item, _lostItems.get(item) + amount);
		} else {
			_lostItems.put(item, amount);
		}
	}

	@Override
	public void itemArrived(LiquidIdentifier item, int amount) {}
}
