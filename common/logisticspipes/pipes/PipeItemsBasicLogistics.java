/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes;

import java.util.LinkedList;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Packet3Chat;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import buildcraft.api.core.Orientations;
import buildcraft.api.liquids.ITankContainer;
import buildcraft.transport.TileGenericPipe;
import logisticspipes.config.Configs;
import logisticspipes.config.Textures;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.routing.ILogisticsPowerProvider;
import logisticspipes.logic.LogicLiquidSupplier;
import logisticspipes.logic.TemporaryLogic;
import logisticspipes.main.LogisticsItem;
import logisticspipes.main.RoutedPipe;
import logisticspipes.modules.ModuleItemSink;
import logisticspipes.proxy.MainProxy;
import logisticspipes.transport.PipeTransportLogistics;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.WorldUtil;

public class PipeItemsBasicLogistics extends RoutedPipe {
	
	private ModuleItemSink itemSinkModule;

	public PipeItemsBasicLogistics(int itemID) {
		super(new PipeTransportLogistics() {

			@Override
			public boolean isPipeConnected(TileEntity tile) {
				if(super.isPipeConnected(tile)) return true;
				if(tile instanceof ILogisticsPowerProvider) return true;
				return false;
			}
		}, new TemporaryLogic(), itemID);
		itemSinkModule = new ModuleItemSink();
	}

	@Override
	public int getNonRoutedTexture(Orientations connection) {
		if(getAdjacentTilePowerProvider() != null && getAdjacentTilePowerProvider().orientation == connection) {
			return Textures.LOGISTICSPIPE_POWERED_TEXTURE;
		}
		return super.getNonRoutedTexture(connection);
	}

	@Override
	public boolean isLockedExit(Orientations orientation) {
		if(getAdjacentTilePowerProvider() != null && getAdjacentTilePowerProvider().orientation == orientation) {
			return true;
		}
		return super.isLockedExit(orientation);
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
	
	@Override
	public boolean blockActivated(World world, int i, int j, int k, EntityPlayer entityplayer) {
		if(entityplayer.getCurrentEquippedItem() != null && entityplayer.getCurrentEquippedItem().itemID == Configs.ItemHUDId + 256 && MainProxy.isServer()) {
			if(getRoutedPowerProvider() != null) {
				MainProxy.sendToAllPlayers(new Packet3Chat("Connected Power: " + getRoutedPowerProvider().getPowerLevel() + " LP"));
			}
			return true;
		} else {
			return super.blockActivated(world, i, j, k, entityplayer);
		}
	}

	private boolean isSideOrientation(Orientations ori) {
		return ori == Orientations.XPos || ori == Orientations.XNeg || ori == Orientations.ZPos || ori == Orientations.ZNeg;
	}
	
	public ILogisticsPowerProvider getConnectedPowerProvider() {
		WorldUtil world = new WorldUtil(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
		LinkedList<AdjacentTile> adjacent = world.getAdjacentTileEntities();
		for(AdjacentTile tile:adjacent) {
			if(tile.tile instanceof ILogisticsPowerProvider && isSideOrientation(tile.orientation)) {
				return (ILogisticsPowerProvider)tile.tile;
			}
		}
		return null;
	}
	
	public AdjacentTile getAdjacentTilePowerProvider() {
		WorldUtil world = new WorldUtil(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
		LinkedList<AdjacentTile> adjacent = world.getAdjacentTileEntities();
		for(AdjacentTile tile:adjacent) {
			if(tile.tile instanceof ILogisticsPowerProvider && isSideOrientation(tile.orientation)) {
				return tile;
			}
		}
		return null;
	}
}
