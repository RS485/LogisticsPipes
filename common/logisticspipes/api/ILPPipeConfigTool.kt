package logisticspipes.api

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack

/**
 * Implement this in an Item to allow pipe GUI configuration.
 *
 * Some mod compatibility is already implemented inside LP.
 */
interface ILPPipeConfigTool {
	/**
	 * @return true if the [player] is allowed to use the [tool][wrench] on a [pipe].
	 */
	fun canWrench(player: EntityPlayer, wrench: ItemStack, pipe: ILPPipeTile): Boolean

	/**
	 * TODO I wan unable to determine how this is used
	 */
	fun wrenchUsed(player: EntityPlayer, wrench: ItemStack, pipe: ILPPipeTile)
}
