package logisticspipes.network.abstractpackets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import lombok.Getter;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public abstract class GenericPacket extends ModernPacket {

	@Getter
	private Object[] args;

	public GenericPacket(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInput input) throws IOException {
		int size = input.readInt();
		args = new Object[size];
		for (int i = 0; i < size; i++) {
			byte[] bytes = input.readByteArray();
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			ObjectInput in = new ObjectInputStream(bis);
			try {
				Object o = in.readObject();
				args[i] = o;
			} catch (ClassNotFoundException e) {
				throw new UnsupportedOperationException(e);
			}
		}
	}

	@Override
	public void writeData(LPDataOutput output) throws IOException {
		output.writeInt(args.length);
		for (Object arg : args) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutput out = new ObjectOutputStream(bos);
			out.writeObject(arg);
			output.writeByteArray(bos.toByteArray());
		}
	}

	public GenericPacket setArgs(Object... input) {
		args = input;
		return this;
	}
}
