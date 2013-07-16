package logisticspipes.network.packets.debuggui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.ticks.DebugGuiTickHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;

@Accessors(chain = true)
public class DebugTargetResponse extends ModernPacket {
	
	public DebugTargetResponse(int id) {
		super(id);
	}
	
	public enum TargetMode {
		Block,
		Entity,
		None;
	}
	
	@Getter
	@Setter
	private TargetMode mode;
	
	@Getter
	@Setter
	private Object[] additions = new Object[0];
	
	@Override
	public void readData(DataInputStream data) throws IOException {
		mode = TargetMode.values()[data.readByte()];
		int size = data.readInt();
		additions = new Object[size];
		for(int i = 0; i < size; i++) {
			int arraySize = data.readInt();
			byte[] bytes = new byte[arraySize];
			data.read(bytes);
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			ObjectInput in = null;
			in = new ObjectInputStream(bis);
			try {
				Object o = in.readObject();
				additions[i] = o;
			} catch(ClassNotFoundException e) {
				throw new UnsupportedOperationException(e);
			}
		}
	}
	
	@Override
	public void processPacket(EntityPlayer player) {
		DebugGuiTickHandler.instance().targetResponse(mode, player, additions);
	}
	
	@Override
	public void writeData(DataOutputStream data) throws IOException {
		data.writeByte(mode.ordinal());
		data.writeInt(additions.length);
		for(int i = 0; i < additions.length; i++) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutput out = null;
			out = new ObjectOutputStream(bos);
			out.writeObject(additions[i]);
			byte[] bytes = bos.toByteArray();
			data.writeInt(bytes.length);
			data.write(bytes);
		}
	}
	
	@Override
	public ModernPacket template() {
		return new DebugTargetResponse(getId());
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}

