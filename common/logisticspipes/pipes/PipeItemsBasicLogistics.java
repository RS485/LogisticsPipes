/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import logisticspipes.config.Configs;
import logisticspipes.config.Textures;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.routing.ILogisticsPowerProvider;
import logisticspipes.logic.TemporaryLogic;
import logisticspipes.modules.ModuleItemSink;
import logisticspipes.pipes.basic.RoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.transport.PipeTransportLogistics;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.OrientationsUtil;
import logisticspipes.utils.WorldUtil;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Packet3Chat;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import buildcraft.api.core.Orientations;

public class PipeItemsBasicLogistics extends RoutedPipe {
	
	private ModuleItemSink itemSinkModule;

	public PipeItemsBasicLogistics(int itemID) {
		super(new PipeTransportLogistics() {

			@Override
			public boolean isPipeConnected(TileEntity tile) {
				if(super.isPipeConnected(tile)) return true;
				if(tile instanceof ILogisticsPowerProvider) {
					Orientations ori = OrientationsUtil.getOrientationOfTilewithPipe(this, tile);
					if(ori == null || ori == Orientations.Unknown || ori == Orientations.YNeg || ori == Orientations.YPos) {
						return false;
					}
					return true;
				}
				return false;
			}
		}, new TemporaryLogic(), itemID);
		itemSinkModule = new ModuleItemSink();
		itemSinkModule.registerHandler(null, null, null, this);
	}

	@Override
	public int getNonRoutedTexture(Orientations connection) {
		if(isPowerProvider(connection)) {
			return Textures.LOGISTICSPIPE_POWERED_TEXTURE;
		}
		return super.getNonRoutedTexture(connection);
	}

	@Override
	public boolean isLockedExit(Orientations orientation) {
		if(isPowerProvider(orientation)) {
			return true;
		}
		return super.isLockedExit(orientation);
	}
	
	private boolean isPowerProvider(Orientations ori) {
		WorldUtil world = new WorldUtil(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
		LinkedList<AdjacentTile> adjacent = world.getAdjacentTileEntities();
		for(AdjacentTile tile:adjacent) {
			if(tile.tile instanceof ILogisticsPowerProvider && ori ==tile.orientation) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int getCenterTexture() {
		return Textures.LOGISTICSPIPE_TEXTURE;
	}

	@Override
	public ILogisticsModule getLogisticsModule() {
		return itemSinkModule;
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}
/*
	@Override
	public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
		if(entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().itemID == Configs.ItemHUDId + 256 && MainProxy.isServer()) {
			if(getRoutedPowerProviders() != null && getRoutedPowerProviders().size() > 0) {
				MainProxy.sendToAllPlayers(new Packet3Chat("Connected Power: " + getRoutedPowerProviders().get(0).getPowerLevel() + " LP"));
			}
			return true;
		} else {
			return super.blockActivated(world, i, j, k, entityplayer);
		}
	}
*/
	private boolean isSideOrientation(Orientations ori) {
		return ori == Orientations.XPos || ori == Orientations.XNeg || ori == Orientations.ZPos || ori == Orientations.ZNeg;
	}
	
	public List<ILogisticsPowerProvider> getConnectedPowerProviders() {
		List<ILogisticsPowerProvider> list = new ArrayList<ILogisticsPowerProvider>();
		WorldUtil world = new WorldUtil(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
		LinkedList<AdjacentTile> adjacent = world.getAdjacentTileEntities();
		for(AdjacentTile tile:adjacent) {
			if(tile.tile instanceof ILogisticsPowerProvider && isSideOrientation(tile.orientation)) {
				list.add((ILogisticsPowerProvider)tile.tile);
			}
		}
		return list;
	}
}
