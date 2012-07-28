package net.minecraft.src.krapht.gui;

import java.util.List;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.NBTTagString;
import net.minecraft.src.mod_LogisticsPipes;
import net.minecraft.src.buildcraft.krapht.pipes.PipeLogisticsChassi;
import net.minecraft.src.buildcraft.logisticspipes.ChassiModule;
import net.minecraft.src.buildcraft.logisticspipes.ItemModuleInformationManager;
import net.minecraft.src.buildcraft.logisticspipes.modules.IClientInformationProvider;

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
