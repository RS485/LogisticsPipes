package crazypants.enderio.conduit;

public interface IConduitBundle {

	boolean hasType(Class<? extends IConduit> type);

	<T extends IConduit> T getConduit(Class<T> type);

}
