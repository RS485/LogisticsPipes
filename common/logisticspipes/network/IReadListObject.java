package logisticspipes.network;

import network.rs485.logisticspipes.util.LPDataInput;

public interface IReadListObject<T> {

	T readObject(LPDataInput input);
}
