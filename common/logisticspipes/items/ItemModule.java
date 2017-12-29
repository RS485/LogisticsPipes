package logisticspipes.items;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

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

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

	private List<Module> modules = new ArrayList<>();

	private class Module {

		private int id;
		private Class<? extends LogisticsModule> moduleClass;

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
			} catch (IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | SecurityException e) {
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

		@SideOnly(Side.CLIENT)
		private void registerModuleModel(Item item) {
			if (moduleClass == null) {
				ModelLoader.setCustomModelResourceLocation(item, getId(), new ModelResourceLocation("logisticspipes:" + getUnlocalizedName().replace("item.", "") + "/blank", "inventory"));
			} else {
				try {
					LogisticsModule instance = moduleClass.newInstance();
					String path = instance.getModuleModelPath();
					if(path != null) {
						ModelLoader.setCustomModelResourceLocation(item, getId(), new ModelResourceLocation("logisticspipes:" + path, "inventory"));
					}
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public ItemModule() {
		super();
		setHasSubtypes(true);
		setUnlocalizedName("itemModule");
		setRegistryName("itemModule");
	}

	public void loadModules() {
		if(!modules.isEmpty()) return;
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
//		registerModule(ItemModule.THAUMICASPECTSINK, ModuleThaumicAspectSink.class);
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
		return CreativeTabs.REDSTONE;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		items.addAll(modules.stream()
				.map(module -> new ItemStack(this, 1, module.getId()))
				.collect(Collectors.toList()));
	}

	private void openConfigGui(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World) {
		LogisticsModule module = getModuleForItem(par1ItemStack, null, null, null);
		if (module != null && module.hasGui()) {
			if (par1ItemStack != null && par1ItemStack.getCount() > 0) {
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
			if (par1ItemStack != null && par1ItemStack.getCount() > 0) {
				return module.hasEffect();
			}
		}
		return false;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(final World par2World, final EntityPlayer par3EntityPlayer, final EnumHand hand) {
		if (MainProxy.isServer(par3EntityPlayer.world)) {
			openConfigGui(par3EntityPlayer.getHeldItem(hand), par3EntityPlayer, par2World);
		}
		return super.onItemRightClick(par2World, par3EntityPlayer, hand);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (MainProxy.isServer(player.world)) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof LogisticsTileGenericPipe) {
				if (player.getDisplayName().equals("ComputerCraft")) { //Allow turtle to place modules in pipes.
					CoreUnroutedPipe pipe = LogisticsBlockGenericPipe.getPipe(world, pos);
					if (LogisticsBlockGenericPipe.isValid(pipe)) {
						pipe.blockActivated(player);
					}
				}
				return EnumActionResult.PASS;
			}
			openConfigGui(player.inventory.getCurrentItem(), player, world);
		}
		return EnumActionResult.PASS;
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
	public void registerModels() {
		if (modules.size() <= 0) {
			loadModules();
		}
		for (Module module : modules) {
			module.registerModuleModel(this);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if (stack.hasTagCompound()) {
			NBTTagCompound nbt = stack.getTagCompound();
			if (nbt.hasKey("informationList")) {
				if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
					NBTTagList nbttaglist = nbt.getTagList("informationList", 8);
					for (int i = 0; i < nbttaglist.tagCount(); i++) {
						Object nbttag = nbttaglist.tagList.get(i);
						String data = ((NBTTagString) nbttag).getString();
						if (data.equals("<inventory>") && i + 1 < nbttaglist.tagCount()) {
							nbttag = nbttaglist.tagList.get(i + 1);
							data = ((NBTTagString) nbttag).getString();
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
									ItemIdentifierStack identStack = inv.getIDStackInSlot(pos);
									if (identStack != null) {
										if (identStack.getStackSize() > 1) {
											tooltip.add("  " + identStack.getStackSize() + "x " + identStack.getFriendlyName());
										} else {
											tooltip.add("  " + identStack.getFriendlyName());
										}
									}
								}
							}
							i++;
						} else {
							tooltip.add(data);
						}
					}
				} else {
					tooltip.add(StringUtils.translate(StringUtils.KEY_HOLDSHIFT));
				}
			} else {
				StringUtils.addShiftAddition(stack, tooltip);
			}
		} else {
			StringUtils.addShiftAddition(stack, tooltip);
		}
	}
}
