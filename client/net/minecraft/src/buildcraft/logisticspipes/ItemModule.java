package net.minecraft.src.buildcraft.logisticspipes;

import java.util.ArrayList;

import net.minecraft.src.ItemStack;
import net.minecraft.src.buildcraft.krapht.LogisticsItem;
import net.minecraft.src.buildcraft.logisticspipes.modules.ILogisticsModule;
import net.minecraft.src.buildcraft.logisticspipes.modules.ISendRoutedItem;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleAdvancedExtractor;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleAdvancedExtractorMK2;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleAdvancedExtractorMK3;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleExtractorMk2;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleExtractorMk3;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModulePolymorphicItemSink;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleExtractor;
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
	public static final int ADVANCED_EXTRACTOR = 7;

	//PASSIVE MK 2
	public static final int EXTRACTOR_MK2 = 100 + EXTRACTOR;
	public static final int ADVANCED_EXTRACTOR_MK2 = 100 + ADVANCED_EXTRACTOR;
	
	//PASSIVE MK 3
	public static final int EXTRACTOR_MK3 = 200 + EXTRACTOR;
	public static final int ADVANCED_EXTRACTOR_MK3 = 200 + ADVANCED_EXTRACTOR;
	
	
	//ACTIVE MODULES
	public static final int PROVIDER = 500;
	
	
	public ItemModule(int i) {
		super(i);
		this.hasSubtypes = true;
	}
	
	@Override
	public int getIconFromDamage(int i) {

		if (i >= 500){
			return 5 * 16 + (i - 500);
		}
		
		if (i >= 200){
			return 4 * 16 + (i - 200);
		}
		
		if (i >= 100){
			return 3 * 16 + (i - 100);
		}
			
		return 2 * 16 + i;
	}
	
	@Override
	public String getItemDisplayName(ItemStack itemstack) {
		switch(itemstack.getItemDamage()){
			case BLANK:
				return "Blank module";
				
			//PASSIVE
			case ITEMSINK:
				return "ItemSink module";
			case PASSIVE_SUPPLIER:
				return "Passive Supplier module";
			case EXTRACTOR:
				return "Extractor module";
			case POLYMORPHIC_ITEMSINK: 
				return "Polymorphic ItemSink module";
			case QUICKSORT:
				return "QuickSort module";
			case TERMINUS:
				return "Terminus module";
			case ADVANCED_EXTRACTOR:
				return "Advanced Extractor module";
				
			//PASSIVE MK2
			case EXTRACTOR_MK2:
				return "Extractor MK2 module";
			case ADVANCED_EXTRACTOR_MK2:
				return "Advanced Extractor MK2";
				
			//PASSIVE MK3
			case EXTRACTOR_MK3:
				return "Extractor MK3 module";
			case ADVANCED_EXTRACTOR_MK3:
				return "Advanced Extractor MK3";
				
			//ACTIVE
			case PROVIDER:
				return "Provider module";
				
				
			default:
				return ""; 
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addCreativeItems(ArrayList itemList) {
		for (int i = 0; i <= 7; i++){
			itemList.add(new ItemStack(this, 1, i));
		}

		itemList.add(new ItemStack(this, 1, 103));
		itemList.add(new ItemStack(this, 1, 107));
		
		itemList.add(new ItemStack(this, 1, 203));
		itemList.add(new ItemStack(this, 1, 207));
		
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
			case ADVANCED_EXTRACTOR:
				if (currentModule != null && currentModule.getClass().equals(ModuleAdvancedExtractor.class)) return currentModule; 
				return new ModuleAdvancedExtractor(invProvider, itemSender);
				
			//PASSIVE MK2
			case EXTRACTOR_MK2:
				if (currentModule != null && currentModule.getClass().equals(ModuleExtractorMk2.class)) return currentModule;
				return new ModuleExtractorMk2(invProvider, itemSender);
			
			case ADVANCED_EXTRACTOR_MK2:
				if (currentModule != null && currentModule.getClass().equals(ModuleAdvancedExtractorMK2.class)) return currentModule;
				return new ModuleAdvancedExtractorMK2(invProvider, itemSender);
				
			//PASSIVE MK2
			case EXTRACTOR_MK3:
				if (currentModule != null && currentModule.getClass().equals(ModuleExtractorMk3.class)) return currentModule;
				return new ModuleExtractorMk3(invProvider, itemSender);
			
			case ADVANCED_EXTRACTOR_MK3:
				if (currentModule != null && currentModule.getClass().equals(ModuleAdvancedExtractorMK3.class)) return currentModule;
				return new ModuleAdvancedExtractorMK3(invProvider, itemSender);
					
			//ACTIVE
			case PROVIDER:
				if (currentModule instanceof ModuleProvider) return currentModule;
				return new ModuleProvider(invProvider, itemSender);
			default:
				return null;
		}
			
		
	}
	
	
	
}
