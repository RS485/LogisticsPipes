package logisticspipes.krapht.gui;

import java.util.List;

import logisticspipes.mod_LogisticsPipes;
import logisticspipes.buildcraft.krapht.pipes.PipeLogisticsChassi;
import logisticspipes.buildcraft.logisticspipes.ChassiModule;
import logisticspipes.buildcraft.logisticspipes.ItemModuleInformationManager;
import logisticspipes.buildcraft.logisticspipes.modules.IClientInformationProvider;


import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.NBTTagString;

public class ModuleSlot extends RestrictedSlot {
	
	private PipeLogisticsChassi _pipe;
	private int _moduleIndex;
	
	public ModuleSlot(IInventory iinventory, int i, int j, int k, PipeLogisticsChassi pipe) {
		super(iinventory, i, j, k, mod_LogisticsPipes.ItemModuleId + 256);
		_pipe = pipe;
		_moduleIndex = i;
	}
	
	public void onPickupFromSlot(ItemStack itemStack) {
		ItemModuleInformationManager.saveInfotmation(itemStack, _pipe.getLogisticsModule().getSubModule(_moduleIndex));
        super.onPickupFromSlot(itemStack);
    }
}
