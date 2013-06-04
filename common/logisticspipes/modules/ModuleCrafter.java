package logisticspipes.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import logisticspipes.api.IRoutedPowerProvider;
import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.SidedInventoryForgeAdapter;
import logisticspipes.utils.SidedInventoryMinecraftAdapter;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import logisticspipes.utils.WorldUtil;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

//IHUDModuleHandler, 
public class ModuleCrafter extends LogisticsModule{
	
	//private final SimpleInventory _filterInventory = new SimpleInventory(9, "Requested items", 1);
	/*private boolean _isDefaultRoute;
	private int slot = 0;


*/
	
//	private IHUDModuleRenderer HUD = new HUDItemSink(this);
	private final PipeItemsCraftingLogistics pipe;
//	private IRoutedPowerProvider _power;
	
	private final List<EntityPlayer> localModeWatchers = new ArrayList<EntityPlayer>();
	
	public ModuleCrafter(PipeItemsCraftingLogistics parent) {
		pipe=parent;
	}

	@Override
	public void registerHandler(IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IRoutedPowerProvider powerprovider) {
//		_power = powerprovider;
	}


	@Override 
	public void registerSlot(int slot) {
	}
	
	@Override 
	public final int getX() {
		return this.pipe.getX();
	}
	@Override 
	public final int getY() {
		return this.pipe.getY();
	}
	
	@Override 
	public final int getZ() {
		return this.pipe.getZ();
	}
	
	private static final SinkReply _sinkReply = new SinkReply(FixedPriority.ItemSink, 0, true, false, 1, 0);
	private static final SinkReply _sinkReplyDefault = new SinkReply(FixedPriority.DefaultRoute, 0, true, true, 1, 0);
	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		if(bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) return null;
		//if(pipe.getSpecificInterests().contains(item))
			return new SinkReply(_sinkReply, spaceFor(item));
		//return null;
	}

	private int spaceFor(ItemIdentifier item){
		int count=0;
		WorldUtil wUtil = new WorldUtil(pipe.worldObj,pipe.getX(),pipe.getY(),pipe.getZ());
		for (AdjacentTile tile : wUtil.getAdjacentTileEntities(true)){
			if (!(tile.tile instanceof IInventory)) continue;
			if (tile.tile instanceof TileGenericPipe) continue;
			IInventory base = (IInventory) tile.tile;
			if (base instanceof net.minecraft.inventory.ISidedInventory) {
				base = new SidedInventoryMinecraftAdapter((net.minecraft.inventory.ISidedInventory) base, tile.orientation.getOpposite(),false);
			}
			if (base instanceof net.minecraftforge.common.ISidedInventory) {
				base = new SidedInventoryForgeAdapter((net.minecraftforge.common.ISidedInventory) base, tile.orientation.getOpposite());
			}
			IInventoryUtil inv =SimpleServiceLocator.inventoryUtilFactory.getInventoryUtil(base);
			count += inv.roomForItem(item, 9999);
		}
		return count;
	}
	
	@Override
	public LogisticsModule getSubModule(int slot) {return null;}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
	//	_filterInventory.readFromNBT(nbttagcompound, "");
	//	setDefaultRoute(nbttagcompound.getBoolean("defaultdestination"));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
    //	_filterInventory.writeToNBT(nbttagcompound, "");
    //	nbttagcompound.setBoolean("defaultdestination", isDefaultRoute());
	}

	@Override
	public void tick() {}

/*
	@Override
	public void startWatching() {
		MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_START_WATCHING_MODULE, getX(), getY(), getZ(), slot).getPacket());
	}

	@Override
	public void stopWatching() {
		MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.HUD_START_WATCHING_MODULE, getX(), getY(), getZ(), slot).getPacket());
	}

	@Override
	public void startWatching(EntityPlayer player) {
		localModeWatchers.add(player);
		MainProxy.sendPacketToPlayer(new PacketModuleInvContent(NetworkConstants.MODULE_INV_CONTENT, getX(), getY(), getZ(), slot, ItemIdentifierStack.getListFromInventory(_filterInventory)).getPacket(), (Player)player);
		MainProxy.sendPacketToPlayer(new PacketModuleInteger(NetworkConstants.ITEM_SINK_STATUS, getX(), getY(), getZ(), slot, isDefaultRoute() ? 1 : 0).getPacket(), (Player)player);
	}

	@Override
	public void stopWatching(EntityPlayer player) {
		localModeWatchers.remove(player);
	}

	@Override
	public void InventoryChanged(SimpleInventory inventory) {
		MainProxy.sendToPlayerList(new PacketModuleInvContent(NetworkConstants.MODULE_INV_CONTENT, getX(), getY(), getZ(), slot, ItemIdentifierStack.getListFromInventory(inventory)).getPacket(), localModeWatchers);
	}

	@Override
	public IHUDModuleRenderer getRenderer() {
		return HUD;
	}

	@Override
	public void handleInvContent(Collection<ItemIdentifierStack> list) {
		_filterInventory.handleItemIdentifierList(list);
	}*/

	@Override
	public boolean hasGenericInterests() {
		return false;
	}

	@Override
	public Collection<ItemIdentifier> getSpecificInterests() {
		return pipe.getSpecificInterests();
	}

	@Override
	public boolean interestedInAttachedInventory() {		
		return false;
		// when we are default we are interested in everything anyway, otherwise we're only interested in our filter.
	}

	@Override
	public boolean interestedInUndamagedID() {
		return false;
	}

	@Override
	public boolean recievePassive() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconTexture(IconRegister register) {
		return null;
	}
}
