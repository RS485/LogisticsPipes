package logisticspipes.network.packets.debuggui;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.ticks.DebugGuiTickHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;

@Accessors(chain = true)
public class DebugPanelOpen extends ModernPacket {
	
	@Setter
	@Getter
	private String name;
	
	public DebugPanelOpen(int id) {
		super(id);
	}
	
	@Override
	public ModernPacket template() {
		return new DebugPanelOpen(getId());
	}
	
	@Override
	public void readData(DataInputStream data) throws IOException {
		setName(data.readUTF());
	}
	
	@Override
	public void writeData(DataOutputStream data) throws IOException {
		data.writeUTF(getName());
	}
	
	@Override
	public void processPacket(EntityPlayer player) {
		try {
			DebugGuiTickHandler.instance().createNewDebugGui(getName());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}

