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
import logisticspipes.ticks.DebugGuiTickHandler.VarType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.EntityPlayer;

@Accessors(chain = true)
public class DebugInfoUpdate extends ModernPacket {
	@Getter
	@Setter
	private Integer[] path;
	
	@Getter
	@Setter
	private VarType information;
	
	public DebugInfoUpdate(int id) {
		super(id);
	}
	
	@Override
	public void readData(DataInputStream data) throws IOException {
		int arraySize = data.readInt();
		byte[] bytes = new byte[arraySize];
		data.read(bytes);
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInput in = null;
		in = new ObjectInputStream(bis);
		try {
			information = (VarType) in.readObject();
		} catch(ClassNotFoundException e) {
			throw new UnsupportedOperationException(e);
		}
		int size = data.readInt();
		path = new Integer[size];
		for(int i = 0; i < size; i++) {
			path[i] = data.readInt();
		}
	}
	
	@Override
	public void processPacket(EntityPlayer player) {
		try {
			DebugGuiTickHandler.instance().handleContentUpdatePacket(path, getInformation());
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void writeData(DataOutputStream data) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = new ObjectOutputStream(bos);
		out.writeObject(getInformation());
		byte[] bytes = bos.toByteArray();
		data.writeInt(bytes.length);
		data.write(bytes);
		data.writeInt(path.length);
		for(int i = 0; i < path.length; i++) {
			data.writeInt(path[i]);
		}
	}
	
	@Override
	public ModernPacket template() {
		return new DebugInfoUpdate(getId());
	}

	@Override
	public boolean isCompressable() {
		return true;
	}
}

