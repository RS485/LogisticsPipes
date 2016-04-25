package logisticspipes.network.packets.gui;

import logisticspipes.network.abstractguis.UpgradeCoordinatesGuiProvider;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.IntegerPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.SlotPacket;
import logisticspipes.network.exception.TargetNotFoundException;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.pipes.upgrades.IConfigPipeUpgrade;
import logisticspipes.pipes.upgrades.IPipeUpgrade;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.UpgradeSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;

public class OpenUpgradePacket extends SlotPacket {
	public OpenUpgradePacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		UpgradeSlot slot = getSlot(player, UpgradeSlot.class);
		IPipeUpgrade upgrade = slot.getUpgrade();
		if(upgrade instanceof IConfigPipeUpgrade) {
			UpgradeCoordinatesGuiProvider gui = ((IConfigPipeUpgrade) upgrade).getGUI();
			if(gui != null) {
				gui.setSlot(slot).setLPPos(slot.getManager().getPipePosition()).open(player);
			}
		}
	}

	@Override
	public ModernPacket template() {
		return new OpenUpgradePacket(getId());
	}
}
