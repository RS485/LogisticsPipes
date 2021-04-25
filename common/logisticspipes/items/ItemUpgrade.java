package logisticspipes.items;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import org.lwjgl.input.Keyboard;

import logisticspipes.LPItems;
import logisticspipes.LogisticsPipes;
import logisticspipes.pipes.upgrades.ActionSpeedUpgrade;
import logisticspipes.pipes.upgrades.AdvancedSatelliteUpgrade;
import logisticspipes.pipes.upgrades.CCRemoteControlUpgrade;
import logisticspipes.pipes.upgrades.CombinedSneakyUpgrade;
import logisticspipes.pipes.upgrades.ConnectionUpgradeConfig;
import logisticspipes.pipes.upgrades.CraftingByproductUpgrade;
import logisticspipes.pipes.upgrades.CraftingCleanupUpgrade;
import logisticspipes.pipes.upgrades.CraftingMonitoringUpgrade;
import logisticspipes.pipes.upgrades.FluidCraftingUpgrade;
import logisticspipes.pipes.upgrades.FuzzyUpgrade;
import logisticspipes.pipes.upgrades.IPipeUpgrade;
import logisticspipes.pipes.upgrades.ItemExtractionUpgrade;
import logisticspipes.pipes.upgrades.ItemStackExtractionUpgrade;
import logisticspipes.pipes.upgrades.LogicControllerUpgrade;
import logisticspipes.pipes.upgrades.OpaqueUpgrade;
import logisticspipes.pipes.upgrades.PatternUpgrade;
import logisticspipes.pipes.upgrades.PowerTransportationUpgrade;
import logisticspipes.pipes.upgrades.SneakyUpgradeConfig;
import logisticspipes.pipes.upgrades.SpeedUpgrade;
import logisticspipes.pipes.upgrades.UpgradeModuleUpgrade;
import logisticspipes.pipes.upgrades.power.BCPowerSupplierUpgrade;
import logisticspipes.pipes.upgrades.power.IC2EVPowerSupplierUpgrade;
import logisticspipes.pipes.upgrades.power.IC2HVPowerSupplierUpgrade;
import logisticspipes.pipes.upgrades.power.IC2LVPowerSupplierUpgrade;
import logisticspipes.pipes.upgrades.power.IC2MVPowerSupplierUpgrade;
import logisticspipes.pipes.upgrades.power.RFPowerSupplierUpgrade;
import network.rs485.logisticspipes.util.TextUtil;

public class ItemUpgrade extends LogisticsItem {

	//Values
	public static final int MAX_LIQUID_CRAFTER = 3;
	public static final int MAX_CRAFTING_CLEANUP = 4;
	public static final int MAX_ITEM_EXTRACTION = 8;
	public static final int MAX_ITEM_STACK_EXTRACTION = 8;

	private static class Upgrade {

		private Supplier<? extends IPipeUpgrade> upgradeConstructor;
		private Class<? extends IPipeUpgrade> upgradeClass;

		private Upgrade(Supplier<? extends IPipeUpgrade> moduleConstructor) {
			upgradeConstructor = moduleConstructor;
			upgradeClass = moduleConstructor.get().getClass();
		}

		private IPipeUpgrade getIPipeUpgrade() {
			if (upgradeConstructor == null) {
				return null;
			}
			return upgradeConstructor.get();
		}

		private Class<? extends IPipeUpgrade> getIPipeUpgradeClass() {
			return upgradeClass;
		}
	}

	private Upgrade upgradeType;

	public ItemUpgrade(Upgrade upgradeType) {
		super();
		this.upgradeType = upgradeType;
		setHasSubtypes(false);
	}

	public static void loadUpgrades(IForgeRegistry<Item> registry) {
		registerUpgrade(registry, CombinedSneakyUpgrade.getName(), CombinedSneakyUpgrade::new);
		registerUpgrade(registry, SneakyUpgradeConfig.getName(), SneakyUpgradeConfig::new);
		registerUpgrade(registry, SpeedUpgrade.getName(), SpeedUpgrade::new);
		registerUpgrade(registry, ConnectionUpgradeConfig.getName(), ConnectionUpgradeConfig::new);

		registerUpgrade(registry, AdvancedSatelliteUpgrade.getName(), AdvancedSatelliteUpgrade::new);
		registerUpgrade(registry, FluidCraftingUpgrade.getName(), FluidCraftingUpgrade::new);
		registerUpgrade(registry, CraftingByproductUpgrade.getName(), CraftingByproductUpgrade::new);
		registerUpgrade(registry, PatternUpgrade.getName(), PatternUpgrade::new);
		registerUpgrade(registry, FuzzyUpgrade.getName(), FuzzyUpgrade::new);
		registerUpgrade(registry, PowerTransportationUpgrade.getName(), PowerTransportationUpgrade::new);
		registerUpgrade(registry, BCPowerSupplierUpgrade.getName(), BCPowerSupplierUpgrade::new);
		registerUpgrade(registry, RFPowerSupplierUpgrade.getName(), RFPowerSupplierUpgrade::new);
		registerUpgrade(registry, IC2LVPowerSupplierUpgrade.getName(), IC2LVPowerSupplierUpgrade::new);
		registerUpgrade(registry, IC2MVPowerSupplierUpgrade.getName(), IC2MVPowerSupplierUpgrade::new);
		registerUpgrade(registry, IC2HVPowerSupplierUpgrade.getName(), IC2HVPowerSupplierUpgrade::new);
		registerUpgrade(registry, IC2EVPowerSupplierUpgrade.getName(), IC2EVPowerSupplierUpgrade::new);
		registerUpgrade(registry, CCRemoteControlUpgrade.getName(), CCRemoteControlUpgrade::new);
		registerUpgrade(registry, CraftingMonitoringUpgrade.getName(), CraftingMonitoringUpgrade::new);
		registerUpgrade(registry, OpaqueUpgrade.getName(), OpaqueUpgrade::new);
		registerUpgrade(registry, CraftingCleanupUpgrade.getName(), CraftingCleanupUpgrade::new);
		registerUpgrade(registry, LogicControllerUpgrade.getName(), LogicControllerUpgrade::new);
		registerUpgrade(registry, UpgradeModuleUpgrade.getName(), UpgradeModuleUpgrade::new);
		registerUpgrade(registry, ActionSpeedUpgrade.getName(), ActionSpeedUpgrade::new);
		registerUpgrade(registry, ItemExtractionUpgrade.getName(), ItemExtractionUpgrade::new);
		registerUpgrade(registry, ItemStackExtractionUpgrade.getName(), ItemStackExtractionUpgrade::new);
	}

	@Nonnull
	public static Item getAndCheckUpgrade(ResourceLocation resource) {
		Objects.requireNonNull(resource, "Resource for upgrade is null. Was the upgrade registered?");
		return Objects.requireNonNull(Item.REGISTRY.getObject(resource), "Upgrade " + resource.toString() + " not found in Item registry");
	}

	public static void registerUpgrade(IForgeRegistry<Item> registry, String name, Supplier<? extends IPipeUpgrade> upgradeConstructor) {
		ItemUpgrade item = LogisticsPipes.setName(new ItemUpgrade(new Upgrade(upgradeConstructor)), String.format("upgrade_%s", name));
		LPItems.upgrades.put(name, item.getRegistryName());
		registry.register(item);
	}

	public IPipeUpgrade getUpgradeForItem(@Nonnull ItemStack itemStack, IPipeUpgrade currentUpgrade) {
		if (itemStack.isEmpty()) {
			return null;
		}
		if (itemStack.getItem() != this) {
			return null;
		}
		if (upgradeType.getIPipeUpgradeClass() == null) {
			return null;
		}
		if (currentUpgrade != null) {
			if (upgradeType.getIPipeUpgradeClass().equals(currentUpgrade.getClass())) {
				return currentUpgrade;
			}
		}
		IPipeUpgrade newupgrade = upgradeType.getIPipeUpgrade();
		if (newupgrade == null) {
			return null;
		}
		return newupgrade;
	}

	@Override
	public String getModelSubdir() {
		return "upgrade";
	}

	public static String SHIFT_INFO_PREFIX = "item.upgrade.info.";

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		IPipeUpgrade upgrade = getUpgradeForItem(stack, null);
		if (upgrade == null) {
			return;
		}
		List<String> pipe = Arrays.asList(upgrade.getAllowedPipes());
		List<String> module = Arrays.asList(upgrade.getAllowedModules());
		if (pipe.isEmpty() && module.isEmpty()) {
			return;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
			if (!pipe.isEmpty() && !module.isEmpty()) {
				//Can be applied to {0} pipes
				//and {0} modules
				String base1 = TextUtil.translate(ItemUpgrade.SHIFT_INFO_PREFIX + "both1");
				String base2 = TextUtil.translate(ItemUpgrade.SHIFT_INFO_PREFIX + "both2");
				tooltip.add(MessageFormat.format(base1, join(pipe)));
				tooltip.add(MessageFormat.format(base2, join(module)));
			} else if (!pipe.isEmpty()) {
				//Can be applied to {0} pipes
				String base = TextUtil.translate(ItemUpgrade.SHIFT_INFO_PREFIX + "pipe");
				tooltip.add(MessageFormat.format(base, join(pipe)));
			} else {
				//Can be applied to {0} modules
				String base = TextUtil.translate(ItemUpgrade.SHIFT_INFO_PREFIX + "module");
				tooltip.add(MessageFormat.format(base, join(module)));
			}
		} else {
			TextUtil.addTooltipInformation(stack, tooltip, false);
		}
	}

	@SideOnly(Side.CLIENT)
	private String join(List<String> join) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < join.size() - 2; i++) {
			builder.append(TextUtil.translate(ItemUpgrade.SHIFT_INFO_PREFIX + join.get(i)));
			builder.append(", ");
		}
		if (join.size() > 1) {
			builder.append(TextUtil.translate(ItemUpgrade.SHIFT_INFO_PREFIX + join.get(join.size() - 2)));
			builder.append(" and ");
		}
		builder.append(TextUtil.translate(ItemUpgrade.SHIFT_INFO_PREFIX + join.get(join.size() - 1)));
		return builder.toString();
	}
}
