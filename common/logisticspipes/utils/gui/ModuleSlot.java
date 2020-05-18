package logisticspipes.utils.gui;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import lombok.Getter;

import logisticspipes.items.ItemModule;
import logisticspipes.logisticspipes.ItemModuleInformationManager;
import logisticspipes.pipes.PipeLogisticsChassi;

public class ModuleSlot extends RestrictedSlot {

	@Getter
	private PipeLogisticsChassi _pipe;
	@Getter
	private int _moduleIndex;

	public ModuleSlot(IInventory iinventory, int i, int j, int k, PipeLogisticsChassi pipe) {
		super(iinventory, i, j, k, ItemModule.class);
		_pipe = pipe;
		_moduleIndex = i;
	}

	@Nonnull
	@Override
	public ItemStack onTake(EntityPlayer pl, @Nonnull ItemStack itemStack) {
		ItemModuleInformationManager.saveInformation(itemStack, _pipe.getSubModule(_moduleIndex));
		return super.onTake(pl, itemStack);
	}
}
