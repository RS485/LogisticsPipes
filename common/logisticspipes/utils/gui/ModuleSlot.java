package logisticspipes.utils.gui;

import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.logisticspipes.ChassiModule;
import logisticspipes.logisticspipes.ItemModuleInformationManager;
import logisticspipes.pipes.PipeLogisticsChassi;


import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.NBTTagString;

public class ModuleSlot extends RestrictedSlot {
	
	private PipeLogisticsChassi _pipe;
	private int _moduleIndex;
	
	public ModuleSlot(IInventory iinventory, int i, int j, int k, PipeLogisticsChassi pipe) {
		super(iinventory, i, j, k, LogisticsPipes.ItemModuleId + 256);
		_pipe = pipe;
		_moduleIndex = i;
	}
	
	public void onPickupFromSlot(ItemStack itemStack) {
		ItemModuleInformationManager.saveInfotmation(itemStack, _pipe.getLogisticsModule().getSubModule(_moduleIndex), _pipe.worldObj);
        super.onPickupFromSlot(itemStack);
    }
}
