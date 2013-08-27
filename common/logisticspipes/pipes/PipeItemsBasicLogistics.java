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
import java.util.Set;
import java.util.TreeSet;

import logisticspipes.api.ILogisticsPowerProvider;
import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.modules.ModuleItemSink;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.transport.PipeTransportLogistics;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.OrientationsUtil;
import logisticspipes.utils.WorldUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class PipeItemsBasicLogistics extends CoreRoutedPipe {
	
	private ModuleItemSink itemSinkModule;

	public PipeItemsBasicLogistics(int itemID) {
		super(new PipeTransportLogistics() {

			@Override
			public boolean canPipeConnect(TileEntity tile, ForgeDirection dir) {
				if(super.canPipeConnect(tile, dir)) return true;
				if(tile instanceof ILogisticsPowerProvider) {
					ForgeDirection ori = OrientationsUtil.getOrientationOfTilewithPipe(this, tile);
					if(ori == null || ori == ForgeDirection.UNKNOWN) {
						return false;
					}
					if(tile instanceof LogisticsPowerJunctionTileEntity && (ori == ForgeDirection.DOWN || ori == ForgeDirection.UP)) {
						return false;
					}
					return true;
				}
				if(tile instanceof LogisticsSecurityTileEntity) {
					ForgeDirection ori = OrientationsUtil.getOrientationOfTilewithPipe(this, tile);
					if(ori == null || ori == ForgeDirection.UNKNOWN || ori == ForgeDirection.DOWN || ori == ForgeDirection.UP) {
						return false;
					}
					return true;
				}
				return false;
			}
		}, itemID);
		itemSinkModule = new ModuleItemSink();
		itemSinkModule.registerHandler(null, null, this, this);
	}

	@Override
	public TextureType getNonRoutedTexture(ForgeDirection connection) {
		if(isPowerProvider(connection)) {
			return Textures.LOGISTICSPIPE_POWERED_TEXTURE;
		}
		if(isSecurityProvider(connection)) {
			return Textures.LOGISTICSPIPE_SECURITY_TEXTURE;
		}
		return super.getNonRoutedTexture(connection);
	}

	@Override
	public boolean isLockedExit(ForgeDirection orientation) {
		if(isPowerJunction(orientation) || isSecurityProvider(orientation)) {
			return true;
		}
		return super.isLockedExit(orientation);
	}
	
	private boolean isPowerJunction(ForgeDirection ori) {
		TileEntity tilePipe = this.container.getTile(ori);
		if(tilePipe == null || !SimpleServiceLocator.buildCraftProxy.canPipeConnect(this.container, tilePipe, ori)) {
			return false;
		}

		if(tilePipe instanceof LogisticsPowerJunctionTileEntity) {
			return true;
		}
		return false;
	}

	private boolean isPowerProvider(ForgeDirection ori) {
		TileEntity tilePipe = this.container.getTile(ori);
		if(tilePipe == null || !SimpleServiceLocator.buildCraftProxy.canPipeConnect(this.container, tilePipe, ori)) {
			return false;
		}

		if(tilePipe instanceof ILogisticsPowerProvider) {
			return true;
		}
		return false;
	}
	
	private boolean isSecurityProvider(ForgeDirection ori) {
		TileEntity tilePipe = this.container.getTile(ori);
		if(tilePipe == null || !SimpleServiceLocator.buildCraftProxy.canPipeConnect(this.container, tilePipe, ori)) {
			return false;
		}
		if(tilePipe instanceof LogisticsSecurityTileEntity) {
			return true;
		}
		return false;
	}
	
	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_TEXTURE;
	}

	@Override
	public LogisticsModule getLogisticsModule() {
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
//TODO Must be handled manualy
				MainProxy.sendToAllPlayers(new Packet3Chat("Connected Power: " + getRoutedPowerProviders().get(0).getPowerLevel() + " LP"));
			}
			return true;
		} else {
			return super.blockActivated(world, i, j, k, entityplayer);
		}
	}
*/
	private boolean isSideOrientation(ForgeDirection ori) {
		return ori == ForgeDirection.EAST || ori == ForgeDirection.WEST || ori == ForgeDirection.SOUTH || ori == ForgeDirection.NORTH;
	}
	
	public List<ILogisticsPowerProvider> getConnectedPowerProviders() {
		List<ILogisticsPowerProvider> list = new ArrayList<ILogisticsPowerProvider>();
		WorldUtil world = new WorldUtil(this.getWorld(), this.getX(), this.getY(), this.getZ());
		LinkedList<AdjacentTile> adjacent = world.getAdjacentTileEntities(true);
		for(AdjacentTile tile:adjacent) {
			if(tile.tile instanceof ILogisticsPowerProvider) {
				if(isSideOrientation(tile.orientation) || !(tile.tile instanceof LogisticsPowerJunctionTileEntity))  {
					list.add((ILogisticsPowerProvider)tile.tile);
				}
			}
		}
		return list;
	}

	@Override
	public void setTile(TileEntity tile) {
		super.setTile(tile);
		itemSinkModule.registerSlot(0);
	}
	
	@Override
	public Set<ItemIdentifier> getSpecificInterests() {
		if(this.itemSinkModule.isDefaultRoute())
			return null;
		Set<ItemIdentifier> l1 = new TreeSet<ItemIdentifier>();
		for(int i=0; i<9;i++){
			ItemIdentifierStack item = this.itemSinkModule.getFilterInventory().getIDStackInSlot(i);
			if(item != null)
				l1.add(item.getItem());
		}
		return l1;
	}

	@Override
	public boolean hasGenericInterests() {
		return this.itemSinkModule.isDefaultRoute();
	}
}
