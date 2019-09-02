package net.mcft.copy.betterstorage.api.crate;

import net.minecraft.item.ItemStack;

public interface ICrateStorage {

	// The "identifier" item stacks are used to look up specific
	// types of items within the crate. Item type, damage and NBT
	// data are used to identify items, so they have to match.

	// You can see which exact items are stored within the crate
	// using the getContents() and getRandomStacks() methods and
	// use the stacks returned to extract these items. Do not
	// modify stacks returned by these methods.

	// Storage crates are supposed to be random and less efficient,
	// as a downside to their ability to expand and store lots of
	// items. Therefore, where possible / it makes sense, it's
	// recommended to use getRandomStacks(). Look at a few items
	// every tick (or a different interval) and give up if none
	// matched the requirements. This encourages players to keep
	// the number of different items within a crate low.

	// If this is not possible, it's recommended to balance things
	// by for example increasing resource or energy cost, perhaps
	// depending on the number of unique items.

	/**
	 * Returns the crate's identifier. If it's the same as another
	 * crate's identifier, they're part of the same crate pile and
	 * therefore share the same inventory.
	 */
	Object getCrateIdentifier();

	/**
	 * Returns the number of stacks this crate pile can hold.
	 */
	int getCapacity();

	/**
	 * Returns the number of slots that are occupied.
	 */
	int getOccupiedSlots();

	/**
	 * Returns the number of different unique items. This is also the
	 * number of items that will be returned by the getContents methods.
	 */
	int getUniqueItems();

	/**
	 * Returns all items in the crate pile. The stacks may have
	 * stack sizes above their usual limit.
	 */
	Iterable<ItemStack> getContents();

	/**
	 * Returns a randomized stream of stacks from the crate pile.
	 * It's not recommended to use this to iterate over the whole
	 * inventory, especially for crates storing lots of items.
	 */
	Iterable<ItemStack> getRandomStacks();

	/**
	 * Returns the number of items of this specific type.
	 */
	int getItemCount(ItemStack identifier);

	/**
	 * Returns the space left for items of this specific type.
	 */
	int getSpaceForItem(ItemStack identifier);

	/**
	 * Tries to insert an item stack into the crate. Returns null
	 * if all items were inserted successfully, or an item stack
	 * of whatever items could not be inserted.
	 * The stack may have a stack size above its usual limit.
	 */
	ItemStack insertItems(ItemStack stack);

	/**
	 * Tries to extract the specified type and amount of items.
	 * Returns null if no items could be extracted, or an item
	 * stack if some, or all of them could be extracted successfully.
	 */
	ItemStack extractItems(ItemStack identifier, int amount);

	/**
	 * Registers a crate watcher. Its onCrateItemsModified method
	 * will be called when any items are changed.
	 */
	void registerCrateWatcher(ICrateWatcher watcher);

	/**
	 * Unregisters a crate watcher.
	 */
	void unregisterCrateWatcher(ICrateWatcher watcher);

}
