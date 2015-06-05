package logisticspipes.network;

import java.io.IOException;

public interface IReadListObject<T> {

	public T readObject(LPDataInputStream data) throws IOException;
}
