package logisticspipes.api;

import network.rs485.logisticspipes.pipe.PipeType;

/**
 * Public interface implemented by LP's internal Pipe logic
 */
public interface ILPPipe {

	/**
	 * @return true if the pipe can route items inside the network
	 */
	boolean isRoutedPipe();

	PipeType getType();

}
