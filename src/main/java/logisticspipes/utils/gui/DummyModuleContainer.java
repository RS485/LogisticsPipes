package logisticspipes.utils.gui;

import java.util.Objects;
import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import logisticspipes.items.ItemModule;
import logisticspipes.logisticspipes.ItemModuleInformationManager;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.modules.LogisticsModule.ModulePositionType;
import logisticspipes.utils.DummyWorldProvider;

public class DummyModuleContainer extends DummyContainer {

	private final LogisticsModule module;
	private final int slot;

	public DummyModuleContainer(EntityPlayer player, int slot) {
		super(player.inventory, null);
		this.slot = slot;
		ItemStack moduleStack = player.inventory.mainInventory.get(slot);
		if (moduleStack.isEmpty()) throw new IllegalStateException("Module stack is empty");
		module = ((ItemModule) moduleStack.getItem()).getModuleForItem(moduleStack, null, new DummyWorldProvider(player.world), null);
		Objects.requireNonNull(module, "module was null for item " + moduleStack.toString());
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
	@Nonnull
	protected Slot addSlotToContainer(@Nonnull Slot slotIn) {
		if (slotIn.getSlotIndex() == slot && slotIn.inventory == _playerInventory) {
			return super.addSlotToContainer(new UnmodifiableSlot(slotIn));
		}
		return super.addSlotToContainer(slotIn);
	}

	@Override
	public void onContainerClosed(@Nonnull EntityPlayer player) {
		super.onContainerClosed(player);
		ItemModuleInformationManager.saveInformation(player.inventory.mainInventory.get(slot), module);
		player.inventory.markDirty();
	}
}
