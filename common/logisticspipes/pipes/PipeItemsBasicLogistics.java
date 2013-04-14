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

import buildcraft.api.core.IIconProvider;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import logisticspipes.LogisticsPipes;
import logisticspipes.api.ILogisticsPowerProvider;
import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.logic.TemporaryLogic;
import logisticspipes.modules.ModuleItemSink;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.textures.provider.DummyProvider;
import logisticspipes.transport.PipeTransportLogistics;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.OrientationsUtil;
import logisticspipes.utils.WorldUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class PipeItemsBasicLogistics extends CoreRoutedPipe {
	
	private ModuleItemSink itemSinkModule;

	public PipeItemsBasicLogistics(int itemID) {
		super(new PipeTransportLogistics() {

			@Override
			public boolean isPipeConnected(TileEntity tile, ForgeDirection dir) {
				if(super.isPipeConnected(tile, dir)) return true;
				if(tile instanceof ILogisticsPowerProvider) {
					ForgeDirection ori = OrientationsUtil.getOrientationOfTilewithPipe(this, tile);
					if(ori == null || ori == ForgeDirection.UNKNOWN || ori == ForgeDirection.DOWN || ori == ForgeDirection.UP) {
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
		}, new TemporaryLogic(), itemID);
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
		if(isPowerProvider(orientation) || isSecurityProvider(orientation)) {
			return true;
		}
		return super.isLockedExit(orientation);
	}
	
	private boolean isPowerProvider(ForgeDirection ori) {
		TileEntity tilePipe = this.container.tileBuffer[ori.ordinal()].getTile();
		if(tilePipe == null || !this.container.isPipeConnected(tilePipe, ori)) {
			return false;
		}

		if(tilePipe instanceof ILogisticsPowerProvider) {
			return true;
		}
		return false;
	}
	
	private boolean isSecurityProvider(ForgeDirection ori) {
		TileEntity tilePipe = this.container.tileBuffer[ori.ordinal()].getTile();
		if(tilePipe == null || !this.container.isPipeConnected(tilePipe, ori)) {
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
	private boolean isSideOrientation(ForgeDirection ori) {
		return ori == ForgeDirection.EAST || ori == ForgeDirection.WEST || ori == ForgeDirection.SOUTH || ori == ForgeDirection.NORTH;
	}
	
	public List<ILogisticsPowerProvider> getConnectedPowerProviders() {
		List<ILogisticsPowerProvider> list = new ArrayList<ILogisticsPowerProvider>();
		WorldUtil world = new WorldUtil(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
		LinkedList<AdjacentTile> adjacent = world.getAdjacentTileEntities(true);
		for(AdjacentTile tile:adjacent) {
			if(tile.tile instanceof ILogisticsPowerProvider && isSideOrientation(tile.orientation)) {
				list.add((ILogisticsPowerProvider)tile.tile);
			}
		}
		return list;
	}

	@Override
	public void setTile(TileEntity tile) {
		super.setTile(tile);
		itemSinkModule.registerPosition(xCoord, yCoord, zCoord, 0);
	}
	
	@Override
	public Set<ItemIdentifier> getSpecificInterests() {
		if(this.itemSinkModule.isDefaultRoute())
			return null;
		Set<ItemIdentifier> l1 = new TreeSet<ItemIdentifier>();
		for(int i=0; i<9;i++){
			ItemStack item = this.itemSinkModule.getFilterInventory().getStackInSlot(i);
			if(item != null)
				l1.add(ItemIdentifier.get(item));
		}
		return l1;
	}

	@Override
	public boolean hasGenericInterests() {
		return this.itemSinkModule.isDefaultRoute();
	}


	@Override
	public int getIconIndex(ForgeDirection direction) {
		// TODO Auto-generated method stub
		return 0;
	}
}
