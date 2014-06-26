package logisticspipes.modules;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import logisticspipes.interfaces.IBufferItems;
import logisticspipes.interfaces.IModuleInventoryReceive;
import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.module.ModuleInventory;
import logisticspipes.pipes.PipeItemsCraftingLogisticsMk3;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.InventoryHelper;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.BufferMode;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ModuleCrafterMK3 extends ModuleCrafter implements IBufferItems, ISimpleInventoryEventHandler, IModuleInventoryReceive {

	public ItemIdentifierInventory inv = new ItemIdentifierInventory(16, "Buffer", 127);
	
	public List<ItemIdentifierStack> bufferList = new LinkedList<ItemIdentifierStack>();
	
	public ModuleCrafterMK3() {
		inv.addListener(this);
	}

	public ModuleCrafterMK3(PipeItemsCraftingLogisticsMk3 parent) {
		super(parent);
		inv.addListener(this);
	}
	
	@Override //function-called-on-module-removal-from-pipe	
	public void onAllowedRemoval(){
		inv.dropContents(getWorld(), getX(), getY(), getZ());
	}
	
	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		if(bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) return null;
		return new SinkReply(_sinkReply, spaceFor(item, includeInTransit, true), isForBuffer(item, includeInTransit) ? BufferMode.BUFFERED : areAllOrderesToBuffer() ? BufferMode.DESTINATION_BUFFERED : BufferMode.NONE);
	}
	
	protected int spaceFor(ItemIdentifier item, boolean includeInTransit, boolean addBufferSpace) {
		int invSpace = super.spaceFor(item, includeInTransit);
		if(addBufferSpace) {
			for(int i=0;i<inv.getSizeInventory();i++) {
				if(inv.getIDStackInSlot(i) == null) {
					invSpace += inv.getInventoryStackLimit();
				} else if(inv.getIDStackInSlot(i).getItem().equals(item)) {
					invSpace += (inv.getInventoryStackLimit() - inv.getIDStackInSlot(i).getStackSize());
				}
			}
		} else {
			Map<ItemIdentifier, Integer> items = inv.getItemsAndCount();
			if(items.containsKey(item)) {
				invSpace -= items.get(item);
			}
		}
		return invSpace;
	}
	
	private boolean isForBuffer(ItemIdentifier item, boolean includeInTransit) {
		return spaceFor(item, includeInTransit, false) <= 0;
	}
	

	@Override
	protected int neededEnergy() {
		return 20;
	}

	@Override
	protected int itemsToExtract() {
		return 128;
	}
	
	@Override
	protected int stacksToExtract() {
		return 8;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIconTexture(IconRegister register) {
		return register.registerIcon("logisticspipes:itemModule/ModuleCrafterMK3");
	}

	@Override
	public void tick(){
		super.tick();
		if(inv.isEmpty()) return;
		if(!_service.isNthTick(6)) return;
		//Add from internal buffer
		List<AdjacentTile> crafters = locateCrafters();
		boolean change = false;
		for(AdjacentTile tile : crafters) {
			for(int i = inv.getSizeInventory() - 1; i >= 0; i--) {
				ItemIdentifierStack slot = inv.getIDStackInSlot(i);
				if(slot == null) continue;
				ForgeDirection insertion = tile.orientation.getOpposite();
				if(_service.getUpgradeManager().hasSneakyUpgrade()) {
					insertion = _service.getUpgradeManager().getSneakyOrientation();
				}
				ItemIdentifierStack toadd = slot.clone();
				toadd.setStackSize(Math.min(toadd.getStackSize(), toadd.getItem().getMaxStackSize()));
				toadd.setStackSize(Math.min(toadd.getStackSize(), ((IInventory)tile.tile).getInventoryStackLimit()));
				ItemStack added = InventoryHelper.getTransactorFor(tile.tile).add(toadd.makeNormalStack(), insertion, true);
				slot.setStackSize(slot.getStackSize() - added.stackSize);
				if(added.stackSize != 0) {
					change = true;
				}
				if(slot.getStackSize() <= 0) {
					inv.clearInventorySlotContents(i);
				} else {
					inv.setInventorySlotContents(i, slot);
				}
			}
		}
		if(change) {
			inv.onInventoryChanged();
		}

	}
	
	@Override
	public void InventoryChanged(IInventory inventory) {
		MainProxy.sendToPlayerList(PacketHandler.getPacket(ModuleInventory.class).setIdentList(ItemIdentifierStack.getListFromInventory(inv, true)).setModulePos(this), localModeWatchers);
	}

	@Override
	public void handleInvContent(Collection<ItemIdentifierStack> list) {
		bufferList.clear();
		bufferList.addAll(list);
	}

	@Override
	public void startWatching(EntityPlayer player) {
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ModuleInventory.class).setIdentList(ItemIdentifierStack.getListFromInventory(inv, true)).setModulePos(this), (Player) player);
		super.startWatching(player);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);
		inv.writeToNBT(nbttagcompound, "buffer");
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		inv.readFromNBT(nbttagcompound, "buffer");
	}

	@Override
	public int addToBuffer(ItemIdentifierStack stack, IAdditionalTargetInformation info) {
		return inv.addCompressed(stack.makeNormalStack(), true);
	}
}
