package logisticspipes.pipes;

import java.util.List;

import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.logisticspipes.ExtractionMode;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.TransportLayer;
import logisticspipes.modules.ModuleApiaristAnalyser;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.modules.abstractmodules.LogisticsModule.ModulePositionType;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.InventoryHelper;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.WorldUtil;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.tuples.LPPosition;
import logisticspipes.utils.tuples.Triplet;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

public class PipeItemsApiaristAnalyser extends CoreRoutedPipe implements ISendRoutedItem {

	private ModuleApiaristAnalyser analyserModule;

	public PipeItemsApiaristAnalyser(Item item) {
		super(item);
		analyserModule = new ModuleApiaristAnalyser();
		analyserModule.registerHandler(this, this);
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_APIARIST_ANALYSER_TEXTURE;
	}

	@Override
	public TransportLayer getTransportLayer() {
		if (_transportLayer == null) {
			_transportLayer = new TransportLayer() {

				@Override
				public ForgeDirection itemArrived(IRoutedItem item, ForgeDirection blocked) {
					ForgeDirection pointed = getPointedOrientation();
					if (blocked != null && blocked.equals(pointed)) {
						return null;
					}
					return pointed;
				}

				@Override
				public boolean stillWantItem(IRoutedItem item) {
					return true;
				}
			};
		}
		return _transportLayer;
	}

	@Override
	public TextureType getNonRoutedTexture(ForgeDirection connection) {
		if (connection.equals(getPointedOrientation())) {
			return Textures.LOGISTICSPIPE_CHASSI_DIRECTION_TEXTURE;
		}
		return Textures.LOGISTICSPIPE_CHASSI_NOTROUTED_TEXTURE;
	}

	@Override
	public LogisticsModule getLogisticsModule() {
		return analyserModule;
	}

	@Override
	public Triplet<Integer, SinkReply, List<IFilter>> hasDestination(ItemIdentifier stack, boolean allowDefault, List<Integer> routerIDsToExclude) {
		return SimpleServiceLocator.logisticsManager.hasDestination(stack, allowDefault, getRouter().getSimpleID(), routerIDsToExclude);
	}

	@Override
	public ForgeDirection getPointedOrientation() {
		for (ForgeDirection ori : ForgeDirection.values()) {
			LPPosition pos = new LPPosition((TileEntity) container);
			pos.moveForward(ori);
			TileEntity tile = pos.getTileEntity(getWorld());
			if (tile != null) {
				if (SimpleServiceLocator.forestryProxy.isTileAnalyser(tile) || SimpleServiceLocator.binnieProxy.isTileAnalyser(tile)) {
					return ori;
				}
			}
		}
		return null;
	}

	public TileEntity getPointedTileEntity() {
		WorldUtil wUtil = new WorldUtil(getWorld(), getX(), getY(), getZ());
		for (AdjacentTile tile : wUtil.getAdjacentTileEntities(true)) {
			if (tile.tile != null) {
				if (SimpleServiceLocator.forestryProxy.isTileAnalyser(tile.tile) || SimpleServiceLocator.binnieProxy.isTileAnalyser(tile.tile)) {
					return tile.tile;
				}
			}
		}
		return null;
	}

	@Override
	public IInventoryUtil getPointedInventory(boolean forExtract) {
		return null; //Unused
	}

	@Override
	public IInventoryUtil getPointedInventory(ExtractionMode mode, boolean forExtract) {
		return null; //Unused
	}

	@Override
	public IInventoryUtil getSneakyInventory(boolean forExtract, ModulePositionType slot, int positionInt) {
		return null; //Unused
	}

	@Override
	public IInventoryUtil getSneakyInventory(ForgeDirection _sneakyOrientation, boolean forExtract) {
		return null;
	}

	@Override
	public IInventoryUtil getUnsidedInventory() {
		IInventory inv = getRealInventory();
		if (inv == null) {
			return null;
		}
		return SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(inv);
	}

	@Override
	public IInventory getRealInventory() {
		TileEntity tile = getPointedTileEntity();
		if (tile == null) {
			return null;
		}
		if (SimpleServiceLocator.pipeInformationManager.isItemPipe(tile)) {
			return null;
		}
		if (!(tile instanceof IInventory)) {
			return null;
		}
		return InventoryHelper.getInventory((IInventory) tile);
	}

	@Override
	public ForgeDirection inventoryOrientation() {
		return getPointedOrientation();
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}

	@Override
	public int getSourceID() {
		return getRouterId();
	}

	@Override
	public void setTile(TileEntity tile) {
		super.setTile(tile);
		analyserModule.registerPosition(ModulePositionType.IN_PIPE, 0);
	}

	@Override
	public boolean hasGenericInterests() {
		return true;
	}

}
