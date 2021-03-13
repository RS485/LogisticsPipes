package logisticspipes.utils.gui;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import lombok.Getter;

import logisticspipes.items.ItemModule;
import logisticspipes.logisticspipes.ItemModuleInformationManager;
import logisticspipes.pipes.PipeLogisticsChassis;

public class ModuleSlot extends RestrictedSlot {

	@Getter
	private final PipeLogisticsChassis _pipe;
	@Getter
	private final int _moduleIndex;

	public ModuleSlot(IInventory iinventory, int i, int j, int k, PipeLogisticsChassis pipe) {
		super(iinventory, i, j, k, ItemModule.class);
		_pipe = pipe;
		_moduleIndex = i;
	}

	@Nonnull
	@Override
	public ItemStack onTake(@Nonnull EntityPlayer player, @Nonnull ItemStack itemStack) {
		ItemModuleInformationManager.saveInformation(itemStack, _pipe.getSubModule(_moduleIndex));
		return super.onTake(player, itemStack);
	}
}
