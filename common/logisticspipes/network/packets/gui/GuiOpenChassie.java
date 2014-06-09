package logisticspipes.network.packets.gui;

import net.minecraft.entity.player.EntityPlayer;
import logisticspipes.LogisticsPipes;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;

public class GuiOpenChassie extends CoordinatesPacket {
	public GuiOpenChassie(int id) {
		super(id);
	}
	
	@Override
	public void processPacket(EntityPlayer player) {
		player.openGui(LogisticsPipes.instance, GuiIDs.GUI_ChassiModule_ID, player.worldObj, getPosX(), getPosY(), getPosZ());
	}

	@Override
	public ModernPacket template() {
		return new GuiOpenChassie(getId());
	}
}
