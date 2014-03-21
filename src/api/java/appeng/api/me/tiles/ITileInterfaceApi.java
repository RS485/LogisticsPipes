package appeng.api.me.tiles;

import java.util.List;

import net.minecraft.item.ItemStack;
import appeng.api.me.util.IMEInventory;
import appeng.api.me.util.InterfaceCraftingResponse;

public interface ITileInterfaceApi {
	
	int apiCurrentAvailableSpace(ItemStack i, int MaxNeeded);
	
	ItemStack apiExtractNetworkItem(ItemStack i, boolean doExtract);
	
	ItemStack apiAddNetworkItem(ItemStack i, boolean doAdd);
	
	List<ItemStack> apiGetNetworkContents();

	IMEInventory getApiArray();
	
	List<ItemStack> getCraftingOptions();

	List<InterfaceCraftingPattern> findCraftingPatterns( ItemStack req );
	
	InterfaceCraftingResponse requestCrafting( ItemStack req, boolean enableRecursive );
	
}
