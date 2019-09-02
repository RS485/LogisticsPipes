package logisticspipes.api;

/**
 * Implemented by an TileEntity this will allow LP to access the current
 * progress of an TileEntity connected to a crafting pipe and display the
 * progress inside it's crafting tree view.
 */
public interface IProgressProvider {

	/**
	 * @return a value between 0 and 100 that indicates the current progress of
	 * this TileEntity machine
	 */
	byte getMachineProgressForLP();
}
