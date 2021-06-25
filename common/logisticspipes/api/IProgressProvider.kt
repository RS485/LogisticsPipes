package logisticspipes.api;

/**
 * Implement this in a TileEntity.
 *
 * This will allow LP to access the progress of a TileEntity connected
 * to a Crafting Pipe and display it inside the pipe's crafting tree view.
 */
interface IProgressProvider {
	/**
	 * TODO convert to Float and use range 0.0f to 1.0f as that is how I have seen it done everywhere?
	 * @return a value between 0 and 100 as completion percentage
	 */
	fun getProgress(): Byte
}
