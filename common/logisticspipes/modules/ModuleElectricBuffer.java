package logisticspipes.modules;

import logisticspipes.interfaces.IInventoryUtil;
import logisticspipes.interfaces.routing.IFilter;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.pipefxhandlers.Particles;
import logisticspipes.pipes.PipeLogisticsChassi.ChassiTargetInformation;
import logisticspipes.pipes.basic.CoreRoutedPipe.ItemSendMode;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.SinkReply;
import logisticspipes.utils.SinkReply.FixedPriority;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.tuples.Triplet;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;

import java.util.List;

public class ModuleElectricBuffer extends LogisticsModule {

	private int currentTickCount = 0;
	private int ticksToAction = 80;

	public ModuleElectricBuffer() {}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {}

	private SinkReply _sinkReply;

	@Override
	public void registerPosition(ModulePositionType slot, int positionInt) {
		super.registerPosition(slot, positionInt);
		_sinkReply = new SinkReply(FixedPriority.ElectricBuffer, 0, true, false, 1, 0, new ChassiTargetInformation(getPositionInt()));
	}

	@Override
	public BlockPos getblockpos() {
		return _service.getblockpos();
	}

	@Override
	public SinkReply sinksItem(ItemIdentifier stack, int bestPriority, int bestCustomPriority, boolean allowDefault, boolean includeInTransit) {
		if (bestPriority >= FixedPriority.ElectricBuffer.ordinal()) {
			return null;
		}
		if (SimpleServiceLocator.IC2Proxy.isElectricItem(stack.makeNormalStack(1))) {
			if (_service.canUseEnergy(1)) {
				return _sinkReply;
			}
		}
		return null;
	}

	@Override
	public LogisticsModule getSubModule(int slot) {
		return null;
	}

	@Override
	public void tick() {
		if (++currentTickCount < ticksToAction) {
			return;
		}
		currentTickCount = 0;

		IInventoryUtil inv = _service.getPointedInventory(true);
		if (inv == null) {
			return;
		}
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack == null) {
				continue;
			}
			if (SimpleServiceLocator.IC2Proxy.isElectricItem(stack)) {
				Triplet<Integer, SinkReply, List<IFilter>> reply = SimpleServiceLocator.logisticsManager.hasDestinationWithMinPriority(ItemIdentifier.get(stack), _service.getSourceID(), true, FixedPriority.ElectricManager);
				if (reply == null) {
					continue;
				}
				_service.spawnParticle(Particles.OrangeParticle, 2);
				_service.sendStack(inv.decrStackSize(i, 1), reply, ItemSendMode.Normal);
				return;
			}
			continue;
		}
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
		return true;
	}

	@Override
	public boolean recievePassive() {
		return true;
	}
}
