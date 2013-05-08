package logisticspipes.pipes.basic;

import buildcraft.transport.TileGenericPipe;

public class LogisticsTileGenericPipe extends TileGenericPipe {

	public void queueEvent(String event, Object[] arguments) {
		//Implemented by subClass
	}

	public void setTurtrleConnect(boolean flag) {}
	public boolean getTurtrleConnect() {return false;}

	public int getLastCCID() {return -1;}
}
