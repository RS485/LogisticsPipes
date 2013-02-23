package gregtechmod.api;

import net.minecraft.item.ItemStack;

/**
 * You are allowed to include this File in your Download, as i will not change it.
 */
public interface IQuantumChest {
	/**
	 * Is this even a TileEntity of a Quantum Chest. I need things like this Function for MetaTileEntities
	 */
	public boolean isQuantumChest();
	
	/**
	 * Gives an Array of Stacks with Size (of all the Data-stored Items) of the correspondent Item kinds (regular QChests have only one)
	 * Does NOT include the 64 "ready" Items inside the Slots.
	 */
	public ItemStack[] getStoredItemData();
}
