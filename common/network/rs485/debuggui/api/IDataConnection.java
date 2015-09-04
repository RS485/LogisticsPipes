package network.rs485.debuggui.api;

public interface IDataConnection {
	void passData(byte[] packet);
	void closeCon();
}
