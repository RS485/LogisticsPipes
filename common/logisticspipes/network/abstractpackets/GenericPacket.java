package logisticspipes.network.abstractpackets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;

import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(chain = true)
public abstract class GenericPacket extends ModernPacket {

	@Getter
	private Object[] args;

	public GenericPacket(int id) {
		super(id);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		int size = data.readInt();
		args = new Object[size];
		for (int i = 0; i < size; i++) {
			int arraySize = data.readInt();
			byte[] bytes = new byte[arraySize];
			data.read(bytes);
			ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
			ObjectInput in = null;
			in = new ObjectInputStream(bis);
			try {
				Object o = in.readObject();
				args[i] = o;
			} catch (ClassNotFoundException e) {
				throw new UnsupportedOperationException(e);
			}
		}
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeInt(args.length);
		for (Object arg : args) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutput out = null;
			out = new ObjectOutputStream(bos);
			out.writeObject(arg);
			byte[] bytes = bos.toByteArray();
			data.writeInt(bytes.length);
			data.write(bytes);
		}
	}

	public GenericPacket setArgs(Object... input) {
		args = input;
		return this;
	}
}
