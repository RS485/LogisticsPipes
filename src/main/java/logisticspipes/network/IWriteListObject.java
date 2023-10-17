package logisticspipes.network;

import network.rs485.logisticspipes.util.LPDataOutput;

public interface IWriteListObject<T> {

	void writeObject(LPDataOutput output, T object);
}
