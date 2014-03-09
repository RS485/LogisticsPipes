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
import cpw.mods.fml.common.network.Player;

@Accessors(chain = true)
public class DebugSetVarContent extends ModernPacket {
	
	@Getter
	@Setter
	private String content;
	
	@Getter
	@Setter
	private Integer[] path;
	
	public DebugSetVarContent(int id) {
		super(id);
	}
	
	@Override
	public void readData(DataInputStream data) throws IOException {
		content = data.readUTF();
		int size = data.readInt();
		path = new Integer[size];
		for(int i = 0; i < size; i++) {
			path[i] = data.readInt();
		}
	}
	
	@Override
	public void processPacket(EntityPlayer player) {
		try {
			DebugGuiTickHandler.instance().handleVarChangePacket(path, content, (Player) player);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void writeData(DataOutputStream data) throws IOException {
		data.writeUTF(content);
		data.writeInt(path.length);
		for(int i = 0; i < path.length; i++) {
			data.writeInt(path[i]);
		}
	}
	
	@Override
	public ModernPacket template() {
		return new DebugSetVarContent(getId());
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}

