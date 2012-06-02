package net.minecraft.src.buildcraft.logisticspipes.modules;

import net.minecraft.src.buildcraft.logisticspipes.IInventoryProvider;

public class ModuleExtractorMk2 extends ModuleExtractor{
	
	public ModuleExtractorMk2(IInventoryProvider invProvider, ISendRoutedItem itemSender) {
		super(invProvider, itemSender);
	}
	
	@Override
	protected int ticksToAction() {
		return 20;
	}

}
