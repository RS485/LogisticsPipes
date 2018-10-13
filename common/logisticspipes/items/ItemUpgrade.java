package logisticspipes.items;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import logisticspipes.LPItems;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.minecraftforge.registries.IForgeRegistry;
import org.lwjgl.input.Keyboard;

import logisticspipes.LogisticsPipes;
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
import logisticspipes.utils.string.StringUtils;

public class ItemUpgrade extends LogisticsItem {

	//Values
	public static final int MAX_LIQUID_CRAFTER = 3;
	public static final int MAX_CRAFTING_CLEANUP = 4;

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
		registerUpgrade(registry, "sneaky_combination", CombinedSneakyUpgrade::new);
		registerUpgrade(registry, "sneaky", SneakyUpgradeConfig::new);
		registerUpgrade(registry, "speed", SpeedUpgrade::new);
		registerUpgrade(registry, "disconnection", ConnectionUpgradeConfig::new);

		registerUpgrade(registry, "satellite_advanced", AdvancedSatelliteUpgrade::new);
		registerUpgrade(registry, "fluid_crafting", FluidCraftingUpgrade::new);
		registerUpgrade(registry, "crafting_byproduct", CraftingByproductUpgrade::new);
		registerUpgrade(registry, "pattern", PatternUpgrade::new);
		registerUpgrade(registry, "fuzzy", FuzzyUpgrade::new);
		registerUpgrade(registry, "power_transportation", PowerTransportationUpgrade::new);
		registerUpgrade(registry, "power_supplier_mj", BCPowerSupplierUpgrade::new);
		registerUpgrade(registry, "power_supplier_rf", RFPowerSupplierUpgrade::new);
		registerUpgrade(registry, "power_supplier_eu_lv", IC2LVPowerSupplierUpgrade::new);
		registerUpgrade(registry, "power_supplier_eu_mv", IC2MVPowerSupplierUpgrade::new);
		registerUpgrade(registry, "power_supplier_eu_hv", IC2HVPowerSupplierUpgrade::new);
		registerUpgrade(registry, "power_supplier_eu_ev", IC2EVPowerSupplierUpgrade::new);
		registerUpgrade(registry, "cc_remote_control", CCRemoteControlUpgrade::new);
		registerUpgrade(registry, "crafting_monitoring", CraftingMonitoringUpgrade::new);
		registerUpgrade(registry, "opaque", OpaqueUpgrade::new);
		registerUpgrade(registry, "crafting_cleanup", CraftingCleanupUpgrade::new);
		registerUpgrade(registry, "logic_controller", LogicControllerUpgrade::new);
		registerUpgrade(registry, "module_upgrade", UpgradeModuleUpgrade::new);
	}

	public static void registerUpgrade(IForgeRegistry<Item> registry, String name, Supplier<? extends IPipeUpgrade> upgradeConstructor) {
		Upgrade upgrade = new Upgrade(upgradeConstructor);
		ItemUpgrade item = LogisticsPipes.setName(new ItemUpgrade(upgrade), String.format("upgrade_%s", name));
		LPItems.upgrades.put(upgrade.getIPipeUpgradeClass(), item); // TODO account for registry overrides → move to init or something
		registry.register(item);
	}

	public IPipeUpgrade getUpgradeForItem(ItemStack itemStack, IPipeUpgrade currentUpgrade) {
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
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
				String base1 = StringUtils.translate(ItemUpgrade.SHIFT_INFO_PREFIX + "both1");
				String base2 = StringUtils.translate(ItemUpgrade.SHIFT_INFO_PREFIX + "both2");
				tooltip.add(MessageFormat.format(base1, join(pipe)));
				tooltip.add(MessageFormat.format(base2, join(module)));
			} else if (!pipe.isEmpty()) {
				//Can be applied to {0} pipes
				String base = StringUtils.translate(ItemUpgrade.SHIFT_INFO_PREFIX + "pipe");
				tooltip.add(MessageFormat.format(base, join(pipe)));
			} else if (!module.isEmpty()) {
				//Can be applied to {0} modules
				String base = StringUtils.translate(ItemUpgrade.SHIFT_INFO_PREFIX + "module");
				tooltip.add(MessageFormat.format(base, join(module)));
			}
		} else {
			String baseKey = MessageFormat.format("{0}.tip", stack.getItem().getUnlocalizedName(stack));
			String key = baseKey + 1;
			String translation = StringUtils.translate(key);
			if (translation.equals(key)) {
				tooltip.add(StringUtils.translate(StringUtils.KEY_HOLDSHIFT));
			}
		}
	}

	private String join(List<String> join) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < join.size() - 2; i++) {
			builder.append(StringUtils.translate(ItemUpgrade.SHIFT_INFO_PREFIX + join.get(i)));
			builder.append(", ");
		}
		if (join.size() > 1) {
			builder.append(StringUtils.translate(ItemUpgrade.SHIFT_INFO_PREFIX + join.get(join.size() - 2)));
			builder.append(" and ");
		}
		builder.append(StringUtils.translate(ItemUpgrade.SHIFT_INFO_PREFIX + join.get(join.size() - 1)));
		return builder.toString();
	}
}
