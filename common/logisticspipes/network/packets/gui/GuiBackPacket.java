package logisticspipes.network.packets.gui;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import net.minecraft.entity.player.EntityPlayer;

public class GuiBackPacket extends IntegerCoordinatesPacket {

	public GuiBackPacket(int id) {
		super(id);
	}

	@Override
	public ModernPacket template() {
		return new GuiBackPacket(getId());
	}

	@Override
	public void processPacket(EntityPlayer player) {
		final LogisticsTileGenericPipe pipe = this.getPipe(player.worldObj);
		if(pipe == null) {
			return;
		}
		if( !(pipe.pipe instanceof PipeLogisticsChassi)) {
			return;
		}
		player.openGui(LogisticsPipes.instance, getInteger(), player.worldObj, getPosX(), getPosY(), getPosZ());
	}
}

