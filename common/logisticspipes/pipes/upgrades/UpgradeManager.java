package logisticspipes.pipes.upgrades;

import logisticspipes.LogisticsPipes;
import logisticspipes.utils.ISimpleInventoryEventHandler;
import logisticspipes.utils.SimpleInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;

public class UpgradeManager implements ISimpleInventoryEventHandler {
	
	private SimpleInventory inv = new SimpleInventory(9, "UpgradeInventory", 1);
	private IPipeUpgrade[] upgrades = new IPipeUpgrade[9];
	
	public UpgradeManager() {
		inv.addListener(this);
	}
	
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		inv.readFromNBT(nbttagcompound, "UpgradeInventory_");
		InventoryChanged(inv);
	}
	
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		inv.writeToNBT(nbttagcompound, "UpgradeInventory_");
		InventoryChanged(inv);
	}

	private void updateModule(int slot) {
		upgrades[slot] = LogisticsPipes.UpgradeItem.getUpgradeForItem(inv.getStackInSlot(slot), upgrades[slot]);
	}
	
	private void removeUpgrade(int slot) {
		upgrades[slot] = null;
	}
	
	@Override
	public void InventoryChanged(SimpleInventory inventory) {
		for(int i=0;i<inv.getSizeInventory();i++) {
			ItemStack item = inv.getStackInSlot(i);
			if(item != null) {
				updateModule(i);
			} else if(item == null && upgrades[i] != null) {
				removeUpgrade(i);
			}
		}
	}

	/* Special implementations */
	
	public boolean hasSneakyUpgrade() {
		return getSneakyUpgrade() != null;
	}

	public SneakyUpgrade getSneakyUpgrade() {
		for(int i=0;i<upgrades.length;i++) {
			IPipeUpgrade update = upgrades[i];
			if(update instanceof SneakyUpgrade) {
				return (SneakyUpgrade) update;
			}
		}
		return null;
	}
	
	public int getSpeedUpgradeCount() {
		int count = 0;
		for(int i=0;i<upgrades.length;i++) {
			IPipeUpgrade update = upgrades[i];
			if(update instanceof SpeedUpgrade) {
				count++;
			}
		}
		return count;
	}
}
