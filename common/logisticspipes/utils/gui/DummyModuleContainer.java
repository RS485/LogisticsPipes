package logisticspipes.utils.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import logisticspipes.items.ItemModule;
import logisticspipes.logisticspipes.ItemModuleInformationManager;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.modules.abstractmodules.LogisticsModule.ModulePositionType;
import logisticspipes.utils.DummyWorldProvider;

public class DummyModuleContainer extends DummyContainer {

	private LogisticsModule module;
	private int slot;

	public DummyModuleContainer(EntityPlayer player, int slot) {
		super(player.inventory, null);
		this.slot = slot;
		ItemStack moduleStack = player.inventory.mainInventory.get(slot);
		module = ((ItemModule) moduleStack.getItem()).getModuleForItem(moduleStack, null, new DummyWorldProvider(player.world), null);
		module.registerPosition(ModulePositionType.IN_HAND, slot);
		ItemModuleInformationManager.readInformation(moduleStack, module);
	}

	public LogisticsModule getModule() {
		return module;
	}

	public void setInventory(IInventory inv) {
		_dummyInventory = inv;
	}

	@Override
	protected Slot addSlotToContainer(Slot par1Slot) {
		if (par1Slot != null && par1Slot.getSlotIndex() == slot && par1Slot.inventory == _playerInventory) {
			return super.addSlotToContainer(new UnmodifiableSlot(par1Slot));
		}
		return super.addSlotToContainer(par1Slot);
	}

	@Override
	public void onContainerClosed(EntityPlayer par1EntityPlayer) {
		super.onContainerClosed(par1EntityPlayer);
		ItemModuleInformationManager.saveInformation(par1EntityPlayer.inventory.mainInventory.get(slot), module);
		par1EntityPlayer.inventory.markDirty();
	}
}
