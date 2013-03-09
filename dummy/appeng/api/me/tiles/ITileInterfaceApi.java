package appeng.api.me.tiles;

import java.util.List;

import appeng.api.InterfaceCraftingRequest;
import appeng.api.me.util.IMEInventory;

import net.minecraft.item.ItemStack;

public interface ITileInterfaceApi {
	
	int apiCurrentAvailableSpace(ItemStack i, int MaxNeeded);
	
	ItemStack apiExtractNetworkItem(ItemStack i, boolean doExtract);
	
	ItemStack apiAddNetworkItem(ItemStack i, boolean doAdd);
	
	List<ItemStack> apiGetNetworkContents();

	IMEInventory getApiArray();
	
	List<ItemStack> getCraftingOptions();
	
	InterfaceCraftingRequest requestCrafting( ItemStack req, boolean calculateOnly );
	
}
