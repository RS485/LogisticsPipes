package net.minecraft.src.buildcraft.logisticspipes.modules;

import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.buildcraft.logisticspipes.IInventoryProvider;
import net.minecraft.src.buildcraft.logisticspipes.SidedInventoryAdapter;
import net.minecraft.src.forge.ISidedInventory;

public class ModuleExtractorMk3 extends ModuleExtractorMk2 {

	public ModuleExtractorMk3(IInventoryProvider invProvider, ISendRoutedItem itemSender) {
		super(invProvider, itemSender);
	}

	protected int ticksToAction(){
		return 0;
	}

	protected int itemsToExtract(){
		return 64;
	}
}
