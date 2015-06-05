package logisticspipes.items;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import logisticspipes.interfaces.IPipeServiceProvider;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.logisticspipes.ItemModuleInformationManager;
import logisticspipes.modules.ModuleActiveSupplier;
import logisticspipes.modules.ModuleAdvancedExtractor;
import logisticspipes.modules.ModuleAdvancedExtractorMK2;
import logisticspipes.modules.ModuleAdvancedExtractorMK3;
import logisticspipes.modules.ModuleApiaristAnalyser;
import logisticspipes.modules.ModuleApiaristRefiller;
import logisticspipes.modules.ModuleApiaristSink;
import logisticspipes.modules.ModuleApiaristTerminus;
import logisticspipes.modules.ModuleCCBasedItemSink;
import logisticspipes.modules.ModuleCCBasedQuickSort;
import logisticspipes.modules.ModuleCrafter;
import logisticspipes.modules.ModuleCrafterMK2;
import logisticspipes.modules.ModuleCrafterMK3;
import logisticspipes.modules.ModuleCreativeTabBasedItemSink;
import logisticspipes.modules.ModuleElectricBuffer;
import logisticspipes.modules.ModuleElectricManager;
import logisticspipes.modules.ModuleEnchantmentSink;
import logisticspipes.modules.ModuleEnchantmentSinkMK2;
import logisticspipes.modules.ModuleExtractor;
import logisticspipes.modules.ModuleExtractorMk2;
import logisticspipes.modules.ModuleExtractorMk3;
import logisticspipes.modules.ModuleItemSink;
import logisticspipes.modules.ModuleModBasedItemSink;
import logisticspipes.modules.ModuleOreDictItemSink;
import logisticspipes.modules.ModulePassiveSupplier;
import logisticspipes.modules.ModulePolymorphicItemSink;
import logisticspipes.modules.ModuleProvider;
import logisticspipes.modules.ModuleProviderMk2;
import logisticspipes.modules.ModuleQuickSort;
import logisticspipes.modules.ModuleTerminus;
import logisticspipes.modules.ModuleThaumicAspectSink;
import logisticspipes.modules.abstractmodules.LogisticsGuiModule;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.modules.abstractmodules.LogisticsModule.ModulePositionType;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.string.StringUtils;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;

public class ItemModule extends LogisticsItem {

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
	public static final int OREDICTITEMSINK = 13;
	public static final int CC_BASED_QUICKSORT = 14;
	public static final int CC_BASED_ITEMSINK = 15;
	public static final int CREATIVETABBASEDITEMSINK = 16;

	public static final int THAUMICASPECTSINK = 30;
	public static final int ENCHANTMENTSINK = 31;

	//PASSIVE MK 2
	public static final int EXTRACTOR_MK2 = 100 + ItemModule.EXTRACTOR;
	public static final int ADVANCED_EXTRACTOR_MK2 = 100 + ItemModule.ADVANCED_EXTRACTOR;
	public static final int ENCHANTMENTSINK_MK2 = 100 + ItemModule.ENCHANTMENTSINK;

	//PASSIVE MK 3
	public static final int EXTRACTOR_MK3 = 200 + ItemModule.EXTRACTOR;
	public static final int ADVANCED_EXTRACTOR_MK3 = 200 + ItemModule.ADVANCED_EXTRACTOR;

	public static final int ELECTRICMANAGER = 300;
	public static final int ELECTRICBUFFER = 301;

	//Providers MODULES
	public static final int PROVIDER = 500;
	public static final int PROVIDER_MK2 = 501;
	public static final int ACTIVE_SUPPLIER = 502;

	//Crafter MODULES
	public static final int CRAFTER = 600;
	public static final int CRAFTER_MK2 = 601;
	public static final int CRAFTER_MK3 = 602;

	private List<Module> modules = new ArrayList<Module>();

	private class Module {

		private int id;
		private Class<? extends LogisticsModule> moduleClass;
		private IIcon moduleIcon = null;

		private Module(int id, Class<? extends LogisticsModule> moduleClass) {
			this.id = id;
			this.moduleClass = moduleClass;
		}

		private LogisticsModule getILogisticsModule() {
			if (moduleClass == null) {
				return null;
			}
			try {
				return moduleClass.getConstructor(new Class[] {}).newInstance(new Object[] {});
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

		private Class<? extends LogisticsModule> getILogisticsModuleClass() {
			return moduleClass;
		}

		private int getId() {
			return id;
		}

		private IIcon getIcon() {
			return moduleIcon;
		}

		@SideOnly(Side.CLIENT)
		private void registerModuleIcon(IIconRegister par1IIconRegister) {
			if (moduleClass == null) {
				moduleIcon = par1IIconRegister.registerIcon("logisticspipes:" + getUnlocalizedName().replace("item.", "") + "/blank");
			} else {
				try {
					LogisticsModule instance = moduleClass.newInstance();
					moduleIcon = instance.getIconTexture(par1IIconRegister);
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public ItemModule() {
		hasSubtypes = true;
	}

	public void loadModules() {
		registerModule(ItemModule.BLANK, null);
		registerModule(ItemModule.ITEMSINK, ModuleItemSink.class);
		registerModule(ItemModule.PASSIVE_SUPPLIER, ModulePassiveSupplier.class);
		registerModule(ItemModule.EXTRACTOR, ModuleExtractor.class);
		registerModule(ItemModule.POLYMORPHIC_ITEMSINK, ModulePolymorphicItemSink.class);
		registerModule(ItemModule.QUICKSORT, ModuleQuickSort.class);
		registerModule(ItemModule.TERMINUS, ModuleTerminus.class);
		registerModule(ItemModule.ADVANCED_EXTRACTOR, ModuleAdvancedExtractor.class);
		registerModule(ItemModule.EXTRACTOR_MK2, ModuleExtractorMk2.class);
		registerModule(ItemModule.ADVANCED_EXTRACTOR_MK2, ModuleAdvancedExtractorMK2.class);
		registerModule(ItemModule.EXTRACTOR_MK3, ModuleExtractorMk3.class);
		registerModule(ItemModule.ADVANCED_EXTRACTOR_MK3, ModuleAdvancedExtractorMK3.class);
		registerModule(ItemModule.PROVIDER, ModuleProvider.class);
		registerModule(ItemModule.PROVIDER_MK2, ModuleProviderMk2.class);
		registerModule(ItemModule.ELECTRICMANAGER, ModuleElectricManager.class);
		registerModule(ItemModule.ELECTRICBUFFER, ModuleElectricBuffer.class);
		registerModule(ItemModule.BEEANALYZER, ModuleApiaristAnalyser.class);
		registerModule(ItemModule.BEESINK, ModuleApiaristSink.class);
		registerModule(ItemModule.APIARISTREFILLER, ModuleApiaristRefiller.class);
		registerModule(ItemModule.APIARISTTERMINUS, ModuleApiaristTerminus.class);
		registerModule(ItemModule.MODBASEDITEMSINK, ModuleModBasedItemSink.class);
		registerModule(ItemModule.OREDICTITEMSINK, ModuleOreDictItemSink.class);
		registerModule(ItemModule.THAUMICASPECTSINK, ModuleThaumicAspectSink.class);
		registerModule(ItemModule.ENCHANTMENTSINK, ModuleEnchantmentSink.class);
		registerModule(ItemModule.ENCHANTMENTSINK_MK2, ModuleEnchantmentSinkMK2.class);
		registerModule(ItemModule.CC_BASED_QUICKSORT, ModuleCCBasedQuickSort.class);
		registerModule(ItemModule.CC_BASED_ITEMSINK, ModuleCCBasedItemSink.class);
		registerModule(ItemModule.CRAFTER, ModuleCrafter.class);
		registerModule(ItemModule.CRAFTER_MK2, ModuleCrafterMK2.class);
		registerModule(ItemModule.CRAFTER_MK3, ModuleCrafterMK3.class);
		registerModule(ItemModule.ACTIVE_SUPPLIER, ModuleActiveSupplier.class);
		registerModule(ItemModule.CREATIVETABBASEDITEMSINK, ModuleCreativeTabBasedItemSink.class);
	}

	public void registerModule(int id, Class<? extends LogisticsModule> moduleClass) {
		boolean flag = true;
		for (Module module : modules) {
			if (module.getId() == id) {
				flag = false;
			}
		}
		if (flag) {
			modules.add(new Module(id, moduleClass));
		} else if (!flag) {
			throw new UnsupportedOperationException("Someting went wrong while registering a new Logistics Pipe Module. (Id " + id + " already in use)");
		} else {
			throw new UnsupportedOperationException("Someting went wrong while registering a new Logistics Pipe Module. (No name given)");
		}
	}

	public int[] getRegisteredModulesIDs() {
		int[] array = new int[modules.size()];
		int i = 0;
		for (Module module : modules) {
			array[i++] = module.getId();
		}
		return array;
	}

	@Override
	public CreativeTabs getCreativeTab() {
		return CreativeTabs.tabRedstone;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
		for (Module module : modules) {
			par3List.add(new ItemStack(this, 1, module.getId()));
		}
	}

	private void openConfigGui(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World) {
		LogisticsModule module = getModuleForItem(par1ItemStack, null, null, null);
		if (module != null && module.hasGui()) {
			if (par1ItemStack != null && par1ItemStack.stackSize > 0) {
				ItemModuleInformationManager.readInformation(par1ItemStack, module);
				module.registerPosition(ModulePositionType.IN_HAND, par2EntityPlayer.inventory.currentItem);
				((LogisticsGuiModule) module).getInHandGuiProviderForModule().open(par2EntityPlayer);
			}
		}
	}

	@Override
	public boolean hasEffect(ItemStack par1ItemStack) {
		LogisticsModule module = getModuleForItem(par1ItemStack, null, null, null);
		if (module != null) {
			if (par1ItemStack != null && par1ItemStack.stackSize > 0) {
				return module.hasEffect();
			}
		}
		return false;
	}

	@Override
	public ItemStack onItemRightClick(final ItemStack par1ItemStack, final World par2World, final EntityPlayer par3EntityPlayer) {
		if (MainProxy.isServer(par3EntityPlayer.worldObj)) {
			openConfigGui(par1ItemStack, par3EntityPlayer, par2World);
		}
		return super.onItemRightClick(par1ItemStack, par2World, par3EntityPlayer);
	}

	@Override
	public boolean onItemUse(final ItemStack par1ItemStack, final EntityPlayer par2EntityPlayer, final World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10) {
		if (MainProxy.isServer(par2EntityPlayer.worldObj)) {
			TileEntity tile = par3World.getTileEntity(par4, par5, par6);
			if (tile instanceof LogisticsTileGenericPipe) {
				if (par2EntityPlayer.getDisplayName().equals("ComputerCraft")) { //Allow turtle to place modules in pipes.
					CoreUnroutedPipe pipe = LogisticsBlockGenericPipe.getPipe(par3World, par4, par5, par6);
					if (LogisticsBlockGenericPipe.isValid(pipe)) {
						pipe.blockActivated(par2EntityPlayer);
					}
				}
				return true;
			}
			openConfigGui(par1ItemStack, par2EntityPlayer, par3World);
		}
		return true;
	}

	public LogisticsModule getModuleForItem(ItemStack itemStack, LogisticsModule currentModule, IWorldProvider world, IPipeServiceProvider service) {
		if (itemStack == null) {
			return null;
		}
		if (itemStack.getItem() != this) {
			return null;
		}
		for (Module module : modules) {
			if (itemStack.getItemDamage() == module.getId()) {
				if (module.getILogisticsModuleClass() == null) {
					return null;
				}
				if (currentModule != null) {
					if (module.getILogisticsModuleClass().equals(currentModule.getClass())) {
						return currentModule;
					}
				}
				LogisticsModule newmodule = module.getILogisticsModule();
				if (newmodule == null) {
					return null;
				}
				newmodule.registerHandler(world, service);
				return newmodule;
			}
		}
		return null;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		for (Module module : modules) {
			if (itemstack.getItemDamage() == module.getId()) {
				if (module.getILogisticsModuleClass() == null) {
					return "item.ModuleBlank";
				}
				return "item." + module.getILogisticsModuleClass().getSimpleName();
			}
		}
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IIconRegister) {
		if (modules.size() <= 0) {
			return;
		}
		for (Module module : modules) {
			module.registerModuleIcon(par1IIconRegister);
		}
	}

	@Override
	public IIcon getIconFromDamage(int i) {
		// should set and store TextureIndex with this object.
		for (Module module : modules) {
			if (module.getId() == i) {
				if (module.getIcon() != null) {
					return module.getIcon();
				}
			}
		}
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean flag) {
		if (itemStack.hasTagCompound()) {
			NBTTagCompound nbt = itemStack.getTagCompound();
			if (nbt.hasKey("informationList")) {
				if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
					NBTTagList nbttaglist = nbt.getTagList("informationList", 8);
					for (int i = 0; i < nbttaglist.tagCount(); i++) {
						Object nbttag = nbttaglist.tagList.get(i);
						String data = ((NBTTagString) nbttag).func_150285_a_();
						if (data.equals("<inventory>") && i + 1 < nbttaglist.tagCount()) {
							nbttag = nbttaglist.tagList.get(i + 1);
							data = ((NBTTagString) nbttag).func_150285_a_();
							if (data.startsWith("<that>")) {
								String prefix = data.substring(6);
								NBTTagCompound module = nbt.getCompoundTag("moduleInformation");
								int size = module.getTagList(prefix + "items", module.getId()).tagCount();
								if (module.hasKey(prefix + "itemsCount")) {
									size = module.getInteger(prefix + "itemsCount");
								}
								ItemIdentifierInventory inv = new ItemIdentifierInventory(size, "InformationTempInventory", Integer.MAX_VALUE);
								inv.readFromNBT(module, prefix);
								for (int pos = 0; pos < inv.getSizeInventory(); pos++) {
									ItemIdentifierStack stack = inv.getIDStackInSlot(pos);
									if (stack != null) {
										if (stack.getStackSize() > 1) {
											list.add("  " + stack.getStackSize() + "x " + stack.getFriendlyName());
										} else {
											list.add("  " + stack.getFriendlyName());
										}
									}
								}
							}
							i++;
						} else {
							list.add(data);
						}
					}
				} else {
					list.add(StringUtils.translate(StringUtils.KEY_HOLDSHIFT));
				}
			} else {
				StringUtils.addShiftAddition(itemStack, list);
			}
		} else {
			StringUtils.addShiftAddition(itemStack, list);
		}
	}
}
