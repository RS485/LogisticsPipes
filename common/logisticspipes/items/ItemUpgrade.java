package logisticspipes.items;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;

import logisticspipes.LPConstants;
import logisticspipes.LogisticsPipes;
import logisticspipes.modules.abstractmodules.LogisticsModule;
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

		private Class<? extends IPipeUpgrade> upgradeClass;
		private String texturePath;

		private Upgrade(Class<? extends IPipeUpgrade> moduleClass, String texturePath) {
			upgradeClass = moduleClass;
			this.texturePath = texturePath;
		}

		private IPipeUpgrade getIPipeUpgrade() {
			if (upgradeClass == null) {
				return null;
			}
			try {
				return upgradeClass.getConstructor(new Class[] {}).newInstance();
			} catch (IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | SecurityException e) {
				e.printStackTrace();
			}
			return null;
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
		setUnlocalizedName("itemModule." + upgradeType.getIPipeUpgradeClass().getSimpleName());
		setRegistryName("itemModule." + upgradeType.getIPipeUpgradeClass().getSimpleName());
	}

	public static void loadUpgrades() {
		registerUpgrade(CombinedSneakyUpgrade.class, "itemupgrade/sneakycombination");
		registerUpgrade(SneakyUpgradeConfig.class, "itemupgrade/sneaky");
		registerUpgrade(SpeedUpgrade.class, "itemupgrade/speed");
		registerUpgrade(ConnectionUpgradeConfig.class, "itemupgrade/dis");

		registerUpgrade(AdvancedSatelliteUpgrade.class, "itemupgrade/satellite");
		registerUpgrade(FluidCraftingUpgrade.class, "itemupgrade/fluidcrafting");
		registerUpgrade(CraftingByproductUpgrade.class, "itemupgrade/craftingbyproduct");
		registerUpgrade(PatternUpgrade.class, "itemupgrade/placementrules");
		registerUpgrade(FuzzyUpgrade.class, "itemupgrade/fuzzycrafting");
		registerUpgrade(PowerTransportationUpgrade.class, "itemupgrade/powertransport");
		registerUpgrade(BCPowerSupplierUpgrade.class, "itemupgrade/powertransportbc");
		registerUpgrade(RFPowerSupplierUpgrade.class, "itemupgrade/powertransportte");
		registerUpgrade(IC2LVPowerSupplierUpgrade.class, "itemupgrade/powertransportic2-lv");
		registerUpgrade(IC2MVPowerSupplierUpgrade.class, "itemupgrade/powertransportic2-mv");
		registerUpgrade(IC2HVPowerSupplierUpgrade.class, "itemupgrade/powertransportic2-hv");
		registerUpgrade(IC2EVPowerSupplierUpgrade.class, "itemupgrade/powertransportic2-ev");
		registerUpgrade(CCRemoteControlUpgrade.class, "itemupgrade/ccremotecontrol");
		registerUpgrade(CraftingMonitoringUpgrade.class, "itemupgrade/craftingmonitoring");
		registerUpgrade(OpaqueUpgrade.class, "itemupgrade/opaqueupgrade");
		registerUpgrade(CraftingCleanupUpgrade.class, "itemupgrade/craftingcleanup");
		registerUpgrade(LogicControllerUpgrade.class, "itemupgrade/logiccontroller");
		registerUpgrade(UpgradeModuleUpgrade.class, "itemupgrade/upgrademodule");
	}

	public static void registerUpgrade(Class<? extends IPipeUpgrade> upgradeClass, String texturePath) {
		Upgrade upgrade = new Upgrade(upgradeClass, texturePath);
		LogisticsPipes.LogisticsUpgrades.put(upgradeClass, LogisticsPipes.registerItem(new ItemUpgrade(upgrade)));
	}

	@Override
	public CreativeTabs getCreativeTab() {
		return CreativeTabs.REDSTONE;
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
	public String getUnlocalizedName() {
		return "item." + upgradeType.getIPipeUpgradeClass().getSimpleName();
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		return "item." + upgradeType.getIPipeUpgradeClass().getSimpleName();
	}

	@Override
	public String getItemStackDisplayName(ItemStack itemstack) {
		return StringUtils.translate(getUnlocalizedName(itemstack));
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
