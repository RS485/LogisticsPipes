/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package net.minecraft.src.buildcraft.krapht.pipes;

import java.util.HashMap;
import java.util.UUID;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.core_LogisticsPipes;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.krapht.IProvideItems;
import net.minecraft.src.buildcraft.krapht.IRequestItems;
import net.minecraft.src.buildcraft.krapht.LogisticsPromise;
import net.minecraft.src.buildcraft.krapht.LogisticsTransaction;
import net.minecraft.src.buildcraft.krapht.RoutedPipe;
import net.minecraft.src.buildcraft.krapht.SimpleServiceLocator;
import net.minecraft.src.buildcraft.krapht.logic.TemporaryLogic;
import net.minecraft.src.buildcraft.krapht.routing.IRouter;
import net.minecraft.src.buildcraft.logisticspipes.ChassiModule;
import net.minecraft.src.buildcraft.logisticspipes.ChassiTransportLayer;
import net.minecraft.src.buildcraft.logisticspipes.IInventoryProvider;
import net.minecraft.src.buildcraft.logisticspipes.IRoutedItem;
import net.minecraft.src.buildcraft.logisticspipes.ItemModule;
import net.minecraft.src.buildcraft.logisticspipes.SidedInventoryAdapter;
import net.minecraft.src.buildcraft.logisticspipes.TransportLayer;
import net.minecraft.src.buildcraft.logisticspipes.IRoutedItem.TransportMode;
import net.minecraft.src.buildcraft.logisticspipes.modules.ILegacyActiveModule;
import net.minecraft.src.buildcraft.logisticspipes.modules.ILogisticsModule;
import net.minecraft.src.buildcraft.logisticspipes.modules.ISendRoutedItem;
import net.minecraft.src.buildcraft.transport.TileGenericPipe;
import net.minecraft.src.forge.ISidedInventory;
import net.minecraft.src.krapht.ISimpleInventoryEventHandler;
import net.minecraft.src.krapht.ItemIdentifier;
import net.minecraft.src.krapht.SimpleInventory;


public abstract class PipeLogisticsChassi extends RoutedPipe implements ISimpleInventoryEventHandler, IInventoryProvider, ISendRoutedItem, IProvideItems{

	private final ChassiModule _module;
	private final SimpleInventory _moduleInventory;
	private boolean switchOrientationOnTick = false;

	
	public PipeLogisticsChassi(int itemID) {
		super(new TemporaryLogic(), itemID);
		_moduleInventory = new SimpleInventory(getChassiSize(), "Chassi pipe", 1);
		_moduleInventory.addListener(this);
		_module = new ChassiModule(getChassiSize(), this);
	}
	
	public Orientations getPointedOrientation(){
		int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		if (meta >= Orientations.values().length) return null;
		return Orientations.values()[meta];
	}
	
	public TileEntity getPointedTileEntity(){
		int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		if (meta >= Orientations.values().length) return null;
		
		Position pos = new Position(xCoord, yCoord, zCoord, Orientations.values()[meta]);
		pos.moveForwards(1.0);
		return worldObj.getBlockTileEntity((int)pos.x, (int)pos.y, (int)pos.z);
	}
	
	public void nextOrientation() {
		int metadata = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
		
		int nextMetadata = metadata;
		
		for (int l = 0; l < 6; ++l) {
			nextMetadata++;
			
			if (nextMetadata > 5) {
				nextMetadata = 0;
			}
			if (!isValidOrientation(Orientations.values()[nextMetadata])) continue;
			worldObj.setBlockMetadata(xCoord, yCoord, zCoord, nextMetadata);
			worldObj.markBlockAsNeedsUpdate(xCoord, yCoord, zCoord);
			//System.out.println("orientation:" + nextMetadata);
			return;
		}
	}
	
	private boolean isValidOrientation(Orientations connection){
		if (getRouter().isRoutedExit(connection)) return false;
		Position pos = new Position(xCoord, yCoord, zCoord, connection);
		pos.moveForwards(1.0);
		TileEntity tile = worldObj.getBlockTileEntity((int)pos.x, (int)pos.y, (int)pos.z);

		if (tile == null) return false;
		return SimpleServiceLocator.buildCraftProxy.checkPipesConnections(this.container, tile);
	}
	
	public IInventory getModuleInventory(){
		return this._moduleInventory;
	}
	
	@Override
	public int getCenterTexture() {
		return core_LogisticsPipes.LOGISTICSPIPE_TEXTURE;
	}
	
	@Override
	public int getRoutedTexture(Orientations connection) {
		return core_LogisticsPipes.LOGISTICSPIPE_CHASSI_ROUTED_TEXTURE;
	}
	
	@Override
	public int getNonRoutedTexture(Orientations connection) {
		if (connection == Orientations.values()[worldObj.getBlockMetadata(xCoord, yCoord, zCoord)]){
			return core_LogisticsPipes.LOGISTICSPIPE_CHASSI_DIRECTION_TEXTURE;
		}
		return core_LogisticsPipes.LOGISTICSPIPE_CHASSI_NOTROUTED_TEXTURE;
	}
	
	@Override
	public void onNeighborBlockChange_Logistics() {
		if (!isValidOrientation(Orientations.values()[worldObj.getBlockMetadata(xCoord, yCoord, zCoord)])){
			nextOrientation();
		}
	};
	
	@Override
	public void onBlockPlaced() {
		super.onBlockPlaced();
		switchOrientationOnTick = true;
	}
	
	
	/*** IInventoryProvider ***/
	
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
	
	/*** ISendRoutedItem ***/
	
	public java.util.UUID getSourceUUID() {
		return this.getRouter().getId();
	};
	
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
	
	
	public void readFromNBT(net.minecraft.src.NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		_moduleInventory.readFromNBT(nbttagcompound, "chassi");
		InventoryChanged(_moduleInventory);
		_module.readFromNBT(nbttagcompound, "");
	};
	
	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		_moduleInventory.writeToNBT(nbttagcompound, "chassi");
		_module.writeToNBT(nbttagcompound, "");
	}

	@Override
	public void destroy() {
		super.destroy();
		_moduleInventory.removeListener(this);
		_moduleInventory.dropContents(this.worldObj, this.xCoord, this.yCoord, this.zCoord);
	}

	@Override
	public void InventoryChanged(SimpleInventory inventory) {
		boolean reInitGui = false;
		for (int i = 0; i < inventory.getSizeInventory(); i++){
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack == null){
				if (_module.hasModule(i)){
					_module.removeModule(i);
					reInitGui = true;
				}
				continue;
			}
			
			if (stack.getItem() instanceof ItemModule){
				ILogisticsModule current = _module.getModule(i);
				ILogisticsModule next = ((ItemModule)stack.getItem()).getModuleForItem(stack, _module.getModule(i), this, this);
				if (current != next){
					_module.installModule(i, next);
					
				}
			}
		}
		if (reInitGui){
			//TODO SendCLient gui Open
			/*if (ModLoader.getMinecraftInstance().currentScreen instanceof GuiChassiPipe){
				ModLoader.getMinecraftInstance().currentScreen.initGui();
			}*/
		}
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (switchOrientationOnTick){
			switchOrientationOnTick = false;
			nextOrientation();
		}
	}
	
	public abstract int getChassiSize();
	
	@Override
	public final ILogisticsModule getLogisticsModule() {
		return _module;
	}
	
	@Override
	public TransportLayer getTransportLayer() {
		if (this._transportLayer == null){
			_transportLayer = new ChassiTransportLayer(this);
		}
		return _transportLayer;
	}
	
	@Override
	public boolean blockActivated(World world, int i, int j, int k,	EntityPlayer entityplayer) {
		if (entityplayer.getCurrentEquippedItem() == null) return super.blockActivated(world, i, j, k, entityplayer);
		
		if (entityplayer.getCurrentEquippedItem().getItem() == net.minecraft.src.BuildCraftCore.wrenchItem){
			if (entityplayer.isSneaking()){
				((PipeLogisticsChassi)this.container.pipe).nextOrientation();
				return true;
			}
		}
		return super.blockActivated(world, i, j, k, entityplayer);
	}
	
	/*** IProvideItems ***/
	@Override
	public void canProvide(LogisticsTransaction transaction) {
		
		if (!isEnabled()){
			return;
		}
		
		for (int i = 0; i < this.getChassiSize(); i++){
			ILogisticsModule x = _module.getSubModule(i);
			if (x instanceof ILegacyActiveModule){
				((ILegacyActiveModule)x).canProvide(transaction);
			}
		}
	}
	
	@Override
	public void fullFill(LogisticsPromise promise, IRequestItems destination) {
		if (!isEnabled()){
			return;
		}
		for (int i = 0; i < this.getChassiSize(); i++){
			ILogisticsModule x = _module.getSubModule(i);
			if (x instanceof ILegacyActiveModule){
				((ILegacyActiveModule)x).fullFill(promise, destination);
			}
		}
	}
	
	@Override
	public int getAvailableItemCount(ItemIdentifier item) {
		if (!isEnabled()){
			return 0;
		}
		
		for (int i = 0; i < this.getChassiSize(); i++){
			ILogisticsModule x = _module.getSubModule(i);
			if (x instanceof ILegacyActiveModule){
				return ((ILegacyActiveModule)x).getAvailableItemCount(item);
			}
		}
		return 0;
	}
	
	@Override
	public HashMap<ItemIdentifier, Integer> getAllItems() {
		if (!isEnabled()){
			return new HashMap<ItemIdentifier, Integer>();
		}
		for (int i = 0; i < this.getChassiSize(); i++){
			ILogisticsModule x = _module.getSubModule(i);
			if (x instanceof ILegacyActiveModule){
				return ((ILegacyActiveModule)x).getAllItems();
			}
		}
		return new HashMap<ItemIdentifier, Integer>();
	}
	
	@Override
	public IRouter getRouter() {
		return super.getRouter();
	}
}
