package net.minecraft.src.buildcraft.krapht.pipes;

import java.util.UUID;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.core_LogisticsPipes;
import net.minecraft.src.mod_LogisticsPipes;
import buildcraft.api.core.Orientations;
import buildcraft.api.core.Position;
import buildcraft.core.Utils;
import net.minecraft.src.buildcraft.krapht.RoutedPipe;
import net.minecraft.src.buildcraft.krapht.SimpleServiceLocator;
import net.minecraft.src.buildcraft.krapht.logic.BaseRoutingLogic;
import net.minecraft.src.buildcraft.krapht.logic.TemporaryLogic;
import net.minecraft.src.buildcraft.logisticspipes.ChassiTransportLayer;
import net.minecraft.src.buildcraft.logisticspipes.IInventoryProvider;
import net.minecraft.src.buildcraft.logisticspipes.IRoutedItem;
import net.minecraft.src.buildcraft.logisticspipes.SidedInventoryAdapter;
import net.minecraft.src.buildcraft.logisticspipes.TransportLayer;
import net.minecraft.src.buildcraft.logisticspipes.IRoutedItem.TransportMode;
import net.minecraft.src.buildcraft.logisticspipes.modules.ILogisticsModule;
import net.minecraft.src.buildcraft.logisticspipes.modules.ISendRoutedItem;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleApiaristAnalyser;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleItemSink;
import buildcraft.transport.TileGenericPipe;
import net.minecraft.src.forge.ISidedInventory;

public class PipeItemsApiaristAnalyser extends RoutedPipe implements IInventoryProvider, ISendRoutedItem {
	
	private ModuleApiaristAnalyser analyserModule;

	public PipeItemsApiaristAnalyser(int itemID) {
		super(new TemporaryLogic(), itemID);
		analyserModule = new ModuleApiaristAnalyser(this, this);
	}

	@Override
	public int getCenterTexture() {
		return mod_LogisticsPipes.LOGISTICSPIPE_APIARIST_ANALYSER_TEXTURE;
	}

	@Override
	public TransportLayer getTransportLayer() {
		if (this._transportLayer == null){
			_transportLayer = new TransportLayer() {
				@Override public Orientations itemArrived(IRoutedItem item) {return getPointedOrientation();}
				@Override public boolean stillWantItem(IRoutedItem item) {return true;}
			};
		}
		return _transportLayer;
	}
	
	@Override
	public int getNonRoutedTexture(Orientations connection) {
		if (connection.equals(getPointedOrientation())){
			return core_LogisticsPipes.LOGISTICSPIPE_CHASSI_DIRECTION_TEXTURE;
		}
		return core_LogisticsPipes.LOGISTICSPIPE_CHASSI_NOTROUTED_TEXTURE;
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
	
	private Orientations getPointedOrientation() {
		for(Orientations ori:Orientations.values()) {
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
		for(Orientations ori:Orientations.values()) {
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
		if (rawInventory instanceof ISidedInventory) return new SidedInventoryAdapter((ISidedInventory) rawInventory, this.getPointedOrientation().reverse());
		return rawInventory;
	}
	
	@Override
	public Orientations inventoryOrientation() {
		return getPointedOrientation();
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}
}
