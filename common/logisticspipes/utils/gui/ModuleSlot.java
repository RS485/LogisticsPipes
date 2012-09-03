package logisticspipes.utils.gui;

import logisticspipes.config.Configs;
import logisticspipes.logisticspipes.ItemModuleInformationManager;
import logisticspipes.pipes.PipeLogisticsChassi;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;

public class ModuleSlot extends RestrictedSlot {
	
	private PipeLogisticsChassi _pipe;
	private int _moduleIndex;
	
	public ModuleSlot(IInventory iinventory, int i, int j, int k, PipeLogisticsChassi pipe) {
		super(iinventory, i, j, k, Configs.ItemModuleId + 256);
		_pipe = pipe;
		_moduleIndex = i;
	}
	
	public void onPickupFromSlot(ItemStack itemStack) {
		ItemModuleInformationManager.saveInfotmation(itemStack, _pipe.getLogisticsModule().getSubModule(_moduleIndex), _pipe.worldObj);
        super.onPickupFromSlot(itemStack);
    }
}
