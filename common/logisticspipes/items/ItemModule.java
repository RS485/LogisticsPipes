package logisticspipes.items;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.IChassiePowerProvider;
import logisticspipes.interfaces.ILogisticsGuiModule;
import logisticspipes.interfaces.ILogisticsModule;
import logisticspipes.interfaces.ISendRoutedItem;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.logisticspipes.IInventoryProvider;
import logisticspipes.modules.ModuleAdvancedExtractor;
import logisticspipes.modules.ModuleAdvancedExtractorMK2;
import logisticspipes.modules.ModuleAdvancedExtractorMK3;
import logisticspipes.modules.ModuleApiaristAnalyser;
import logisticspipes.modules.ModuleApiaristRefiller;
import logisticspipes.modules.ModuleApiaristSink;
import logisticspipes.modules.ModuleApiaristTerminus;
import logisticspipes.modules.ModuleElectricBuffer;
import logisticspipes.modules.ModuleElectricManager;
import logisticspipes.modules.ModuleExtractor;
import logisticspipes.modules.ModuleExtractorMk2;
import logisticspipes.modules.ModuleExtractorMk3;
import logisticspipes.modules.ModuleItemSink;
import logisticspipes.modules.ModuleModBasedItemSink;
import logisticspipes.modules.ModulePassiveSupplier;
import logisticspipes.modules.ModulePolymorphicItemSink;
import logisticspipes.modules.ModuleProvider;
import logisticspipes.modules.ModuleProviderMk2;
import logisticspipes.modules.ModuleQuickSort;
import logisticspipes.modules.ModuleTerminus;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.SimpleInventory;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;

public class ItemModule extends LogisticsItem {

	//Texture Map
	/*public static final String textureMap =	"0000111111111111" +
											"0000011111111111" +
											"0000000000001111" +
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
	 */

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
	public static final int APIARISTREFILLER = 10;
	public static final int APIARISTTERMINUS = 11;
	public static final int MODBASEDITEMSINK = 12;

	//PASSIVE MK 2
	public static final int EXTRACTOR_MK2 = 100 + EXTRACTOR;
	public static final int ADVANCED_EXTRACTOR_MK2 = 100 + ADVANCED_EXTRACTOR;

	//PASSIVE MK 3
	public static final int EXTRACTOR_MK3 = 200 + EXTRACTOR;
	public static final int ADVANCED_EXTRACTOR_MK3 = 200 + ADVANCED_EXTRACTOR;

	public static final int ELECTRICMANAGER = 300;
	public static final int ELECTRICBUFFER = 301;


	//ACTIVE MODULES
	public static final int PROVIDER = 500;
	public static final int PROVIDER_MK2 = 501;

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
		registerModule(PROVIDER_MK2				, "Provider module MK2"			, ModuleProviderMk2.class);
		registerModule(ELECTRICMANAGER			, "Electric Manager module"		, ModuleElectricManager.class, 96);
		registerModule(ELECTRICBUFFER			, "Electric Buffer module"		, ModuleElectricBuffer.class, 97);
		registerModule(BEEANALYZER				, "Bee Analyzer module"			, ModuleApiaristAnalyser.class);
		registerModule(BEESINK					, "BeeSink module"				, ModuleApiaristSink.class);
		registerModule(APIARISTREFILLER			, "Apiary Refiller module"		, ModuleApiaristRefiller.class);
		registerModule(APIARISTTERMINUS			, "Drone Terminus module"		, ModuleApiaristTerminus.class);
		registerModule(MODBASEDITEMSINK			, "Mod Based ItemSink module"	, ModuleModBasedItemSink.class);
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

	public int[] getRegisteredModulesIDs() {
		int[] array = new int[modules.size()];
		int i = 0;
		for(Module module:modules) {
			array[i++] = module.getId();
		}
		return array;
	}

	@Override
	public CreativeTabs getCreativeTab()
	{
		return CreativeTabs.tabRedstone;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		for(Module module:modules) {
			par3List.add(new ItemStack(this, 1, module.getId()));
		}
	}

	private void openConfigGui(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World) {
		ILogisticsModule module = getModuleForItem(par1ItemStack, null, null, null, null, null);
		if(module != null && module instanceof ILogisticsGuiModule) {
			if(par1ItemStack != null && par1ItemStack.stackSize > 0) {
				par2EntityPlayer.openGui(LogisticsPipes.instance, -1, par3World, ((ILogisticsGuiModule)module).getGuiHandlerID(), -1 ,par2EntityPlayer.inventory.currentItem);
			}
		}
	}

	@Override
	public ItemStack onItemRightClick(final ItemStack par1ItemStack, final World par2World, final EntityPlayer par3EntityPlayer)
	{
		if(MainProxy.isServer()) {
			openConfigGui(par1ItemStack, par3EntityPlayer, par2World);
		}
		return super.onItemRightClick(par1ItemStack, par2World, par3EntityPlayer);
	}

	@Override
	public boolean onItemUse(final ItemStack par1ItemStack, final EntityPlayer par2EntityPlayer, final World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10) {
		if(MainProxy.isServer()) {
			TileEntity tile = par3World.getBlockTileEntity(par4, par5, par6);
			if(tile instanceof LogisticsTileGenericPipe) {
				return true;
			}
			openConfigGui(par1ItemStack, par2EntityPlayer, par3World);
		}
		return true;
	}

	public ILogisticsModule getModuleForItem(ItemStack itemStack, ILogisticsModule currentModule, IInventoryProvider invProvider, ISendRoutedItem itemSender, IWorldProvider world, IChassiePowerProvider power){
		if (itemStack == null) return null;
		if (itemStack.itemID != this.itemID) return null;
		for(Module module:modules) {
			if(itemStack.getItemDamage() == module.getId()) {
				if(module.getILogisticsModuleClass() == null) return null;
				if(currentModule != null) {
					if (module.getILogisticsModuleClass().equals(currentModule.getClass())) return currentModule;
				}
				ILogisticsModule newmodule = module.getILogisticsModule();
				if(newmodule == null) return null;
				newmodule.registerHandler(invProvider, itemSender, world, power);
				return newmodule;
			}
		}
		return null;
	}

	@Override
	public String getItemDisplayName(ItemStack itemstack) {
		for(Module module:modules) {
			if(itemstack.getItemDamage() == module.getId()) {
				return module.getName();
			}
		}
		return null;
	}

	@Override
	public int getIconFromDamage(int i) {
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean flag) {
		if(itemStack.hasTagCompound()) {
			if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
				NBTTagCompound nbt = itemStack.getTagCompound();
				if(nbt.hasKey("informationList")) {
					NBTTagList nbttaglist = nbt.getTagList("informationList");
					for(int i=0;i<nbttaglist.tagCount();i++) {
						NBTBase nbttag = nbttaglist.tagAt(i);
						String data = ((NBTTagString)nbttag).data;
						if(data.equals("<inventory>") && i + 1 < nbttaglist.tagCount()) {
							nbttag = nbttaglist.tagAt(i + 1);
							data = ((NBTTagString)nbttag).data;
							if(data.startsWith("<that>")) {
								String prefix = data.substring(6);
								NBTTagCompound module = nbt.getCompoundTag("moduleInformation");
								int size = module.getTagList(prefix + "items").tagCount();
								if(module.hasKey(prefix + "itemsCount")) {
									size = module.getInteger(prefix + "itemsCount");
								}
								SimpleInventory inv = new SimpleInventory(size, "InformationTempInventory", Integer.MAX_VALUE);
								inv.readFromNBT(module, prefix);
								for(int pos=0;pos < inv.getSizeInventory();pos++) {
									ItemStack stack = inv.getStackInSlot(pos);
									if(stack != null) {
										if(stack.stackSize > 1) {
											list.add("  " + stack.stackSize+"x " + ItemIdentifier.get(stack).getFriendlyName());
										} else {
											list.add("  " + ItemIdentifier.get(stack).getFriendlyName());
										}
									}
								}
							}
							i++;
						} else {
							list.add(data);
						}
					}
				}
			}
		}
	}
}
