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
import logisticspipes.utils.InventoryHelper;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.tuples.Triplet;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;

import net.minecraft.util.EnumFacing;

import net.minecraftforge.items.CapabilityItemHandler;

import network.rs485.logisticspipes.world.WorldCoordinatesWrapper;

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
				public EnumFacing itemArrived(IRoutedItem item, EnumFacing blocked) {
					EnumFacing pointed = getPointedOrientation();
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
	public TextureType getNonRoutedTexture(EnumFacing connection) {
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
	public EnumFacing getPointedOrientation() {
		//@formatter:off
		return new WorldCoordinatesWrapper(container).getConnectedAdjacentTileEntities()
				.filter(adjacent -> isTileAnalyser(adjacent.tileEntity))
				.map(adjacent -> adjacent.direction)
				.findFirst().orElse(null);
		//@formatter:on
	}

	public TileEntity getPointedTileEntity() {
		//@formatter:off
		return new WorldCoordinatesWrapper(container).getConnectedAdjacentTileEntities()
				.map(adjacent -> adjacent.tileEntity)
				.filter(this::isTileAnalyser)
				.findFirst().orElse(null);
		//@formatter:on
	}

	@Override
	public IInventoryUtil getPointedInventory() {
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
	public IInventoryUtil getSneakyInventory(EnumFacing _sneakyOrientation) {
		return null;
	}

	@Override
	public IInventoryUtil getUnsidedInventory() {
		TileEntity inv = getRealInventory();
		if (inv == null) {
			return null;
		}
		return SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(inv, getPointedOrientation().getOpposite());
	}

	@Override
	public TileEntity getRealInventory() {
		TileEntity tile = getPointedTileEntity();
		if (tile == null) {
			return null;
		}
		if (SimpleServiceLocator.pipeInformationManager.isItemPipe(tile)) {
			return null;
		}
		if (!(tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getPointedOrientation().getOpposite()))) {
			return null;
		}
		return tile;
	}

	@Override
	public EnumFacing inventoryOrientation() {
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

	private boolean isTileAnalyser(TileEntity tile) {
		return SimpleServiceLocator.forestryProxy.isTileAnalyser(tile);
	}
}
