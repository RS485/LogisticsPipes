/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.pipes;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import logisticspipes.gui.hud.HUDCrafting;
import logisticspipes.interfaces.IChangeListener;
import logisticspipes.interfaces.IHeadUpDisplayRenderer;
import logisticspipes.interfaces.IHeadUpDisplayRendererProvider;
import logisticspipes.interfaces.IOrderManagerContentReceiver;
import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.ICraftItems;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.interfaces.routing.IRequestItems;
import logisticspipes.interfaces.routing.IRequireReliableTransport;
import logisticspipes.modules.ModuleCrafter;
import logisticspipes.modules.abstractmodules.LogisticsModule.ModulePositionType;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.hud.HUDStartWatchingPacket;
import logisticspipes.network.packets.hud.HUDStopWatchingPacket;
import logisticspipes.network.packets.module.RequestCraftingPipeUpdatePacket;
import logisticspipes.network.packets.orderer.OrdererManagerContent;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.signs.CraftingPipeSign;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.cc.interfaces.CCCommand;
import logisticspipes.proxy.cc.interfaces.CCQueued;
import logisticspipes.proxy.cc.interfaces.CCType;
import logisticspipes.request.CraftingTemplate;
import logisticspipes.request.RequestTreeNode;
import logisticspipes.routing.LogisticsPromise;
import logisticspipes.routing.order.IOrderInfoProvider.RequestType;
import logisticspipes.routing.order.LogisticsOrder;
import logisticspipes.routing.order.LogisticsOrderManager;
import logisticspipes.textures.Textures;
import logisticspipes.textures.Textures.TextureType;
import logisticspipes.utils.IHavePriority;
import logisticspipes.utils.PlayerCollectionList;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.Player;

@CCType(name = "LogisticsPipes:Crafting")
public class PipeItemsCraftingLogistics extends CoreRoutedPipe implements ICraftItems, IRequireReliableTransport, IHeadUpDisplayRendererProvider, IChangeListener, IOrderManagerContentReceiver, IHavePriority {

	protected ModuleCrafter craftingModule;
	
	public final LinkedList<ItemIdentifierStack> oldList = new LinkedList<ItemIdentifierStack>();
	public final LinkedList<ItemIdentifierStack> displayList = new LinkedList<ItemIdentifierStack>();
	public final PlayerCollectionList localModeWatchers = new PlayerCollectionList();
	private final HUDCrafting HUD = new HUDCrafting(this);
	
	private boolean init = false;
	private boolean doContentUpdate = true;
	
	public PipeItemsCraftingLogistics(int itemID) {
		super(itemID);
		// module still relies on this for some code
		craftingModule = new ModuleCrafter(this);
		craftingModule.registerPosition(ModulePositionType.IN_PIPE, 0);
		throttleTime = 40;
		_orderManager = new LogisticsOrderManager(this); // null by default when not needed
	}

	@Override
	public void onNeighborBlockChange(int blockId) {
		craftingModule.clearCache();
		super.onNeighborBlockChange(blockId);
	}
	
	@Override
	public void onAllowedRemoval() {
		while(_orderManager.hasOrders(RequestType.CRAFTING)) {
			_orderManager.sendFailed();
		}
	}

	public void enableUpdateRequest() {
		init = false;
	}
	
	@Override
	public void ignoreDisableUpdateEntity() {
		if(!init) {
			if(MainProxy.isClient(getWorld())) {
				if(FMLClientHandler.instance().getClient() != null && FMLClientHandler.instance().getClient().thePlayer != null && FMLClientHandler.instance().getClient().thePlayer.sendQueue != null){
					MainProxy.sendPacketToServer(PacketHandler.getPacket(RequestCraftingPipeUpdatePacket.class).setModulePos(craftingModule));
				}
			}
			init = true;
		}
	}

	@Override
	public void enabledUpdateEntity() {
		super.enabledUpdateEntity();
		if (doContentUpdate) {
			checkContentUpdate();
		}
		//craftingModule.enabledUpdateEntity();
	}

	@Override
	public TextureType getCenterTexture() {
		return Textures.LOGISTICSPIPE_CRAFTER_TEXTURE;
	}

	
	@Override
	public void canProvide(RequestTreeNode tree, int donePromisses, List<IFilter> filters) {
		
		if (!isEnabled()){
			return;
		}
		craftingModule.canProvide(tree, donePromisses, filters);

	}

	@Override
	public CraftingTemplate addCrafting(ItemIdentifier toCraft) {
		
		if (!isEnabled()){
			return null;
		}		
		
		return craftingModule.addCrafting(toCraft);
	}

	@Override
	public LogisticsOrder fullFill(LogisticsPromise promise, IRequestItems destination, IAdditionalTargetInformation info) {
		return craftingModule.fullFill(promise, destination, info);
	}

	@Override
	public void registerExtras(LogisticsPromise promise) {		
		craftingModule.registerExtras(promise);
	}

	
	
	@Override
	public void getAllItems(Map<ItemIdentifier, Integer> list,List<IFilter> filters) {
		craftingModule.getAllItems(list, filters);
	}

	@Override
	public boolean canCraft(ItemIdentifier toCraft) {
		return craftingModule.canCraft(toCraft);
	}
	@Override
	public List<ItemIdentifierStack> getCraftedItems() {
		return craftingModule.getCraftedItems();
	}
	@Override
	public ModuleCrafter getLogisticsModule() {
		return craftingModule;
	}

	@Override
	public ItemSendMode getItemSendMode() {
		return ItemSendMode.Normal;
	}
	
	@Override
	public int getTodo() {
		return _orderManager.totalItemsCountInAllOrders();
	}

	@Override
	public void startWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStartWatchingPacket.class).setInteger(1).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	@Override
	public void stopWatching() {
		MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDStopWatchingPacket.class).setInteger(1).setPosX(getX()).setPosY(getY()).setPosZ(getZ()));
	}

	@Override
	public void playerStartWatching(EntityPlayer player, int mode) {
		if(mode == 1) {
			localModeWatchers.add(player);
			MainProxy.sendPacketToPlayer(PacketHandler.getPacket(OrdererManagerContent.class).setIdentList(oldList).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), (Player)player);
			this.craftingModule.startWatching(player);
		} else {
			super.playerStartWatching(player, mode);
		}
	}

	@Override
	public void playerStopWatching(EntityPlayer player, int mode) {
		super.playerStopWatching(player, mode);
		localModeWatchers.remove(player);
		this.craftingModule.stopWatching(player);
	}

	@Override
	public void listenedChanged() {
		doContentUpdate = true;
	}

	private void checkContentUpdate() {
		doContentUpdate = false;
		LinkedList<ItemIdentifierStack> all = _orderManager.getContentList(this.getWorld());
		if(!oldList.equals(all)) {
			oldList.clear();
			oldList.addAll(all);
			MainProxy.sendToPlayerList(PacketHandler.getPacket(OrdererManagerContent.class).setIdentList(all).setPosX(getX()).setPosY(getY()).setPosZ(getZ()), localModeWatchers);
		}
	}

	@Override
	public void setOrderManagerContent(Collection<ItemIdentifierStack> list) {
		displayList.clear();
		displayList.addAll(list);
	}

	@Override
	public IHeadUpDisplayRenderer getRenderer() {
		return HUD;
	}
	
	@Override
	public double getLoadFactor() {
		return (_orderManager.totalItemsCountInAllOrders()+63.0)/64.0;
	}
	
	/* ComputerCraftCommands */
	@CCCommand(description="Imports the crafting recipe from the connected machine/crafter")
	@CCQueued(prefunction="testImportAccess")
	public void reimport() {
		craftingModule.importFromCraftingTable(null);
	}

	@Override
	public Set<ItemIdentifier> getSpecificInterests() {
		return craftingModule.getSpecificInterests();
	}

	@Override
	public int getPriority() {
		return craftingModule.getPriority();
	}

	public List<ForgeDirection> getCraftingSigns() {
		return craftingModule.getCraftingSigns();
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		craftingModule.readFromNBT(nbttagcompound);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		craftingModule.writeToNBT(nbttagcompound);
	}

	@Override
	public void throttledUpdateEntity() {
		super.throttledUpdateEntity();
		craftingModule.tick();
	}

	@Override
	public void itemArrived(ItemIdentifierStack item, IAdditionalTargetInformation info) {
		craftingModule.itemArrived(item, info);
	}

	@Override
	public void itemLost(ItemIdentifierStack item, IAdditionalTargetInformation info) {
		craftingModule.itemLost(item, info);
	}

	public IInventory getDummyInventory() {
		return craftingModule.getDummyInventory();
	}

	public IInventory getFluidInventory() {
		return craftingModule.getFluidInventory();
	}

	public IInventory getCleanupInventory() {
		return craftingModule.getCleanupInventory();
	}
	
	public boolean hasCraftingSign() {
		for(int i=0;i<6;i++) {
			if(signItem[i] instanceof CraftingPipeSign) {
				return true;
			}
		}
		return false;
	}
}
