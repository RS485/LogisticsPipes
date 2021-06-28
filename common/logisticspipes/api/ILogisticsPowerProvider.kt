package logisticspipes.api

/**
 * Implement this in a block to provide power to the LP network.
 *
 * Lists of these objects available to a network will be cached, and the closest
 * one with power preferentially pulled from.
 */
interface ILogisticsPowerProvider : IRoutedPowerProvider {
	/**
	 * @return the stored amount of energy available in this provider
	 */
	fun getPowerLevel(): Int
}
