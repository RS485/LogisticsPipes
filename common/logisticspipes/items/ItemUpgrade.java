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
import logisticspipes.pipes.upgrades.connection.ConnectionUpgradeDOWN;
import logisticspipes.pipes.upgrades.connection.ConnectionUpgradeEAST;
import logisticspipes.pipes.upgrades.connection.ConnectionUpgradeNORTH;
import logisticspipes.pipes.upgrades.connection.ConnectionUpgradeSOUTH;
import logisticspipes.pipes.upgrades.connection.ConnectionUpgradeUP;
import logisticspipes.pipes.upgrades.connection.ConnectionUpgradeWEST;
import logisticspipes.pipes.upgrades.power.IC2EVPowerSupplierUpgrade;
import logisticspipes.pipes.upgrades.power.IC2HVPowerSupplierUpgrade;
import logisticspipes.pipes.upgrades.power.IC2LVPowerSupplierUpgrade;
import logisticspipes.pipes.upgrades.power.IC2MVPowerSupplierUpgrade;
import logisticspipes.pipes.upgrades.power.RFPowerSupplierUpgrade;
import logisticspipes.pipes.upgrades.sneaky.SneakyUpgradeDOWN;
import logisticspipes.pipes.upgrades.sneaky.SneakyUpgradeEAST;
import logisticspipes.pipes.upgrades.sneaky.SneakyUpgradeNORTH;
import logisticspipes.pipes.upgrades.sneaky.SneakyUpgradeSOUTH;
import logisticspipes.pipes.upgrades.sneaky.SneakyUpgradeUP;
import logisticspipes.pipes.upgrades.sneaky.SneakyUpgradeWEST;
import logisticspipes.utils.string.StringUtils;

public class ItemUpgrade extends LogisticsItem {

	//Sneaky Upgrades
	public static final int SNEAKY_UP = 0;
	public static final int SNEAKY_DOWN = 1;
	public static final int SNEAKY_NORTH = 2;
	public static final int SNEAKY_SOUTH = 3;
	public static final int SNEAKY_EAST = 4;
	public static final int SNEAKY_WEST = 5;
	public static final int SNEAKY_COMBINATION = 6;
	public static final int SNEAKY = 7;

	//Connection Upgrades
	public static final int CONNECTION_UP = 10;
	public static final int CONNECTION_DOWN = 11;
	public static final int CONNECTION_NORTH = 12;
	public static final int CONNECTION_SOUTH = 13;
	public static final int CONNECTION_EAST = 14;
	public static final int CONNECTION_WEST = 15;
	public static final int CONNECTION = 16;

	//Speed Upgrade
	public static final int SPEED = 20;

	//Crafting Upgrades
	public static final int ADVANCED_SAT_CRAFTINGPIPE = 21;
	public static final int LIQUID_CRAFTING = 22;
	public static final int CRAFTING_BYPRODUCT_EXTRACTOR = 23;
	public static final int SUPPLIER_PATTERN = 24;
	public static final int FUZZY_CRAFTING = 25;
	public static final int CRAFTING_CLEANUP = 26;

	//Power Upgrades
	public static final int POWER_TRANSPORTATION = 30;
	public static final int POWER_BC_SUPPLIER = 31;
	public static final int POWER_RF_SUPPLIER = 32;
	public static final int POWER_IC2_LV_SUPPLIER = 33;
	public static final int POWER_IC2_MV_SUPPLIER = 34;
	public static final int POWER_IC2_HV_SUPPLIER = 35;
	public static final int POWER_IC2_EV_SUPPLIER = 36;

	//Various
	public static final int CC_REMOTE_CONTROL = 40;
	public static final int CRAFTING_MONITORING = 41;
	public static final int OPAQUE_UPGRADE = 42;
	public static final int LOGIC_CONTROLLER_UPGRADE = 43;
	public static final int UPGRADE_MODULE_UPGRADE = 44;

	//Values
	public static final int MAX_LIQUID_CRAFTER = 3;
	public static final int MAX_CRAFTING_CLEANUP = 4;

	List<Upgrade> upgrades = new ArrayList<>();
	private TextureAtlasSprite[] icons;

	private class Upgrade {

		private int id;
		private Class<? extends IPipeUpgrade> upgradeClass;
		private String texturePath;
		private boolean deprecated = false;

		private Upgrade(int id, Class<? extends IPipeUpgrade> moduleClass, String texturePath) {
			this.id = id;
			upgradeClass = moduleClass;
			this.texturePath = texturePath;
		}

		private Upgrade(int id, Class<? extends IPipeUpgrade> moduleClass, String texturePath, boolean deprecated) {
			this.id = id;
			upgradeClass = moduleClass;
			this.texturePath = texturePath;
			this.deprecated = deprecated;
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

		private int getId() {
			return id;
		}

		@SideOnly(Side.CLIENT)
		private void registerUpgradeModel(Item item) {
			if (texturePath == null) {
				ModelLoader.setCustomModelResourceLocation(item, getId(), new ModelResourceLocation("logisticspipes:" + getUnlocalizedName().replace("item.", "") + "/blank", "inventory"));
			} else {
				ModelLoader.setCustomModelResourceLocation(item, getId(), new ModelResourceLocation("logisticspipes:" + texturePath, "inventory"));
			}
		}
	}

	public ItemUpgrade() {
		hasSubtypes = true;
	}

	public void loadUpgrades() {
		registerUpgrade(ItemUpgrade.SNEAKY_UP, SneakyUpgradeUP.class, "itemupgrade/sneakyup", true);
		registerUpgrade(ItemUpgrade.SNEAKY_DOWN, SneakyUpgradeDOWN.class, "itemupgrade/sneakydown", true);
		registerUpgrade(ItemUpgrade.SNEAKY_NORTH, SneakyUpgradeNORTH.class, "itemupgrade/sneakynorth", true);
		registerUpgrade(ItemUpgrade.SNEAKY_SOUTH, SneakyUpgradeSOUTH.class, "itemupgrade/sneakysouth", true);
		registerUpgrade(ItemUpgrade.SNEAKY_EAST, SneakyUpgradeEAST.class, "itemupgrade/sneakyeast", true);
		registerUpgrade(ItemUpgrade.SNEAKY_WEST, SneakyUpgradeWEST.class, "itemupgrade/sneakywest", true);
		registerUpgrade(ItemUpgrade.SNEAKY_COMBINATION, CombinedSneakyUpgrade.class, "itemupgrade/sneakycombination");
		registerUpgrade(ItemUpgrade.SNEAKY, SneakyUpgradeConfig.class, "itemupgrade/sneaky");
		registerUpgrade(ItemUpgrade.SPEED, SpeedUpgrade.class, "itemupgrade/speed");
		registerUpgrade(ItemUpgrade.CONNECTION_UP, ConnectionUpgradeUP.class, "itemupgrade/disup", true);
		registerUpgrade(ItemUpgrade.CONNECTION_DOWN, ConnectionUpgradeDOWN.class, "itemupgrade/disdown", true);
		registerUpgrade(ItemUpgrade.CONNECTION_NORTH, ConnectionUpgradeNORTH.class, "itemupgrade/disnorth", true);
		registerUpgrade(ItemUpgrade.CONNECTION_SOUTH, ConnectionUpgradeSOUTH.class, "itemupgrade/dissouth", true);
		registerUpgrade(ItemUpgrade.CONNECTION_EAST, ConnectionUpgradeEAST.class, "itemupgrade/diseast", true);
		registerUpgrade(ItemUpgrade.CONNECTION_WEST, ConnectionUpgradeWEST.class, "itemupgrade/diswest", true);
		registerUpgrade(ItemUpgrade.CONNECTION, ConnectionUpgradeConfig.class, "itemupgrade/dis");

		registerUpgrade(ItemUpgrade.ADVANCED_SAT_CRAFTINGPIPE, AdvancedSatelliteUpgrade.class, "itemupgrade/satellite");
		registerUpgrade(ItemUpgrade.LIQUID_CRAFTING, FluidCraftingUpgrade.class, "itemupgrade/fluidcrafting");
		registerUpgrade(ItemUpgrade.CRAFTING_BYPRODUCT_EXTRACTOR, CraftingByproductUpgrade.class, "itemupgrade/craftingbyproduct");
		registerUpgrade(ItemUpgrade.SUPPLIER_PATTERN, PatternUpgrade.class, "itemupgrade/placementrules");
		registerUpgrade(ItemUpgrade.FUZZY_CRAFTING, FuzzyUpgrade.class, "itemupgrade/fuzzycrafting");
		registerUpgrade(ItemUpgrade.POWER_TRANSPORTATION, PowerTransportationUpgrade.class, "itemupgrade/powertransport");
		//registerUpgrade(ItemUpgrade.POWER_BC_SUPPLIER, BCPowerSupplierUpgrade.class, "itemupgrade/powertransportbc");
		registerUpgrade(ItemUpgrade.POWER_RF_SUPPLIER, RFPowerSupplierUpgrade.class, "itemupgrade/powertransportte");
		registerUpgrade(ItemUpgrade.POWER_IC2_LV_SUPPLIER, IC2LVPowerSupplierUpgrade.class, "itemupgrade/powertransportic2-lv");
		registerUpgrade(ItemUpgrade.POWER_IC2_MV_SUPPLIER, IC2MVPowerSupplierUpgrade.class, "itemupgrade/powertransportic2-mv");
		registerUpgrade(ItemUpgrade.POWER_IC2_HV_SUPPLIER, IC2HVPowerSupplierUpgrade.class, "itemupgrade/powertransportic2-hv");
		registerUpgrade(ItemUpgrade.POWER_IC2_EV_SUPPLIER, IC2EVPowerSupplierUpgrade.class, "itemupgrade/powertransportic2-ev");
		registerUpgrade(ItemUpgrade.CC_REMOTE_CONTROL, CCRemoteControlUpgrade.class, "itemupgrade/ccremotecontrol");
		registerUpgrade(ItemUpgrade.CRAFTING_MONITORING, CraftingMonitoringUpgrade.class, "itemupgrade/craftingmonitoring");
		registerUpgrade(ItemUpgrade.OPAQUE_UPGRADE, OpaqueUpgrade.class, "itemupgrade/opaqueupgrade");
		registerUpgrade(ItemUpgrade.CRAFTING_CLEANUP, CraftingCleanupUpgrade.class, "itemupgrade/craftingcleanup");
		if (LPConstants.DEBUG) {
			registerUpgrade(ItemUpgrade.LOGIC_CONTROLLER_UPGRADE, LogicControllerUpgrade.class, "itemupgrade/logiccontroller");
		}
		registerUpgrade(ItemUpgrade.UPGRADE_MODULE_UPGRADE, UpgradeModuleUpgrade.class, "itemupgrade/upgrademodule");
	}

	public void registerUpgrade(int id, Class<? extends IPipeUpgrade> moduleClass, String texturePath) {
		boolean flag = true;
		for (Upgrade upgrade : upgrades) {
			if (upgrade.getId() == id) {
				flag = false;
			}
		}
		if (flag) {
			upgrades.add(new Upgrade(id, moduleClass, texturePath));
		} else if (!flag) {
			throw new UnsupportedOperationException("Someting went wrong while registering a new Logistics Pipe Upgrade. (Id " + id + " already in use)");
		} else {
			throw new UnsupportedOperationException("Someting went wrong while registering a new Logistics Pipe Upgrade. (No name given)");
		}
	}

	public void registerUpgrade(int id, Class<? extends IPipeUpgrade> moduleClass, String texturePath, boolean deprecated) {
		boolean flag = true;
		for (Upgrade upgrade : upgrades) {
			if (upgrade.getId() == id) {
				flag = false;
			}
		}
		if (flag) {
			upgrades.add(new Upgrade(id, moduleClass, texturePath, deprecated));
		} else if (!flag) {
			throw new UnsupportedOperationException("Someting went wrong while registering a new Logistics Pipe Upgrade. (Id " + id + " already in use)");
		} else {
			throw new UnsupportedOperationException("Someting went wrong while registering a new Logistics Pipe Upgrade. (No name given)");
		}
	}

	public int[] getRegisteredUpgradeIDs() {
		int[] array = new int[upgrades.size()];
		int i = 0;
		for (Upgrade upgrade : upgrades) {
			array[i++] = upgrade.getId();
		}
		return array;
	}

	@Override
	public CreativeTabs getCreativeTab() {
		return CreativeTabs.REDSTONE;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void getSubItems(CreativeTabs par2CreativeTabs, NonNullList par3List) {
		par3List.addAll(upgrades.stream().filter(upgrade -> !upgrade.deprecated)
				.map(upgrade -> new ItemStack(this, 1, upgrade.getId()))
				.collect(Collectors.toList()));
	}

	public IPipeUpgrade getUpgradeForItem(ItemStack itemStack, IPipeUpgrade currentUpgrade) {
		if (itemStack == null) {
			return null;
		}
		if (itemStack.getItem() != this) {
			return null;
		}
		for (Upgrade upgrade : upgrades) {
			if (itemStack.getItemDamage() == upgrade.getId()) {
				if (upgrade.getIPipeUpgradeClass() == null) {
					return null;
				}
				if (currentUpgrade != null) {
					if (upgrade.getIPipeUpgradeClass().equals(currentUpgrade.getClass())) {
						return currentUpgrade;
					}
				}
				IPipeUpgrade newupgrade = upgrade.getIPipeUpgrade();
				if (newupgrade == null) {
					return null;
				}
				return newupgrade;
			}
		}
		return null;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		for (Upgrade upgrade : upgrades) {
			if (itemstack.getItemDamage() == upgrade.getId()) {
				return "item." + upgrade.getIPipeUpgradeClass().getSimpleName();
			}
		}
		return null;
	}

	@Override
	public String getItemStackDisplayName(ItemStack itemstack) {
		for (Upgrade upgrade : upgrades) {
			if (itemstack.getItemDamage() == upgrade.getId()) {
				return StringUtils.translate(getUnlocalizedName(itemstack)) + (upgrade.deprecated ? " (Deprecated)" : "");
			}
		}
		return StringUtils.translate(getUnlocalizedName(itemstack));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModels() {
		if (upgrades.size() <= 0) {
			loadUpgrades();
		}
		for (Upgrade module : upgrades) {
			module.registerUpgradeModel(this);
		}
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
