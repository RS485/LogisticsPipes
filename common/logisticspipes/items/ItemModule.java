package logisticspipes.items;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import logisticspipes.LogisticsPipes;
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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.minecraftforge.registries.IForgeRegistry;
import org.lwjgl.input.Keyboard;

public class ItemModule extends LogisticsItem {

	private static class Module {

		private Supplier<? extends LogisticsModule> moduleConstructor;
		private Class<? extends LogisticsModule> moduleClass;

		private Module(Supplier<? extends LogisticsModule> moduleConstructor) {
			this.moduleConstructor = moduleConstructor;
			this.moduleClass = moduleConstructor.get().getClass();
		}

		private LogisticsModule getILogisticsModule() {
			if (moduleConstructor == null) {
				return null;
			}
			return moduleConstructor.get();
		}

		private Class<? extends LogisticsModule> getILogisticsModuleClass() {
			return moduleClass;
		}

		@SideOnly(Side.CLIENT)
		private void registerModuleModel(Item item) {
			LogisticsModule instance = moduleConstructor.get();
			String path = instance.getModuleModelPath();
			if (path != null) {
				ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation("logisticspipes:" + path, "inventory"));
			}
		}
	}

	private Module moduleType;

	public ItemModule(Module moduleType) {
		super();
		this.moduleType = moduleType;
		setHasSubtypes(false);
	}

	public static void loadModules(IForgeRegistry<Item> registry) {
		registerModule(registry, "item_sink", ModuleItemSink::new);
		registerModule(registry, "passive_supplier", ModulePassiveSupplier::new);
		registerModule(registry, "extractor", ModuleExtractor::new);
		registerModule(registry, "item_sink_polymorphic", ModulePolymorphicItemSink::new);
		registerModule(registry, "quick_sort", ModuleQuickSort::new);
		registerModule(registry, "terminus", ModuleTerminus::new);
		registerModule(registry, "extractor_advanced", ModuleAdvancedExtractor::new);
		registerModule(registry, "extractor_mk2", ModuleExtractorMk2::new);
		registerModule(registry, "extractor_advanced_mk2", ModuleAdvancedExtractorMK2::new);
		registerModule(registry, "extractor_mk3", ModuleExtractorMk3::new);
		registerModule(registry, "extractor_advanced_mk3", ModuleAdvancedExtractorMK3::new);
		registerModule(registry, "provider", ModuleProvider::new);
		registerModule(registry, "provider_mk2", ModuleProviderMk2::new);
		registerModule(registry, "electric_manager", ModuleElectricManager::new);
		registerModule(registry, "electric_buffer", ModuleElectricBuffer::new);
		registerModule(registry, "apiarist_analyser", ModuleApiaristAnalyser::new);
		registerModule(registry, "apiarist_sink", ModuleApiaristSink::new);
		registerModule(registry, "apiarist_refiller", ModuleApiaristRefiller::new);
		registerModule(registry, "apiarist_terminus", ModuleApiaristTerminus::new);
		registerModule(registry, "item_sink_mod_based", ModuleModBasedItemSink::new);
		registerModule(registry, "item_sink_oredict", ModuleOreDictItemSink::new);
		// registerModule(registry, "thaumic_aspect_sink", ModuleThaumicAspectSink::new);
		registerModule(registry, "enchantment_sink", ModuleEnchantmentSink::new);
		registerModule(registry, "enchantment_sink_mk2", ModuleEnchantmentSinkMK2::new);
		registerModule(registry, "quick_sort_cc", ModuleCCBasedQuickSort::new);
		registerModule(registry, "item_sink_cc", ModuleCCBasedItemSink::new);
		registerModule(registry, "crafter", ModuleCrafter::new);
		registerModule(registry, "crafter_mk2", ModuleCrafterMK2::new);
		registerModule(registry, "crafter_mk3", ModuleCrafterMK3::new);
		registerModule(registry, "active_supplier", ModuleActiveSupplier::new);
		registerModule(registry, "item_sink_creativetab", ModuleCreativeTabBasedItemSink::new);
	}

	public static void registerModule(IForgeRegistry<Item> registry, String name, @Nonnull Supplier<? extends LogisticsModule> moduleConstructor) {
		Module module = new Module(moduleConstructor);
		ItemModule mod = LogisticsPipes.setName(new ItemModule(module), String.format("module_%s", name));
		LogisticsPipes.LogisticsModules.put(moduleConstructor, mod); // TODO account for registry overrides → move to init or something
		registry.register(mod);
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
		if (currentModule != null) {
			if (moduleType.getILogisticsModuleClass().equals(currentModule.getClass())) {
				return currentModule;
			}
		}
		LogisticsModule newmodule = moduleType.getILogisticsModule();
		if (newmodule == null) {
			return null;
		}
		newmodule.registerHandler(world, service);
		return newmodule;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModels() {
		moduleType.registerModuleModel(this);
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
