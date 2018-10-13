package logisticspipes.modules;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.tuples.Pair;

import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class ModuleApiaristRefiller extends LogisticsModule {

	private int currentTickCount = 0;
	private int ticksToOperation = 200;

	public ModuleApiaristRefiller() {}

	@Override
	public SinkReply sinksItem(ItemIdentifier item, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		return null;
	}

	@Override
	public LogisticsModule getSubModule(int slot) {
		return null;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {}

	@Override
	public final int getX() {
		return _service.getX();
	}

	@Override
	public final int getY() {
		return _service.getY();
	}

	@Override
	public final int getZ() {
		return _service.getZ();
	}

	@Override
	public void tick() {
		if (++currentTickCount < ticksToOperation) {
			return;
		}
		currentTickCount = 0;
		TileEntity inv = _service.getRealInventory();
		if (!(inv instanceof ISidedInventory)) {
			return;
		}
		ISidedInventory sinv = (ISidedInventory) inv;
		EnumFacing direction = _service.inventoryOrientation().getOpposite();
		ItemStack stack = extractItem(sinv, false, direction, 1);
		if (stack == null) {
			return;
		}
		if (!(_service.canUseEnergy(100))) {
			return;
		}

		currentTickCount = ticksToOperation;

		if (reinsertBee(stack, sinv, direction)) {
			return;
		}

		Pair<Integer, SinkReply> reply = _service.hasDestination(ItemIdentifier.get(stack), true, new ArrayList<>());
		if (reply == null) {
			return;
		}
		_service.useEnergy(20);
		extractItem(sinv, true, direction, 1);
		_service.sendStack(stack, reply, ItemSendMode.Normal);
	}

	private ItemStack extractItem(ISidedInventory inv, boolean remove, EnumFacing dir, int amount) {
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			if (inv.getStackInSlot(i) != null && inv.canExtractItem(i, inv.getStackInSlot(i), dir)) {
				if (remove) {
					return inv.decrStackSize(i, amount);
				} else {
					ItemStack extracted = inv.getStackInSlot(i).copy();
					extracted.setCount(amount);
					return extracted;
				}
			}
		}
		return null;
	}

	private int addItem(ISidedInventory inv, ItemStack stack, EnumFacing dir) {
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			if (inv.getStackInSlot(i).isEmpty() && inv.canInsertItem(i, stack, dir)) {
				inv.setInventorySlotContents(i, stack);
				return stack.getCount();
			}
		}
		return 0;
	}

	private boolean reinsertBee(ItemStack stack, ISidedInventory inv, EnumFacing direction) {
		if ((inv.getStackInSlot(0) == null)) {
			if (SimpleServiceLocator.forestryProxy.isPrincess(stack)) {
				if (SimpleServiceLocator.forestryProxy.isPurebred(stack)) {
					int inserted = addItem(inv, stack, direction);
					if (inserted == 0) {
						return false;
					}
					_service.useEnergy(100);
					extractItem(inv, true, direction, 1);
					_service.spawnParticle(Particles.VioletParticle, 5);
					_service.spawnParticle(Particles.BlueParticle, 5);
					return true;
				}
			}
		}
		if ((inv.getStackInSlot(1) == null) && !(SimpleServiceLocator.forestryProxy.isQueen(inv.getStackInSlot(0)))) {
			if (SimpleServiceLocator.forestryProxy.isDrone(stack)) {
				if (SimpleServiceLocator.forestryProxy.isPurebred(stack)) {
					int inserted = addItem(inv, stack, direction);
					if (inserted == 0) {
						return false;
					}
					_service.useEnergy(100);
					extractItem(inv, true, direction, 1);
					_service.spawnParticle(Particles.VioletParticle, 5);
					_service.spawnParticle(Particles.BlueParticle, 5);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean hasGenericInterests() {
		return true;
	}

	@Override
	public List<ItemIdentifier> getSpecificInterests() {
		return null;
	}

	@Override
	public boolean interestedInAttachedInventory() {
		return false;
	}

	@Override
	public boolean interestedInUndamagedID() {
		return false;
	}

	@Override
	public boolean recievePassive() {
		return true;
	}

}
