package logisticspipes.network.packets.debuggui;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.ticks.DebugGuiTickHandler;
import net.minecraft.entity.player.EntityPlayer;

public class DebugAskForTarget extends ModernPacket {
	
	public DebugAskForTarget(int id) {
		super(id);
	}
	
	@Override
	public void readData(DataInputStream data) throws IOException {}
	
	@Override
	public void processPacket(EntityPlayer player) {
		DebugGuiTickHandler.instance().handleTargetRequest();
	}
	
	@Override
	public void writeData(DataOutputStream data) throws IOException {}
	
	@Override
	public ModernPacket template() {
		return new DebugAskForTarget(getId());
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}

