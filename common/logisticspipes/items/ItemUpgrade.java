package logisticspipes.items;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import net.minecraftforge.client.model.ModelLoader;
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
		private String texturePath;

		private Upgrade(Supplier<? extends IPipeUpgrade> moduleConstructor, String texturePath) {
			upgradeConstructor = moduleConstructor;
			upgradeClass = moduleConstructor.get().getClass();
			this.texturePath = texturePath;
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

		@SideOnly(Side.CLIENT)
		private void registerUpgradeModel(Item item) {
			if (texturePath == null) {
				ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation("logisticspipes:" + item.getUnlocalizedName().replace("item.", "") + "/blank", "inventory"));
			} else {
				ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation("logisticspipes:" + texturePath, "inventory"));
			}
		}
	}

	private Upgrade upgradeType;

	public ItemUpgrade(Upgrade upgradeType) {
		super();
		this.upgradeType = upgradeType;
		setHasSubtypes(false);
	}

	public static void loadUpgrades(IForgeRegistry<Item> registry) {
		registerUpgrade(registry, "sneaky_combination", CombinedSneakyUpgrade::new, "itemupgrade/sneakycombination");
		registerUpgrade(registry, "sneaky", SneakyUpgradeConfig::new, "itemupgrade/sneaky");
		registerUpgrade(registry, "speed", SpeedUpgrade::new, "itemupgrade/speed");
		registerUpgrade(registry, "connection", ConnectionUpgradeConfig::new, "itemupgrade/dis");

		registerUpgrade(registry, "satellite_advanced", AdvancedSatelliteUpgrade::new, "itemupgrade/satellite");
		registerUpgrade(registry, "fluid_crafting", FluidCraftingUpgrade::new, "itemupgrade/fluidcrafting");
		registerUpgrade(registry, "crafting_byproduct", CraftingByproductUpgrade::new, "itemupgrade/craftingbyproduct");
		registerUpgrade(registry, "pattern", PatternUpgrade::new, "itemupgrade/placementrules");
		registerUpgrade(registry, "fuzzy", FuzzyUpgrade::new, "itemupgrade/fuzzycrafting");
		registerUpgrade(registry, "power_transportation", PowerTransportationUpgrade::new, "itemupgrade/powertransport");
		registerUpgrade(registry, "power_supplier_bc", BCPowerSupplierUpgrade::new, "itemupgrade/powertransportbc");
		registerUpgrade(registry, "power_supplier_rf", RFPowerSupplierUpgrade::new, "itemupgrade/powertransportte");
		registerUpgrade(registry, "power_supplier_ic2_lv", IC2LVPowerSupplierUpgrade::new, "itemupgrade/powertransportic2-lv");
		registerUpgrade(registry, "power_supplier_ic2_mv", IC2MVPowerSupplierUpgrade::new, "itemupgrade/powertransportic2-mv");
		registerUpgrade(registry, "power_supplier_ic2_hv", IC2HVPowerSupplierUpgrade::new, "itemupgrade/powertransportic2-hv");
		registerUpgrade(registry, "power_supplier_ic2_ev", IC2EVPowerSupplierUpgrade::new, "itemupgrade/powertransportic2-ev");
		registerUpgrade(registry, "cc_remote_control", CCRemoteControlUpgrade::new, "itemupgrade/ccremotecontrol");
		registerUpgrade(registry, "crafting_monitoring", CraftingMonitoringUpgrade::new, "itemupgrade/craftingmonitoring");
		registerUpgrade(registry, "opaque", OpaqueUpgrade::new, "itemupgrade/opaqueupgrade");
		registerUpgrade(registry, "crafting_cleanup", CraftingCleanupUpgrade::new, "itemupgrade/craftingcleanup");
		registerUpgrade(registry, "logic_controller", LogicControllerUpgrade::new, "itemupgrade/logiccontroller");
		registerUpgrade(registry, "module_upgrade", UpgradeModuleUpgrade::new, "itemupgrade/upgrademodule");
	}

	public static void registerUpgrade(IForgeRegistry<Item> registry, String name, Supplier<? extends IPipeUpgrade> upgradeConstructor, String texturePath) {
		Upgrade upgrade = new Upgrade(upgradeConstructor, texturePath);
		ItemUpgrade item = LogisticsPipes.setName(new ItemUpgrade(upgrade), String.format("upgrade_%s", name));
		LogisticsPipes.LogisticsUpgrades.put(upgradeConstructor, item); // TODO account for registry overrides → move to init or something
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
	@SideOnly(Side.CLIENT)
	public void registerModels() {
		upgradeType.registerUpgradeModel(this);
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
