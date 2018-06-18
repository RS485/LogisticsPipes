package logisticspipes.network.guis.pipe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import logisticspipes.LogisticsPipes;
import logisticspipes.gui.GuiChassiPipe;
import logisticspipes.items.ItemUpgrade;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.network.abstractguis.BooleanModuleCoordinatesGuiProvider;
import logisticspipes.network.abstractguis.GuiProvider;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.pipes.upgrades.ModuleUpgradeManager;
import logisticspipes.utils.gui.DummyContainer;

public class ChassiGuiProvider extends BooleanModuleCoordinatesGuiProvider {

	public ChassiGuiProvider(int id) {
		super(id);
	}

	@Override
	public Object getClientGui(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = getPipe(player.getEntityWorld());
		if (pipe == null || !(pipe.pipe instanceof PipeLogisticsChassi)) {
			return null;
		}
		return new GuiChassiPipe(player, (PipeLogisticsChassi) pipe.pipe, isFlag());
	}

	@Override
	public DummyContainer getContainer(EntityPlayer player) {
		LogisticsTileGenericPipe pipe = getPipe(player.getEntityWorld());
		if (pipe == null || !(pipe.pipe instanceof PipeLogisticsChassi)) {
			return null;
		}
		final PipeLogisticsChassi _chassiPipe = (PipeLogisticsChassi) pipe.pipe;
		IInventory _moduleInventory = _chassiPipe.getModuleInventory();
		DummyContainer dummy = new DummyContainer(player.inventory, _moduleInventory);
		if (_chassiPipe.getChassiSize() < 5) {
			dummy.addNormalSlotsForPlayerInventory(18, 97);
		} else {
			dummy.addNormalSlotsForPlayerInventory(18, 174);
		}
		if (_chassiPipe.getChassiSize() > 0) {
			dummy.addModuleSlot(0, _moduleInventory, 19, 9, _chassiPipe);
		}
		if (_chassiPipe.getChassiSize() > 1) {
			dummy.addModuleSlot(1, _moduleInventory, 19, 29, _chassiPipe);
		}
		if (_chassiPipe.getChassiSize() > 2) {
			dummy.addModuleSlot(2, _moduleInventory, 19, 49, _chassiPipe);
		}
		if (_chassiPipe.getChassiSize() > 3) {
			dummy.addModuleSlot(3, _moduleInventory, 19, 69, _chassiPipe);
		}
		if (_chassiPipe.getChassiSize() > 4) {
			dummy.addModuleSlot(4, _moduleInventory, 19, 89, _chassiPipe);
			dummy.addModuleSlot(5, _moduleInventory, 19, 109, _chassiPipe);
			dummy.addModuleSlot(6, _moduleInventory, 19, 129, _chassiPipe);
			dummy.addModuleSlot(7, _moduleInventory, 19, 149, _chassiPipe);
		}

		if (_chassiPipe.getUpgradeManager().hasUpgradeModuleUpgrade()) {
			for (int i = 0; i < _chassiPipe.getChassiSize(); i++) {
				final int fI = i;
				ModuleUpgradeManager upgradeManager = _chassiPipe.getModuleUpgradeManager(i);
				dummy.addUpgradeSlot(0, upgradeManager, 0, 145, 9 + i * 20, itemStack -> ChassiGuiProvider.checkStack(itemStack, _chassiPipe, fI));
				dummy.addUpgradeSlot(1, upgradeManager, 1, 165, 9 + i * 20, itemStack -> ChassiGuiProvider.checkStack(itemStack, _chassiPipe, fI));
			}
		}
		return dummy;
	}

	public static boolean checkStack(ItemStack stack, PipeLogisticsChassi chassiPipe, int moduleSlot) {
		if (stack == null) {
			return false;
		}
		if (!(stack.getItem() instanceof ItemUpgrade)) {
			return false;
		}
		LogisticsModule module = chassiPipe.getModules().getModule(moduleSlot);
		if (module == null) {
			return false;
		}
		return ((ItemUpgrade)stack.getItem()).getUpgradeForItem(stack, null).isAllowedForModule(module);
	}

	@Override
	public GuiProvider template() {
		return new ChassiGuiProvider(getId());
	}
}
