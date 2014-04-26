package logisticspipes.asm.wrapper;

import net.minecraft.tileentity.TileEntity;
import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.utils.item.ItemIdentifierInventory;

class CraftingRecipeProviderWrapper extends AbstractWrapper implements ICraftingRecipeProvider {
	
	private ICraftingRecipeProvider provider;
	private final String name;
	
	CraftingRecipeProviderWrapper(ICraftingRecipeProvider provider, String name) {
		this.provider = provider;
		this.name = name;
	}
	
	@Override
	public void onDisable() {
		this.provider = null;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String getTypeName() {
		return "RecipeProvider";
	}

	@Override
	public boolean canOpenGui(TileEntity tile) {
		if(this.state == WrapperState.Enabled) {
			try {
				provider.canOpenGui(tile);
			} catch(Exception e) {
				handleException(e);
			} catch(NoClassDefFoundError e) {
				handleException(e);
			}
		}
		return false;
	}

	@Override
	public boolean importRecipe(TileEntity tile, ItemIdentifierInventory inventory) {
		if(this.state == WrapperState.Enabled) {
			try {
				provider.importRecipe(tile, inventory);
			} catch(Exception e) {
				handleException(e);
			} catch(NoClassDefFoundError e) {
				handleException(e);
			}
		}
		return false;
	}
}
