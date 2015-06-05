package logisticspipes.items;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import logisticspipes.LPConstants;
import logisticspipes.pipes.upgrades.AdvancedSatelliteUpgrade;
import logisticspipes.pipes.upgrades.CCRemoteControlUpgrade;
import logisticspipes.pipes.upgrades.CombinedSneakyUpgrade;
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

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;

public class ItemUpgrade extends LogisticsItem {

	//Sneaky Upgrades
	public static final int SNEAKY_UP = 0;
	public static final int SNEAKY_DOWN = 1;
	public static final int SNEAKY_NORTH = 2;
	public static final int SNEAKY_SOUTH = 3;
	public static final int SNEAKY_EAST = 4;
	public static final int SNEAKY_WEST = 5;
	public static final int SNEAKY_COMBINATION = 6;

	//Connection Upgrades
	public static final int CONNECTION_UP = 10;
	public static final int CONNECTION_DOWN = 11;
	public static final int CONNECTION_NORTH = 12;
	public static final int CONNECTION_SOUTH = 13;
	public static final int CONNECTION_EAST = 14;
	public static final int CONNECTION_WEST = 15;

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

	List<Upgrade> upgrades = new ArrayList<Upgrade>();
	private IIcon[] icons;

	private class Upgrade {

		private int id;
		private Class<? extends IPipeUpgrade> upgradeClass;
		private int textureIndex = -1;

		private Upgrade(int id, Class<? extends IPipeUpgrade> moduleClass, int textureIndex) {
			this.id = id;
			upgradeClass = moduleClass;
			this.textureIndex = textureIndex;
		}

		private IPipeUpgrade getIPipeUpgrade() {
			if (upgradeClass == null) {
				return null;
			}
			try {
				return upgradeClass.getConstructor(new Class[] {}).newInstance(new Object[] {});
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

		private Class<? extends IPipeUpgrade> getIPipeUpgradeClass() {
			return upgradeClass;
		}

		private int getId() {
			return id;
		}

		private int getTextureIndex() {
			return textureIndex;
		}
	}

	public ItemUpgrade() {
		hasSubtypes = true;
	}

	public void loadUpgrades() {
		registerUpgrade(ItemUpgrade.SNEAKY_UP, SneakyUpgradeUP.class, 0);
		registerUpgrade(ItemUpgrade.SNEAKY_DOWN, SneakyUpgradeDOWN.class, 1);
		registerUpgrade(ItemUpgrade.SNEAKY_NORTH, SneakyUpgradeNORTH.class, 2);
		registerUpgrade(ItemUpgrade.SNEAKY_SOUTH, SneakyUpgradeSOUTH.class, 3);
		registerUpgrade(ItemUpgrade.SNEAKY_EAST, SneakyUpgradeEAST.class, 4);
		registerUpgrade(ItemUpgrade.SNEAKY_WEST, SneakyUpgradeWEST.class, 5);
		registerUpgrade(ItemUpgrade.SNEAKY_COMBINATION, CombinedSneakyUpgrade.class, 6);
		registerUpgrade(ItemUpgrade.SPEED, SpeedUpgrade.class, 7);
		registerUpgrade(ItemUpgrade.CONNECTION_UP, ConnectionUpgradeUP.class, 8);
		registerUpgrade(ItemUpgrade.CONNECTION_DOWN, ConnectionUpgradeDOWN.class, 9);
		registerUpgrade(ItemUpgrade.CONNECTION_NORTH, ConnectionUpgradeNORTH.class, 10);
		registerUpgrade(ItemUpgrade.CONNECTION_SOUTH, ConnectionUpgradeSOUTH.class, 11);
		registerUpgrade(ItemUpgrade.CONNECTION_EAST, ConnectionUpgradeEAST.class, 12);
		registerUpgrade(ItemUpgrade.CONNECTION_WEST, ConnectionUpgradeWEST.class, 13);

		registerUpgrade(ItemUpgrade.ADVANCED_SAT_CRAFTINGPIPE, AdvancedSatelliteUpgrade.class, 14);
		registerUpgrade(ItemUpgrade.LIQUID_CRAFTING, FluidCraftingUpgrade.class, 15);
		registerUpgrade(ItemUpgrade.CRAFTING_BYPRODUCT_EXTRACTOR, CraftingByproductUpgrade.class, 16);
		registerUpgrade(ItemUpgrade.SUPPLIER_PATTERN, PatternUpgrade.class, 17);
		registerUpgrade(ItemUpgrade.FUZZY_CRAFTING, FuzzyUpgrade.class, 18);
		registerUpgrade(ItemUpgrade.POWER_TRANSPORTATION, PowerTransportationUpgrade.class, 19);
		registerUpgrade(ItemUpgrade.POWER_RF_SUPPLIER, RFPowerSupplierUpgrade.class, 21);
		registerUpgrade(ItemUpgrade.POWER_IC2_LV_SUPPLIER, IC2LVPowerSupplierUpgrade.class, 22);
		registerUpgrade(ItemUpgrade.POWER_IC2_MV_SUPPLIER, IC2MVPowerSupplierUpgrade.class, 23);
		registerUpgrade(ItemUpgrade.POWER_IC2_HV_SUPPLIER, IC2HVPowerSupplierUpgrade.class, 24);
		registerUpgrade(ItemUpgrade.POWER_IC2_EV_SUPPLIER, IC2EVPowerSupplierUpgrade.class, 25);
		registerUpgrade(ItemUpgrade.CC_REMOTE_CONTROL, CCRemoteControlUpgrade.class, 26);
		registerUpgrade(ItemUpgrade.CRAFTING_MONITORING, CraftingMonitoringUpgrade.class, 27);
		registerUpgrade(ItemUpgrade.OPAQUE_UPGRADE, OpaqueUpgrade.class, 28);
		registerUpgrade(ItemUpgrade.CRAFTING_CLEANUP, CraftingCleanupUpgrade.class, 29);
		if (LPConstants.DEBUG) {
			registerUpgrade(ItemUpgrade.LOGIC_CONTROLLER_UPGRADE, LogicControllerUpgrade.class, 30);
		}
		registerUpgrade(ItemUpgrade.UPGRADE_MODULE_UPGRADE, UpgradeModuleUpgrade.class, 31);
	}

	public void registerUpgrade(int id, Class<? extends IPipeUpgrade> moduleClass, int textureId) {
		boolean flag = true;
		for (Upgrade upgrade : upgrades) {
			if (upgrade.getId() == id) {
				flag = false;
			}
		}
		if (flag) {
			upgrades.add(new Upgrade(id, moduleClass, textureId));
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
		return CreativeTabs.tabRedstone;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List par3List) {
		for (Upgrade upgrade : upgrades) {
			par3List.add(new ItemStack(this, 1, upgrade.getId()));
		}
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
		return StringUtils.translate(getUnlocalizedName(itemstack));
	}

	@Override
	public void registerIcons(IIconRegister par1IIconRegister) {
		icons = new IIcon[32];
		icons[0] = par1IIconRegister.registerIcon("logisticspipes:itemUpgrade/SneakyUP");
		icons[1] = par1IIconRegister.registerIcon("logisticspipes:itemUpgrade/SneakyDOWN");
		icons[2] = par1IIconRegister.registerIcon("logisticspipes:itemUpgrade/SneakyNORTH");
		icons[3] = par1IIconRegister.registerIcon("logisticspipes:itemUpgrade/SneakySOUTH");
		icons[4] = par1IIconRegister.registerIcon("logisticspipes:itemUpgrade/SneakyEAST");
		icons[5] = par1IIconRegister.registerIcon("logisticspipes:itemUpgrade/SneakyWEST");
		icons[6] = par1IIconRegister.registerIcon("logisticspipes:itemUpgrade/SneakyCombination");

		icons[7] = par1IIconRegister.registerIcon("logisticspipes:itemUpgrade/Speed");

		icons[8] = par1IIconRegister.registerIcon("logisticspipes:itemUpgrade/DisUP");
		icons[9] = par1IIconRegister.registerIcon("logisticspipes:itemUpgrade/DisDOWN");
		icons[10] = par1IIconRegister.registerIcon("logisticspipes:itemUpgrade/DisNORTH");
		icons[11] = par1IIconRegister.registerIcon("logisticspipes:itemUpgrade/DisSOUTH");
		icons[12] = par1IIconRegister.registerIcon("logisticspipes:itemUpgrade/DisEAST");
		icons[13] = par1IIconRegister.registerIcon("logisticspipes:itemUpgrade/DisWEST");

		icons[14] = par1IIconRegister.registerIcon("logisticspipes:itemUpgrade/Satelite");
		icons[15] = par1IIconRegister.registerIcon("logisticspipes:itemUpgrade/FluidCrafting");
		icons[16] = par1IIconRegister.registerIcon("logisticspipes:itemUpgrade/CraftingByproduct");
		icons[17] = par1IIconRegister.registerIcon("logisticspipes:itemUpgrade/PlacementRules");
		icons[18] = par1IIconRegister.registerIcon("logisticspipes:itemUpgrade/FuzzyCrafting");
		icons[19] = par1IIconRegister.registerIcon("logisticspipes:itemUpgrade/PowerTransport");
		icons[20] = par1IIconRegister.registerIcon("logisticspipes:itemUpgrade/PowerTransportBC");
		icons[21] = par1IIconRegister.registerIcon("logisticspipes:itemUpgrade/PowerTransportTE");
		icons[22] = par1IIconRegister.registerIcon("logisticspipes:itemUpgrade/PowerTransportIC2-LV");
		icons[23] = par1IIconRegister.registerIcon("logisticspipes:itemUpgrade/PowerTransportIC2-MV");
		icons[24] = par1IIconRegister.registerIcon("logisticspipes:itemUpgrade/PowerTransportIC2-HV");
		icons[25] = par1IIconRegister.registerIcon("logisticspipes:itemUpgrade/PowerTransportIC2-EV");
		icons[26] = par1IIconRegister.registerIcon("logisticspipes:itemUpgrade/CCRemoteControl");
		icons[27] = par1IIconRegister.registerIcon("logisticspipes:itemUpgrade/CraftingMonitoring");
		icons[28] = par1IIconRegister.registerIcon("logisticspipes:itemUpgrade/OpaqueUpgrade");
		icons[29] = par1IIconRegister.registerIcon("logisticspipes:itemUpgrade/CraftingCleanup");
		icons[30] = par1IIconRegister.registerIcon("logisticspipes:itemUpgrade/LogicController");
		icons[31] = par1IIconRegister.registerIcon("logisticspipes:itemUpgrade/UpgradeModule");
	}

	@Override
	public IIcon getIconFromDamage(int i) {

		for (Upgrade upgrade : upgrades) {
			if (upgrade.getId() == i) {
				if (upgrade.getTextureIndex() != -1) {
					return icons[upgrade.getTextureIndex()];
				}
			}
		}
		return icons[0];
	}

	public static String SHIFT_INFO_PREFIX = "item.upgrade.info.";

	@Override
	@SideOnly(Side.CLIENT)
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void addInformation(ItemStack stack, EntityPlayer par2EntityPlayer, List list, boolean flag) {
		super.addInformation(stack, par2EntityPlayer, list, flag);
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
				list.add(MessageFormat.format(base1, join(pipe)));
				list.add(MessageFormat.format(base2, join(module)));
			} else if (!pipe.isEmpty()) {
				//Can be applied to {0} pipes
				String base = StringUtils.translate(ItemUpgrade.SHIFT_INFO_PREFIX + "pipe");
				list.add(MessageFormat.format(base, join(pipe)));
			} else if (!module.isEmpty()) {
				//Can be applied to {0} modules
				String base = StringUtils.translate(ItemUpgrade.SHIFT_INFO_PREFIX + "module");
				list.add(MessageFormat.format(base, join(module)));
			}
		} else {
			String baseKey = MessageFormat.format("{0}.tip", stack.getItem().getUnlocalizedName(stack));
			String key = baseKey + 1;
			String translation = StringUtils.translate(key);
			if (translation.equals(key)) {
				list.add(StringUtils.translate(StringUtils.KEY_HOLDSHIFT));
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
