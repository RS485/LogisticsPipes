package logisticspipes.api

import net.minecraft.item.ItemStack

/**
 * Implement this in an armor to provide HUD capability.
 */
interface IHUDArmor {
	/**
	 * TODO I was not able to deduce how this may be useful as our only implementation returns true
	 */
	fun isEnabled(item: ItemStack): Boolean
}
