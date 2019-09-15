package logisticspipes.items;

import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import net.minecraftforge.registries.IForgeRegistry;

import org.lwjgl.input.Keyboard;

import logisticspipes.LPConstants;
import logisticspipes.LPItems;
import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.IPipeServiceProvider;
import logisticspipes.interfaces.IWorldProvider;
import logisticspipes.logisticspipes.ItemModuleInformationManager;
import logisticspipes.modules.abstractmodules.LogisticsGuiModule;
import logisticspipes.modules.abstractmodules.LogisticsModule;
import logisticspipes.modules.abstractmodules.LogisticsModule.ModulePositionType;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.item.ItemIdentifierInventory;
import logisticspipes.utils.string.StringUtils;

public class ItemModule extends LogisticsItem {

	private Module moduleType;

	public ItemModule(Module moduleType) {
		super();
		this.moduleType = moduleType;
		setHasSubtypes(false);
	}

	public static void registerModule(IForgeRegistry<Item> registry, String name, @Nonnull Supplier<? extends LogisticsModule> moduleConstructor) {
		registerModule(registry, name, moduleConstructor, LPConstants.LP_MOD_ID);
	}

	public static void registerModule(IForgeRegistry<Item> registry, String name, @Nonnull Supplier<? extends LogisticsModule> moduleConstructor, String modID) {
		Module module = new Module(moduleConstructor);
		ItemModule mod = LogisticsPipes.setName(new ItemModule(module), String.format("module_%s", name), modID);
		LPItems.modules.put(module.getILogisticsModuleClass(), mod); // TODO account for registry overrides → move to init or something
		registry.register(mod);
	}

	private void openConfigGui(ItemStack stack, EntityPlayer player, World world) {
		LogisticsModule module = getModuleForItem(stack, null, null, null);
		if (module != null && module.hasGui()) {
			if (stack != null && stack.getCount() > 0) {
				ItemModuleInformationManager.readInformation(stack, module);
				module.registerPosition(ModulePositionType.IN_HAND, player.inventory.currentItem);
				((LogisticsGuiModule) module).getInHandGuiProviderForModule().open(player);
			}
		}
	}

	@Override
	@Nonnull
	public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, @Nonnull final EnumHand hand) {
		if (MainProxy.isServer(player.world)) {
			openConfigGui(player.getHeldItem(hand), player, world);
		}
		return super.onItemRightClick(world, player, hand);
	}

	@Override
	@Nonnull
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, Direction facing, float hitX, float hitY, float hitZ) {
		if (MainProxy.isServer(player.world)) {
			BlockEntity tile = world.getBlockEntity(pos);
			if (tile instanceof LogisticsTileGenericPipe) {
				if (player.getDisplayName().getUnformattedText().equals("ComputerCraft")) { // Allow turtle to place modules in pipes.
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

	@Nullable
	public LogisticsModule getModuleForItem(ItemStack itemStack, LogisticsModule currentModule, IWorldProvider world, IPipeServiceProvider service) {
		if (itemStack == null) {
			return null;
		}
		if (itemStack.getItem() != this) {
			return null;
		}
		if (currentModule != null) {
			if (moduleType.getLogisticsModuleClass().equals(currentModule.getClass())) {
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
	public String getModelSubdir() {
		return "module";
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if (stack.hasTag()) {
			CompoundTag nbt = stack.getTag();
			assert nbt != null;

			if (nbt.hasKey("informationList")) {
				if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
					ListTag listTag = nbt.getTagList("informationList", 8);
					for (int i = 0; i < listTag.tagCount(); i++) {
						Object nbttag = listTag.get(i);
						String data = ((NBTTagString) nbttag).getString();
						if (data.equals("<inventory>") && i + 1 < listTag.tagCount()) {
							nbttag = listTag.get(i + 1);
							data = ((NBTTagString) nbttag).getString();
							if (data.startsWith("<that>")) {
								String prefix = data.substring(6);
								CompoundTag module = nbt.getCompoundTag("moduleInformation");
								int size = module.getTagList(prefix + "items", module.getId()).tagCount();
								if (module.hasKey(prefix + "itemsCount")) {
									size = module.getInteger(prefix + "itemsCount");
								}
								ItemIdentifierInventory inv = new ItemIdentifierInventory(size, "InformationTempInventory", Integer.MAX_VALUE);
								inv.readFromNBT(module, prefix);
								for (int pos = 0; pos < inv.getSizeInventory(); pos++) {
									ItemStack identStack = inv.getIDStackInSlot(pos);
									if (identStack != null) {
										if (identStack.getCount() > 1) {
											tooltip.add("  " + identStack.getCount() + "x " + identStack.getFriendlyName());
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
