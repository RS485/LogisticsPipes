package net.minecraft.src.buildcraft.logisticspipes.items;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.src.ItemStack;
import net.minecraft.src.ModLoader;
import net.minecraft.src.World;
import net.minecraft.src.core_LogisticsPipes;
import net.minecraft.src.buildcraft.logisticspipes.IInventoryProvider;
import net.minecraft.src.buildcraft.logisticspipes.modules.ILogisticsModule;
import net.minecraft.src.buildcraft.logisticspipes.modules.ISendRoutedItem;
import net.minecraft.src.buildcraft.logisticspipes.modules.IWorldProvider;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleAdvancedExtractor;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleAdvancedExtractorMK2;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleAdvancedExtractorMK3;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleApiaristAnalyser;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleApiaristSink;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleElectricManager;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleExtractor;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleExtractorMk2;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleExtractorMk3;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleItemSink;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModulePassiveSupplier;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModulePolymorphicItemSink;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleProvider;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleQuickSort;
import net.minecraft.src.buildcraft.logisticspipes.modules.ModuleTerminus;

public class ItemModule extends ItemModuleProxy {
	
	//Texture Map
	public static final String textureMap =	"0000111111111111" +
											"0000011111111111" +
											"0000000001111111" +
											"1110111011111111" +
											"1110111011111111" +
											"0111111111111111" +
											"1111111111111111" +
											"1111111111111111" +
											"1111111111111111" +
											"1111111111111111" +
											"1111111111111111" +
											"1111111111111111" +
											"1111111111111111" +
											"1111111111111111" +
											"1111111111111111" +
											"1111111111111111";

	
	//PASSIVE MODULES
	public static final int BLANK = 0;
	public static final int ITEMSINK = 1;
	public static final int PASSIVE_SUPPLIER = 2;
	public static final int EXTRACTOR = 3;
	public static final int POLYMORPHIC_ITEMSINK = 4;
	public static final int QUICKSORT = 5;
	public static final int TERMINUS = 6;
	public static final int ADVANCED_EXTRACTOR = 7;
	public static final int BEEANALYZER = 8;
	public static final int BEESINK = 9;

	//PASSIVE MK 2
	public static final int EXTRACTOR_MK2 = 100 + EXTRACTOR;
	public static final int ADVANCED_EXTRACTOR_MK2 = 100 + ADVANCED_EXTRACTOR;
	
	//PASSIVE MK 3
	public static final int EXTRACTOR_MK3 = 200 + EXTRACTOR;
	public static final int ADVANCED_EXTRACTOR_MK3 = 200 + ADVANCED_EXTRACTOR;

	public static final int ELECTRICMANAGER = 300;
	
	
	//ACTIVE MODULES
	public static final int PROVIDER = 500;
	
	private List<Module> modules = new ArrayList<Module>();
	
	private class Module {
		private String name;
		private int id;
		private Class<? extends ILogisticsModule> moduleClass;
		private int textureIndex = -1;

		private Module(int id, String name, Class<? extends ILogisticsModule> moduleClass) {
			this.id = id;
			this.name = name;
			this.moduleClass = moduleClass;
		}

		private Module(int id, String name, Class<? extends ILogisticsModule> moduleClass, int textureIndex) {
			this.id = id;
			this.name = name;
			this.moduleClass = moduleClass;
			this.textureIndex = textureIndex;
		}
		
		private ILogisticsModule getILogisticsModule() {
			if(moduleClass == null) return null;
			try {
				return (ILogisticsModule)moduleClass.getConstructor(new Class[]{}).newInstance(new Object[]{});
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		private Class<? extends ILogisticsModule> getILogisticsModuleClass() {
			return moduleClass;
		}
		
		private int getId() {
			return id;
		}
		
		private String getName() {
			return name;
		}
		
		private int getTextureIndex() {
			return textureIndex;
		}
	}
	
	public ItemModule(int i) {
		super(i);
		this.hasSubtypes = true;
	}
	
	public void loadModules() {
		super.loadModules();
		registerModule(BLANK					, "Blank module"				, null);
		registerModule(ITEMSINK					, "ItemSink module"				, ModuleItemSink.class);
		registerModule(PASSIVE_SUPPLIER			, "Passive Supplier module"		, ModulePassiveSupplier.class);
		registerModule(EXTRACTOR				, "Extractor module"			, ModuleExtractor.class);
		registerModule(POLYMORPHIC_ITEMSINK		, "Polymorphic ItemSink module"	, ModulePolymorphicItemSink.class);
		registerModule(QUICKSORT				, "QuickSort module"			, ModuleQuickSort.class);
		registerModule(TERMINUS					, "Terminus module"				, ModuleTerminus.class);
		registerModule(ADVANCED_EXTRACTOR		, "Advanced Extractor module"	, ModuleAdvancedExtractor.class);
		registerModule(EXTRACTOR_MK2			, "Extractor MK2 module"		, ModuleExtractorMk2.class);
		registerModule(ADVANCED_EXTRACTOR_MK2	, "Advanced Extractor MK2"		, ModuleAdvancedExtractorMK2.class);
		registerModule(EXTRACTOR_MK3			, "Extractor MK3 module"		, ModuleExtractorMk3.class);
		registerModule(ADVANCED_EXTRACTOR_MK3	, "Advanced Extractor MK3"		, ModuleAdvancedExtractorMK3.class);
		registerModule(PROVIDER					, "Provider module"				, ModuleProvider.class);
		registerModule(ELECTRICMANAGER			, "Electric Manager module"		, ModuleElectricManager.class, 96);
		registerModule(BEEANALYZER				, "Bee Analyzer module"			, ModuleApiaristAnalyser.class);
		registerModule(BEESINK					, "BeeSink module"				, ModuleApiaristSink.class);
	}
	
	public void registerModule(int id, String name, Class<? extends ILogisticsModule> moduleClass) {
		boolean flag = true;
		for(Module module:modules) {
			if(module.getId() == id) {
				flag = false;
			}
		}
		if(!"".equals(name) && flag) {
			modules.add(new Module(id,name,moduleClass));
		} else if(!flag) {
			throw new UnsupportedOperationException("Someting went wrong while registering a new Logistics Pipe Module. (Id " + id + " already in use)");
		} else {
			throw new UnsupportedOperationException("Someting went wrong while registering a new Logistics Pipe Module. (No name given)");
		}
	}
	
	public void registerModule(int id, String name, Class<? extends ILogisticsModule> moduleClass, int textureId) {
		boolean flag = true;
		for(Module module:modules) {
			if(module.getId() == id) {
				flag = false;
			}
		}
		if(!"".equals(name) && flag) {
			modules.add(new Module(id,name,moduleClass,textureId));
		} else if(!flag) {
			throw new UnsupportedOperationException("Someting went wrong while registering a new Logistics Pipe Module. (Id " + id + " already in use)");
		} else {
			throw new UnsupportedOperationException("Someting went wrong while registering a new Logistics Pipe Module. (No name given)");
		}
	}
	
	public int addOverlay(String newFileName) {
		return ModLoader.addOverride(core_LogisticsPipes.LOGISTICSITEMS_TEXTURE_FILE, newFileName);
	}
	
	public int[] getRegisteredModulesIDs() {
		int[] array = new int[modules.size()];
		int i = 0;
		for(Module module:modules) {
			array[i++] = module.getId();
		}
		return array;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addCreativeItems(ArrayList itemList) {
		for(Module module:modules) {
			itemList.add(new ItemStack(this, 1, module.getId()));
		}
	}
	
	public ILogisticsModule getModuleForItem(ItemStack itemStack, ILogisticsModule currentModule, IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world){
		if (itemStack == null) return null;
		if (itemStack.itemID != this.shiftedIndex) return null;
		for(Module module:modules) {
			if(itemStack.getItemDamage() == module.getId()) {
				if(module.getILogisticsModuleClass() == null) return null;
				if(currentModule != null) {
					if (module.getILogisticsModuleClass().equals(currentModule.getClass())) return currentModule;
				}
				ILogisticsModule newmodule = module.getILogisticsModule();
				if(newmodule == null) return null;
				newmodule.registerHandler(invProvider, itemSender, world);
				return newmodule;
			}
		}
		return null;
	}

	@Override
	public String getModuleDisplayName(ItemStack itemstack) {
		for(Module module:modules) {
			if(itemstack.getItemDamage() == module.getId()) {
				return module.getName();
			}
		}
		return null;
	}
	
	@Override
	public int getModuleIconFromDamage(int i) {
		for(Module module:modules) {
			if(module.getId() == i) {
				if(module.getTextureIndex() != -1) {
					return module.getTextureIndex();
				}
			}
		}
		
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
	public String getTextureMap() {
		return textureMap;
	}
}
