package logisticspipes.network;

import java.io.IOException;

import network.rs485.logisticspipes.util.LPDataInput;

public interface IReadListObject<T> {

	T readObject(LPDataInput input) throws IOException;
}
