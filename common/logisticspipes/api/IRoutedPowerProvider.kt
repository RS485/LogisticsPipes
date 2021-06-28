package logisticspipes.api

import net.minecraft.util.math.BlockPos

/**
 * Things implementing this interface are capable of providing power, but they
 * draw from another sources. Implement {@link ILogisticsPowerProvider} if you wish to
 * provide power to the LP network.
 */
interface IRoutedPowerProvider {

	/**
	 * @return true whether the provider is able to provide the requested [amount] of energy,
	 * the [amount] is subtracted from the available capacity
	 */
	fun useEnergy(amount: Int): Boolean

	/**
	 * @return true whether the provider is able to provide the requested amount of energy
	 */
	fun canUseEnergy(amount: Int): Boolean

	/**
	 * TODO make the MutableList more constrained, ideally this method should not exist
	 * @return true whether the provider is able to provide the requested [amount] of energy,
	 * given that it is not present in the [providersToIgnore] list.
	 * The [amount] is subtracted from the available capacity.
	 */
	fun useEnergy(amount: Int, providersToIgnore: MutableList<Any>): Boolean

	/**
	 * TODO make the MutableList more constrained, ideally this method should not exist
	 * @return true whether the provider is able to provide the requested [amount] of energy,
	 * given that it is not present in the [providersToIgnore] list.
	 */
	fun canUseEnergy(amount: Int, providersToIgnore: MutableList<Any>): Boolean

    /**
     * @return the provider's coordinates
     */
    val blockPos: BlockPos

	/**
	 * @return the provider's X coordinate
	 */
    @Deprecated("use the blockPos/getBlockPos function instead")
	fun getX(): Int

	/**
	 * @return the provider's Y coordinate
	 */
    @Deprecated("use the blockPos/getBlockPos function instead")
	fun getY(): Int

	/**
	 * @return the provider's Z coordinate
	 */
    @Deprecated("use the blockPos/getBlockPos function instead")
	fun getZ(): Int
}
