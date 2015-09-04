package network.rs485.debuggui.api;

import java.util.concurrent.Future;

public abstract class IDebugGuiEntry {

	public abstract IDataConnection startServerDebugging(Object object, IDataConnection outgoingData, IObjectIdentification objectIdent);

	public abstract Future<IDataConnection> startClientDebugging(String name, IDataConnection outgoingData);

	public abstract void exec();

	public static IDebugGuiEntry create() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
		return (IDebugGuiEntry) Class.forName("network.rs485.debuggui.DebugGuiEntry").newInstance();
	}

}
