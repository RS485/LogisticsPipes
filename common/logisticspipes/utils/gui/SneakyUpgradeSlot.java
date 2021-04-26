package logisticspipes.utils.gui;

import logisticspipes.interfaces.ISlotCheck;
import logisticspipes.pipes.upgrades.UpgradeManager;

public class SneakyUpgradeSlot extends UpgradeSlot {

	protected SneakyUpgradeSlot(UpgradeManager manager, int upgradeSlotId, int i, int j, int k, ISlotCheck slotCheck) {
		super(manager.sneakyInv, manager, upgradeSlotId, i, j, k, slotCheck);
	}
}
