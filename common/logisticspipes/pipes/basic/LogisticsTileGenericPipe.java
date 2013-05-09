package logisticspipes.pipes.basic;

import buildcraft.transport.TileGenericPipe;

public class LogisticsTileGenericPipe extends TileGenericPipe {

	public void queueEvent(String event, Object[] arguments) {}
	public void setTurtrleConnect(boolean flag) {}
	public boolean getTurtrleConnect() {return false;}
	public int getLastCCID() {return -1;}
	
	protected CoreRoutedPipe getCPipe() {
		if(pipe instanceof CoreRoutedPipe) {
			return (CoreRoutedPipe) pipe;
		}
		return null;
	}

	@Override
	public void invalidate() {
		if(!getCPipe().blockRemove()) {
			super.invalidate();
		}
	}
}
