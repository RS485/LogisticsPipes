package net.minecraft.src.buildcraft.logisticspipes.modules;

import java.util.ArrayList;

import net.minecraft.src.ItemStack;
import net.minecraft.src.buildcraft.krapht.LogisticsItem;
import net.minecraft.src.buildcraft.logisticspipes.IInventoryProvider;
import net.minecraft.src.buildcraft.logisticspipes.modules.ISendRoutedItem;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleExtractorMk2;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModulePolymorphicItemSink;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleExtractor;
import net.minecraft.src.buildcraft.logisticspipes.modules.ILogisticsModule;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleItemSink;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModulePassiveSupplier;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleProvider;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleQuickSort;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleTerminus;

public class ItemModule extends LogisticsItem{

	
	//PASSIVE MODULES
	public static final int BLANK = 0;
	public static final int ITEMSINK = 1;
	public static final int PASSIVE_SUPPLIER = 2;
	public static final int EXTRACTOR = 3;
	public static final int POLYMORPHIC_ITEMSINK = 4;
	public static final int QUICKSORT = 5;
	public static final int TERMINUS = 6;
	
	//PASSIVE MK 2
	public static final int EXTRACTOR_MK2 = 100 + EXTRACTOR;
	
	
	//ACTIVE MODULES
	public static final int PROVIDER = 500;
	
	
	public ItemModule(int i) {
		super(i);
		this.hasSubtypes = true;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addCreativeItems(ArrayList itemList) {
		for (int i = 0; i <= 6; i++){
			itemList.add(new ItemStack(this, 1, i));
		}
		
		for (int i = 103; i <= 103; i++){
			itemList.add(new ItemStack(this, 1, i));
		}
		
		for (int i = 500; i <= 500; i++){
			itemList.add(new ItemStack(this, 1, i));
		}
		
	}
	
	public ILogisticsModule getModuleForItem(ItemStack itemStack, ILogisticsModule currentModule, IInventoryProvider invProvider, ISendRoutedItem itemSender){
		if (itemStack == null) return null;
		if (itemStack.itemID != this.shiftedIndex) return null;
		switch (itemStack.getItemDamage()){
		
			//PASSIVE
			case ITEMSINK:
				if (currentModule instanceof ModuleItemSink) return currentModule;
				return new ModuleItemSink();
			case PASSIVE_SUPPLIER:
				if (currentModule instanceof ModulePassiveSupplier) return currentModule;
				return new ModulePassiveSupplier(invProvider);
			case EXTRACTOR:
				if (currentModule != null && currentModule.getClass().equals(ModuleExtractor.class)) return currentModule; 
				//currentModule instanceof ModuleExtractor && !(currentModule instanceof ModuleExtractorMk2)) return currentModule;
				return new ModuleExtractor(invProvider, itemSender);
			case POLYMORPHIC_ITEMSINK:
				if (currentModule instanceof ModulePolymorphicItemSink) return currentModule;
				return new ModulePolymorphicItemSink(invProvider);
			case QUICKSORT:
				if (currentModule instanceof ModuleQuickSort) return currentModule;
				return new ModuleQuickSort(invProvider, itemSender);
			case TERMINUS:
				if (currentModule instanceof ModuleTerminus) return currentModule;
				return new ModuleTerminus();
				
			//PASSIVE MK2
			case EXTRACTOR_MK2:
				if (currentModule != null && currentModule.getClass().equals(ModuleExtractorMk2.class)) return currentModule;
				//if (currentModule instanceof ModuleExtractorMk2 && !(currentModule instanceof ModuleExtractor)) return currentModule;
				return new ModuleExtractorMk2(invProvider, itemSender);
				
			//ACTIVE
			case PROVIDER:
				if (currentModule instanceof ModuleProvider) return currentModule;
				return new ModuleProvider(invProvider, itemSender);
			default:
				return null;
		}
			
		
	}
	
	
	
}
