package logisticspipes.items;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import logisticspipes.pipes.upgrades.IPipeUpgrade;
import logisticspipes.pipes.upgrades.SpeedUpgrade;
import logisticspipes.pipes.upgrades.sneaky.SneakyUpgradeDOWN;
import logisticspipes.pipes.upgrades.sneaky.SneakyUpgradeEAST;
import logisticspipes.pipes.upgrades.sneaky.SneakyUpgradeNORTH;
import logisticspipes.pipes.upgrades.sneaky.SneakyUpgradeSOUTH;
import logisticspipes.pipes.upgrades.sneaky.SneakyUpgradeUP;
import logisticspipes.pipes.upgrades.sneaky.SneakyUpgradeWEST;
import net.minecraft.src.CreativeTabs;
import net.minecraft.src.ItemStack;

public class ItemUpgrade extends LogisticsItem {
	
	//Sneaky Upgrades
	public static final int SNEAKY_UP = 0;
	public static final int SNEAKY_DOWN = 1;
	public static final int SNEAKY_NORTH = 2;
	public static final int SNEAKY_SOUTH = 3;
	public static final int SNEAKY_EAST = 4;
	public static final int SNEAKY_WEST = 5;
	
	
	public static final int SPEED = 20;

	List<Upgrade> upgrades = new ArrayList<Upgrade>();
	
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
				return (IPipeUpgrade)upgradeClass.getConstructor(new Class[]{}).newInstance(new Object[]{});
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
		registerUpgrade(SNEAKY_UP, "Sneaky Upgrade (UP)", SneakyUpgradeUP.class);
		registerUpgrade(SNEAKY_DOWN, "Sneaky Upgrade (DOWN)", SneakyUpgradeDOWN.class);
		registerUpgrade(SNEAKY_NORTH, "Sneaky Upgrade (NORTH)", SneakyUpgradeNORTH.class);
		registerUpgrade(SNEAKY_SOUTH, "Sneaky Upgrade (SOUTH)", SneakyUpgradeSOUTH.class);
		registerUpgrade(SNEAKY_EAST, "Sneaky Upgrade (EAST)", SneakyUpgradeEAST.class);
		registerUpgrade(SNEAKY_WEST, "Sneaky Upgrade (WEST)", SneakyUpgradeWEST.class);
		registerUpgrade(SPEED, "Item Speed Upgrade", SpeedUpgrade.class, 9 * 16 + 6);
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
		if (itemStack.itemID != this.shiftedIndex) return null;
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
	public int getIconFromDamage(int i) {
		for(Upgrade upgrade:upgrades) {
			if(upgrade.getId() == i) {
				if(upgrade.getTextureIndex() != -1) {
					return upgrade.getTextureIndex();
				}
			}
		}
			
		return 9 * 16 + i;
	}
}
