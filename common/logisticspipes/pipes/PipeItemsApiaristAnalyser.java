package logisticspipes.pipes;

import java.util.UUID;

import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.logic.TemporaryLogic;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.logisticspipes.IRoutedItem;
import logisticspipes.logisticspipes.IRoutedItem.TransportMode;
import logisticspipes.logisticspipes.SidedInventoryAdapter;
import logisticspipes.logisticspipes.TransportLayer;
import logisticspipes.modules.ModuleApiaristAnalyser;
import logisticspipes.pipes.basic.RoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.ISidedInventory;
import buildcraft.api.core.Position;
import buildcraft.core.utils.Utils;
import buildcraft.transport.TileGenericPipe;

public class PipeItemsApiaristAnalyser extends RoutedPipe implements IInventoryProvider, ISendRoutedItem {
	
	private ModuleApiaristAnalyser analyserModule;

	public PipeItemsApiaristAnalyser(int itemID) {
		super(new TemporaryLogic(), itemID);
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
				@Override public ForgeDirection itemArrived(IRoutedItem item) {return getPointedOrientation();}
				@Override public boolean stillWantItem(IRoutedItem item) {return true;}
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
	public ILogisticsModule getLogisticsModule() {
		return analyserModule;
	}

	@Override
	public UUID getSourceUUID() {
		return getRouter().getId();
	}

	@Override
	public void sendStack(ItemStack stack) {
		IRoutedItem itemToSend = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(stack, this.worldObj);
		//itemToSend.setSource(this.getRouter().getId());
		itemToSend.setTransportMode(TransportMode.Passive);
		super.queueRoutedItem(itemToSend, getPointedOrientation());
	}
	
	@Override
	public void sendStack(ItemStack stack, UUID destination) {
		IRoutedItem itemToSend = SimpleServiceLocator.buildCraftProxy.CreateRoutedItem(stack, this.worldObj);
		itemToSend.setSource(this.getRouter().getId());
		itemToSend.setDestination(destination);
		itemToSend.setTransportMode(TransportMode.Active);
		super.queueRoutedItem(itemToSend, getPointedOrientation());
	}
	
	private ForgeDirection getPointedOrientation() {
		for(ForgeDirection ori:ForgeDirection.values()) {
			Position pos = new Position(this.container);
			pos.orientation = ori;
			pos.moveForwards(1);
			TileEntity tile = this.worldObj.getBlockTileEntity((int)pos.x, (int)pos.y, (int)pos.z);
			if(tile != null) {
				if(SimpleServiceLocator.forestryProxy.isTileAnalyser(tile)) {
					return ori;
				}
			}
		}
		return null;
	}
	
	private TileEntity getPointedTileEntity() {
		for(ForgeDirection ori:ForgeDirection.values()) {
			Position pos = new Position(this.container);
			pos.orientation = ori;
			pos.moveForwards(1);
			TileEntity tile = this.worldObj.getBlockTileEntity((int)pos.x, (int)pos.y, (int)pos.z);
			if(tile != null) {
				if(SimpleServiceLocator.forestryProxy.isTileAnalyser(tile)) {
					return tile;
				}
			}
		}
		return null;
	}
	
	@Override
	public IInventory getRawInventory() {
		TileEntity tile = getPointedTileEntity();
		if (tile instanceof TileGenericPipe) return null;
		if (!(tile instanceof IInventory)) return null;
		return Utils.getInventory((IInventory) tile);
	}
	
	@Override
	public IInventory getInventory() {
		IInventory rawInventory = getRawInventory();
		if (rawInventory instanceof ISidedInventory) return new SidedInventoryAdapter((ISidedInventory) rawInventory, this.getPointedOrientation().getOpposite());
		return rawInventory;
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
	public void sendStack(ItemStack stack, UUID destination, ItemSendMode mode) {
		sendStack(stack,destination); // Ignore send mode
	}
}
