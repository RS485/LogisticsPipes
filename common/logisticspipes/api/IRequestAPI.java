package logisticspipes.api;

import java.util.List;

import net.minecraft.item.ItemStack;

/**
 * Public interface implemented by Request pipes
 */
public interface IRequestAPI {

	/**
	 * do NOT modify the tagcompounds of the returned itemstacks
	 * @return list of items and amounts provided by providers in the network
	 */
	List<ItemStack> getProvidedItems();

	/**
	 * do NOT modify the tagcompounds of the returned itemstacks
	 * @return list of items craftable by crafters in the network, stacksize is always 0
	 */
	List<ItemStack> getCraftedItems();

	/**
	 * do NOT modify the tagcompounds of the returned itemstacks
	 * @param wanted result
	 * @return list of provided items and amounts that would be used by this request
	 */
	List<ItemStack> simulateRequest(ItemStack wanted);

	/**
	 * do NOT modify the tagcompounds of the returned itemstacks
	 * @param wanted result
	 * @return list of missing items, empty list if request was successful
	 */
	List<ItemStack> performRequest(ItemStack wanted);

}
