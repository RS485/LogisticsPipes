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
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.routing.order.IOrderInfoProvider.ResourceType;
import logisticspipes.utils.AdjacentTile;
import logisticspipes.utils.CacheHolder.CacheTypes;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.InventoryHelper;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.BufferMode;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.tuples.Triplet;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

import net.minecraftforge.common.util.ForgeDirection;

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

	@Override
	//function-called-on-module-removal-from-pipe
	public void onAllowedRemoval() {
		inv.dropContents(getWorld(), getX(), getY(), getZ());
	}

	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		if (bestPriority > _sinkReply.fixedPriority.ordinal() || (bestPriority == _sinkReply.fixedPriority.ordinal() && bestCustomPriority >= _sinkReply.customPriority)) {
			return null;
		}
		return new SinkReply(_sinkReply, spaceFor(item, includeInTransit, true), isForBuffer(item, includeInTransit) ? BufferMode.BUFFERED : areAllOrderesToBuffer() ? BufferMode.DESTINATION_BUFFERED : BufferMode.NONE);
	}

	protected int spaceFor(ItemIdentifier item, boolean includeInTransit, boolean addBufferSpace) {
		Triplet<String, ItemIdentifier, Boolean> key = new Triplet<String, ItemIdentifier, Boolean>("spaceForMK3", item, addBufferSpace);
		int invSpace = super.spaceFor(item, includeInTransit);
		Object cache = _service.getCacheHolder().getCacheFor(CacheTypes.Inventory, key);
		if (cache != null) {
			return invSpace + (Integer) cache;
		}
		int modify = 0;
		if (addBufferSpace) {
			for (int i = 0; i < inv.getSizeInventory(); i++) {
				if (inv.getIDStackInSlot(i) == null) {
					modify += inv.getInventoryStackLimit();
				} else if (inv.getIDStackInSlot(i).getItem().equals(item)) {
					modify += (inv.getInventoryStackLimit() - inv.getIDStackInSlot(i).getStackSize());
				}
			}
		} else {
			Map<ItemIdentifier, Integer> items = inv.getItemsAndCount();
			if (items.containsKey(item)) {
				modify -= items.get(item);
			}
		}
		_service.getCacheHolder().setCache(CacheTypes.Inventory, key, modify);
		return invSpace + modify;
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
	public IIcon getIconTexture(IIconRegister register) {
		return register.registerIcon("logisticspipes:itemModule/ModuleCrafterMK3");
	}

	@Override
	public void tick() {
		super.tick();
		if (inv.isEmpty()) {
			return;
		}
		if (!_service.isNthTick(6)) {
			return;
		}
		//Add from internal buffer
		List<AdjacentTile> crafters = locateCrafters();
		boolean change = false;
		for (AdjacentTile tile : crafters) {
			for (int i = inv.getSizeInventory() - 1; i >= 0; i--) {
				ItemIdentifierStack slot = inv.getIDStackInSlot(i);
				if (slot == null) {
					continue;
				}
				ForgeDirection insertion = tile.orientation.getOpposite();
				if (getUpgradeManager().hasSneakyUpgrade()) {
					insertion = getUpgradeManager().getSneakyOrientation();
				}
				ItemIdentifierStack toadd = slot.clone();
				toadd.setStackSize(Math.min(toadd.getStackSize(), toadd.getItem().getMaxStackSize()));
				if (_service.getItemOrderManager().hasOrders(ResourceType.CRAFTING)) {
					toadd.setStackSize(Math.min(toadd.getStackSize(), ((IInventory) tile.tile).getInventoryStackLimit()));
					ItemStack added = InventoryHelper.getTransactorFor(tile.tile, tile.orientation.getOpposite()).add(toadd.makeNormalStack(), insertion, true);
					slot.setStackSize(slot.getStackSize() - added.stackSize);
					if (added.stackSize != 0) {
						change = true;
					}
				} else {
					_service.queueRoutedItem(SimpleServiceLocator.routedItemHelper.createNewTravelItem(toadd), tile.orientation.getOpposite());
					slot.setStackSize(slot.getStackSize() - toadd.getStackSize());
					change = true;
				}
				if (slot.getStackSize() <= 0) {
					inv.clearInventorySlotContents(i);
				} else {
					inv.setInventorySlotContents(i, slot);
				}
			}
		}
		if (change) {
			inv.markDirty();
			_service.getCacheHolder().trigger(CacheTypes.Inventory);
		}

	}

	@Override
	public void InventoryChanged(IInventory inventory) {
		if (MainProxy.isServer(_world.getWorld())) {
			MainProxy.sendToPlayerList(PacketHandler.getPacket(ModuleInventory.class).setIdentList(ItemIdentifierStack.getListFromInventory(inv, true)).setModulePos(this), localModeWatchers);
		}
	}

	@Override
	public void handleInvContent(Collection<ItemIdentifierStack> list) {
		bufferList.clear();
		bufferList.addAll(list);
	}

	@Override
	public void startWatching(EntityPlayer player) {
		MainProxy.sendPacketToPlayer(PacketHandler.getPacket(ModuleInventory.class).setIdentList(ItemIdentifierStack.getListFromInventory(inv, true)).setModulePos(this), player);
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

	/**
	 * does not claim ownership of the stack
	 */
	@Override
	public int addToBuffer(ItemIdentifierStack stack, IAdditionalTargetInformation info) {
		return inv.addCompressed(stack.makeNormalStack(), true);
	}
}
