package logisticspipes.utils.gui;

import logisticspipes.LogisticsPipes;
import logisticspipes.logisticspipes.ItemModuleInformationManager;
import logisticspipes.pipes.PipeLogisticsChassi;

import lombok.Getter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class ModuleSlot extends RestrictedSlot {

	@Getter
	private PipeLogisticsChassi _pipe;
	@Getter
	private int _moduleIndex;

	public ModuleSlot(IInventory iinventory, int i, int j, int k, PipeLogisticsChassi pipe) {
		super(iinventory, i, j, k, LogisticsPipes.ModuleItem);
		_pipe = pipe;
		_moduleIndex = i;
	}

	@Override
	public void onPickupFromSlot(EntityPlayer pl, ItemStack itemStack) {
		ItemModuleInformationManager.saveInfotmation(itemStack, _pipe.getLogisticsModule().getSubModule(_moduleIndex));
		super.onPickupFromSlot(pl, itemStack);
	}

	@Override
	public boolean isItemValid(ItemStack par1ItemStack) {
		if (par1ItemStack.getItemDamage() == 0) {
			return false;
		}
		return super.isItemValid(par1ItemStack);
	}
}
