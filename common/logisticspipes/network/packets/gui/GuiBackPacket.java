package logisticspipes.network.packets.gui;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.abstractpackets.IntegerCoordinatesPacket;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.pipes.PipeLogisticsChassi;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.TileGenericPipe;

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
		final TileGenericPipe pipe = this.getPipe(player.worldObj);
		if(pipe == null) {
			return;
		}
		if( !(pipe.pipe instanceof PipeLogisticsChassi)) {
			return;
		}
		player.openGui(LogisticsPipes.instance, getInteger(), player.worldObj, getPosX(), getPosY(), getPosZ());
	}
}

