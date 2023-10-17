package logisticspipes.utils.gui;

import net.minecraft.inventory.IInventory;

import lombok.Getter;

import logisticspipes.interfaces.ISlotCheck;
import logisticspipes.interfaces.ISlotUpgradeManager;
import logisticspipes.pipes.upgrades.IPipeUpgrade;

public class UpgradeSlot extends RestrictedSlot {

	@Getter
	protected final ISlotUpgradeManager manager;
	protected final int upgradeSlotId;

	protected UpgradeSlot(IInventory iinventory, ISlotUpgradeManager manager, int upgradeSlotId, int i, int j, int k, ISlotCheck slotCheck) {
		super(iinventory, i, j, k, slotCheck);
		this.manager = manager;
		this.upgradeSlotId = upgradeSlotId;
	}

	public UpgradeSlot(ISlotUpgradeManager manager, int upgradeSlotId, int i, int j, int k, ISlotCheck check) {
		super(manager.getInv(), i, j, k, check);
		this.manager = manager;
		this.upgradeSlotId = upgradeSlotId;
	}

	public IPipeUpgrade getUpgrade() {
		return manager.getUpgrade(upgradeSlotId);
	}
}
