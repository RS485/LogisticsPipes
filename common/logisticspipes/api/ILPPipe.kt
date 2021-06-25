package logisticspipes.api

/**
 * Base interface for all pipes in the LP mod
 */
interface ILPPipe {

	/**
	 * @return is this pipe capable of routing items in the network
	 */
	fun isRouted(): Boolean
}
