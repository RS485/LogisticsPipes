package logisticspipes.items;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import logisticspipes.pipes.upgrades.AdvancedSatelliteUpgrade;
import logisticspipes.pipes.upgrades.CombinedSneakyUpgrade;
import logisticspipes.pipes.upgrades.CraftingByproductUpgrade;
import logisticspipes.pipes.upgrades.IPipeUpgrade;
import logisticspipes.pipes.upgrades.FluidCraftingUpgrade;
import logisticspipes.pipes.upgrades.SpeedUpgrade;
import logisticspipes.pipes.upgrades.connection.ConnectionUpgradeDOWN;
import logisticspipes.pipes.upgrades.connection.ConnectionUpgradeEAST;
import logisticspipes.pipes.upgrades.connection.ConnectionUpgradeNORTH;
import logisticspipes.pipes.upgrades.connection.ConnectionUpgradeSOUTH;
import logisticspipes.pipes.upgrades.connection.ConnectionUpgradeUP;
import logisticspipes.pipes.upgrades.connection.ConnectionUpgradeWEST;
import logisticspipes.pipes.upgrades.sneaky.SneakyUpgradeDOWN;
import logisticspipes.pipes.upgrades.sneaky.SneakyUpgradeEAST;
import logisticspipes.pipes.upgrades.sneaky.SneakyUpgradeNORTH;
import logisticspipes.pipes.upgrades.sneaky.SneakyUpgradeSOUTH;
import logisticspipes.pipes.upgrades.sneaky.SneakyUpgradeUP;
import logisticspipes.pipes.upgrades.sneaky.SneakyUpgradeWEST;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;

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
	
	//Values
	public static final int MAX_LIQUID_CRAFTER = 3;

	List<Upgrade> upgrades = new ArrayList<Upgrade>();
	private static Icon[] icons;
	private class Upgrade {
		private String name;
		private int id;
		private Class<? extends IPipeUpgrade> upgradeClass;
		private int textureIndex = -1;

		private Upgrade(int id, String name, Class<? extends IPipeUpgrade> moduleClass) {
			this.id = id;
			this.name = name;
			this.upgradeClass = moduleClass;
		}

		private Upgrade(int id, String name, Class<? extends IPipeUpgrade> moduleClass, int textureIndex) {
			this.id = id;
			this.name = name;
			this.upgradeClass = moduleClass;
			this.textureIndex = textureIndex;
		}
		
		private IPipeUpgrade getIPipeUpgrade() {
			if(upgradeClass == null) return null;
			try {
				return upgradeClass.getConstructor(new Class[]{}).newInstance(new Object[]{});
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
		
		private String getName() {
			return name;
		}
		
		private int getTextureIndex() {
			return textureIndex;
		}
	}
	
	public ItemUpgrade(int i) {
		super(i);
		this.hasSubtypes = true;
	}
	
	public void loadUpgrades() {
		registerUpgrade(SNEAKY_UP, "Sneaky Upgrade (UP)", SneakyUpgradeUP.class,0);
		registerUpgrade(SNEAKY_DOWN, "Sneaky Upgrade (DOWN)", SneakyUpgradeDOWN.class,1);
		registerUpgrade(SNEAKY_NORTH, "Sneaky Upgrade (NORTH)", SneakyUpgradeNORTH.class,2);
		registerUpgrade(SNEAKY_SOUTH, "Sneaky Upgrade (SOUTH)", SneakyUpgradeSOUTH.class,3);
		registerUpgrade(SNEAKY_EAST, "Sneaky Upgrade (EAST)", SneakyUpgradeEAST.class,4);
		registerUpgrade(SNEAKY_WEST, "Sneaky Upgrade (WEST)", SneakyUpgradeWEST.class,5);
		registerUpgrade(SNEAKY_COMBINATION, "Sneaky Combination Upgrade", CombinedSneakyUpgrade.class,6);
		registerUpgrade(SPEED, "Item Speed Upgrade", SpeedUpgrade.class, 7);
		registerUpgrade(CONNECTION_UP, "Disconnection Upgrade (UP)", ConnectionUpgradeUP.class, 8);
		registerUpgrade(CONNECTION_DOWN, "Disconnection Upgrade (DOWN)", ConnectionUpgradeDOWN.class, 9);
		registerUpgrade(CONNECTION_NORTH, "Disconnection Upgrade (NORTH)", ConnectionUpgradeNORTH.class, 10);
		registerUpgrade(CONNECTION_SOUTH, "Disconnection Upgrade (SOUTH)", ConnectionUpgradeSOUTH.class, 11);
		registerUpgrade(CONNECTION_EAST, "Disconnection Upgrade (EAST)", ConnectionUpgradeEAST.class, 12);
		registerUpgrade(CONNECTION_WEST, "Disconnection Upgrade (WEST)", ConnectionUpgradeWEST.class, 13);

		registerUpgrade(ADVANCED_SAT_CRAFTINGPIPE, "Advanced Satellite Upgrade", AdvancedSatelliteUpgrade.class, 14);
		registerUpgrade(LIQUID_CRAFTING, "Fluid Crafting Upgrade", FluidCraftingUpgrade.class, 15);
		registerUpgrade(CRAFTING_BYPRODUCT_EXTRACTOR, "Crafting Byproduct Extraction Upgrade", CraftingByproductUpgrade.class, 16);
	}
	
	public void registerUpgrade(int id, String name, Class<? extends IPipeUpgrade> moduleClass) {
		boolean flag = true;
		for(Upgrade upgrade:upgrades) {
			if(upgrade.getId() == id) {
				flag = false;
			}
		}
		if(!"".equals(name) && flag) {
			upgrades.add(new Upgrade(id,name,moduleClass));
		} else if(!flag) {
			throw new UnsupportedOperationException("Someting went wrong while registering a new Logistics Pipe Upgrade. (Id " + id + " already in use)");
		} else {
			throw new UnsupportedOperationException("Someting went wrong while registering a new Logistics Pipe Upgrade. (No name given)");
		}
	}
	
	public void registerUpgrade(int id, String name, Class<? extends IPipeUpgrade> moduleClass, int textureId) {
		boolean flag = true;
		for(Upgrade upgrade:upgrades) {
			if(upgrade.getId() == id) {
				flag = false;
			}
		}
		if(!"".equals(name) && flag) {
			upgrades.add(new Upgrade(id,name,moduleClass,textureId));
		} else if(!flag) {
			throw new UnsupportedOperationException("Someting went wrong while registering a new Logistics Pipe Upgrade. (Id " + id + " already in use)");
		} else {
			throw new UnsupportedOperationException("Someting went wrong while registering a new Logistics Pipe Upgrade. (No name given)");
		}
	}
	
	public int[] getRegisteredUpgradeIDs() {
		int[] array = new int[upgrades.size()];
		int i = 0;
		for(Upgrade upgrade:upgrades) {
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
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List) {
		for(Upgrade upgrade:upgrades) {
			par3List.add(new ItemStack(this, 1, upgrade.getId()));
		}
    }
	
	public IPipeUpgrade getUpgradeForItem(ItemStack itemStack, IPipeUpgrade currentUpgrade){
		if (itemStack == null) return null;
		if (itemStack.itemID != this.itemID) return null;
		for(Upgrade upgrade:upgrades) {
			if(itemStack.getItemDamage() == upgrade.getId()) {
				if(upgrade.getIPipeUpgradeClass() == null) return null;
				if(currentUpgrade != null) {
					if (upgrade.getIPipeUpgradeClass().equals(currentUpgrade.getClass())) return currentUpgrade;
				}
				IPipeUpgrade newupgrade = upgrade.getIPipeUpgrade();
				if(newupgrade == null) return null;
				return newupgrade;
			}
		}
		return null;
	}

	@Override
	public String getItemDisplayName(ItemStack itemstack) {
		for(Upgrade upgrade:upgrades) {
			if(itemstack.getItemDamage() == upgrade.getId()) {
				return upgrade.getName();
			}
		}
		return null;
	}
	@Override
	public void registerIcons(IconRegister par1IconRegister)
	{
		icons=new Icon[18];
		icons[0]=par1IconRegister.registerIcon("logisticspipes:itemUpgrade/SneakyUP");
		icons[1]=par1IconRegister.registerIcon("logisticspipes:itemUpgrade/SneakyDOWN");
		icons[2]=par1IconRegister.registerIcon("logisticspipes:itemUpgrade/SneakyNORTH");
		icons[3]=par1IconRegister.registerIcon("logisticspipes:itemUpgrade/SneakySOUTH");
		icons[4]=par1IconRegister.registerIcon("logisticspipes:itemUpgrade/SneakyEAST");
		icons[5]=par1IconRegister.registerIcon("logisticspipes:itemUpgrade/SneakyWEST");
		icons[6]=par1IconRegister.registerIcon("logisticspipes:itemUpgrade/SneakyCombination");
		
		icons[7]=par1IconRegister.registerIcon("logisticspipes:itemUpgrade/Speed");
		
		icons[8]=par1IconRegister.registerIcon("logisticspipes:itemUpgrade/DisUP");
		icons[9]=par1IconRegister.registerIcon("logisticspipes:itemUpgrade/DisDOWN");
		icons[10]=par1IconRegister.registerIcon("logisticspipes:itemUpgrade/DisNORTH");
		icons[11]=par1IconRegister.registerIcon("logisticspipes:itemUpgrade/DisSOUTH");
		icons[12]=par1IconRegister.registerIcon("logisticspipes:itemUpgrade/DisEAST");
		icons[13]=par1IconRegister.registerIcon("logisticspipes:itemUpgrade/DisWEST");

		icons[14]=par1IconRegister.registerIcon("logisticspipes:itemUpgrade/Satelite");
		icons[15]=par1IconRegister.registerIcon("logisticspipes:itemUpgrade/FluidCrafting");
		icons[16]=par1IconRegister.registerIcon("logisticspipes:itemUpgrade/CraftingByproduct");
		icons[17]=par1IconRegister.registerIcon("logisticspipes:itemUpgrade/UNKNOWN01");
	}
	@Override
	public Icon getIconFromDamage(int i) {

		for(Upgrade upgrade:upgrades) {
			if(upgrade.getId() == i) {
				if(upgrade.getTextureIndex() != -1) {
					return icons[upgrade.getTextureIndex()];
				}
			}
		}
			
		return icons[0];
	}
}
