package logisticspipes.pipes;

import java.util.LinkedList;
import java.util.List;

import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IRelayItem;
import logisticspipes.logisticspipes.ExtractionMode;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.logisticspipes.TransportLayer;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.modules.ModuleApiaristAnalyser;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.RoutedEntityItem;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.InventoryHelper;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.Pair3;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.WorldUtil;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.core.Position;
import buildcraft.transport.TileGenericPipe;

public class PipeItemsApiaristAnalyser extends CoreRoutedPipe implements IInventoryProvider, ISendRoutedItem {

	private ModuleApiaristAnalyser analyserModule;

	public PipeItemsApiaristAnalyser(int itemID) {
		super(itemID);
		analyserModule = new ModuleApiaristAnalyser();
		analyserModule.registerHandler(this, this, this, this);
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_APIARIST_ANALYSER_TEXTURE;
	}

	@Override
	public TransportLayer getTransportLayer() {
		if (this._transportLayer == null){
			_transportLayer = new TransportLayer() {
				@Override public ForgeDirection itemArrived(IRoutedItem item, ForgeDirection blocked) {
					item.setArrived(true);
					getRouter().inboundItemArrived((RoutedEntityItem) item); //NOT TESTED
					ForgeDirection pointed = getPointedOrientation();
					if(blocked != null && blocked.equals(pointed))
						return null;
					return pointed;
				}
				@Override public boolean stillWantItem(IRoutedItem item) {
					return true;
				}
			};
		}
		return _transportLayer;
	}

	@Override
	public TextureType getNonRoutedTexture(ForgeDirection connection) {
		if (connection.equals(getPointedOrientation())){
			return Textures.LOGISTICSPIPE_CHASSI_DIRECTION_TEXTURE;
		}
		return Textures.LOGISTICSPIPE_CHASSI_NOTROUTED_TEXTURE;
	}

	@Override
	public LogisticsModule getLogisticsModule() {
		return analyserModule;
	}

	@Override
	public Pair3<Integer, SinkReply, List<IFilter>> hasDestination(ItemIdentifier stack, boolean allowDefault, List<Integer> routerIDsToExclude) {
		return SimpleServiceLocator.logisticsManager.hasDestination(stack, allowDefault, getRouter().getSimpleID(), routerIDsToExclude);
	}

	@Override
	public void sendStack(ItemStack stack, Pair3<Integer, SinkReply, List<IFilter>> reply, ItemSendMode mode) {
		IRoutedItem itemToSend = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(this.container, stack);
		itemToSend.setDestination(reply.getValue1());
		if (reply.getValue2().isPassive){
			if (reply.getValue2().isDefault){
				itemToSend.setTransportMode(TransportMode.Default);
			} else {
				itemToSend.setTransportMode(TransportMode.Passive);
			}
		}
		List<IRelayItem> list = new LinkedList<IRelayItem>();
		if(reply.getValue3() != null) {
			for(IFilter filter:reply.getValue3()) {
				list.add(filter);
			}
		}
		itemToSend.addRelayPoints(list);
		super.queueRoutedItem(itemToSend, getPointedOrientation(), mode);
	}

	@Override
	public void sendStack(ItemStack stack, int destination, ItemSendMode mode, List<IRelayItem> relays) {
		IRoutedItem itemToSend = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(this.container, stack);
		itemToSend.setDestination(destination);
		itemToSend.setTransportMode(TransportMode.Active);
		itemToSend.addRelayPoints(relays);
		super.queueRoutedItem(itemToSend, getPointedOrientation(), mode);
	}

	private ForgeDirection getPointedOrientation() {
		for(ForgeDirection ori:ForgeDirection.values()) {
			Position pos = new Position(this.container);
			pos.orientation = ori;
			pos.moveForwards(1);
			TileEntity tile = this.getWorld().getBlockTileEntity((int)pos.x, (int)pos.y, (int)pos.z);
			if(tile != null) {
				if(SimpleServiceLocator.forestryProxy.isTileAnalyser(tile)) {
					return ori;
				}
			}
		}
		return null;
	}

	private TileEntity getPointedTileEntity() {
		WorldUtil wUtil = new WorldUtil(getWorld(), getX(), getY(), getZ());
		for (AdjacentTile tile : wUtil.getAdjacentTileEntities(true)){
			if(tile.tile != null) {
				if(SimpleServiceLocator.forestryProxy.isTileAnalyser(tile.tile)) {
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
	public IInventoryUtil getSneakyInventory(boolean forExtract) {
		return null; //Unused
	}

	@Override
	public IInventoryUtil getSneakyInventory(ForgeDirection _sneakyOrientation, boolean forExtract) {
		return null;
	}

	@Override
	public IInventoryUtil getUnsidedInventory() {
		IInventory inv = getRealInventory();
		if(inv == null) return null;
		return SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(inv);
	}

	@Override
	public IInventory getRealInventory() {
		TileEntity tile = getPointedTileEntity();
		if (tile == null ) return null;
		if (tile instanceof TileGenericPipe) return null;
		if (!(tile instanceof IInventory)) return null;
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
		return this.getRouterId();
	}

	@Override
	public void setTile(TileEntity tile) {
		super.setTile(tile);
		analyserModule.registerSlot(0);
	}

	@Override
	public boolean hasGenericInterests() {
		return true;
	}
	
}
