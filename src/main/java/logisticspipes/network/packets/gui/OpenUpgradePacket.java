package logisticspipes.network.packets.gui;

import net.minecraft.entity.player.EntityPlayer;

import logisticspipes.network.abstractguis.UpgradeCoordinatesGuiProvider;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.abstractpackets.SlotPacket;
import logisticspipes.pipes.upgrades.IConfigPipeUpgrade;
import logisticspipes.pipes.upgrades.IPipeUpgrade;
import logisticspipes.utils.StaticResolve;
import logisticspipes.utils.gui.UpgradeSlot;

@StaticResolve
public class OpenUpgradePacket extends SlotPacket {

	public OpenUpgradePacket(int id) {
		super(id);
	}

	@Override
	public void processPacket(EntityPlayer player) {
		UpgradeSlot slot = getSlot(player, UpgradeSlot.class);
		IPipeUpgrade upgrade = slot.getUpgrade();
		if (upgrade instanceof IConfigPipeUpgrade) {
			UpgradeCoordinatesGuiProvider gui = ((IConfigPipeUpgrade) upgrade).getGUI();
			if (gui != null) {
				gui.setSlot(slot).setLPPos(slot.getManager().getPipePosition()).open(player);
			}
		}
	}

	@Override
	public ModernPacket template() {
		return new OpenUpgradePacket(getId());
	}
}
