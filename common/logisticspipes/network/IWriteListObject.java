package logisticspipes.network;

import java.io.IOException;

import network.rs485.logisticspipes.util.LPDataOutput;

public interface IWriteListObject<T> {

	void writeObject(LPDataOutput output, T object) throws IOException;
}
