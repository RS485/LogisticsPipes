package logisticspipes.asm.wrapper;

import net.minecraft.block.entity.BlockEntity;

import logisticspipes.proxy.interfaces.CraftingRecipeProvider;
import logisticspipes.utils.item.ItemIdentifierInventory;

class CraftingRecipeProviderWrapper extends AbstractWrapper implements CraftingRecipeProvider {

	private CraftingRecipeProvider provider;
	private final String name;

	CraftingRecipeProviderWrapper(CraftingRecipeProvider provider, String name) {
		this.provider = provider;
		this.name = name;
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
	public boolean canOpenGui(BlockEntity tile) {
		if (isEnabled()) {
			try {
				return provider.canOpenGui(tile);
			} catch (Exception | NoClassDefFoundError e) {
				handleException(e);
			}
		}
		return false;
	}

	@Override
	public boolean importRecipe(BlockEntity tile, ItemIdentifierInventory inventory) {
		if (isEnabled()) {
			try {
				return provider.importRecipe(tile, inventory);
			} catch (Exception | NoClassDefFoundError e) {
				handleException(e);
			}
		}
		return false;
	}
}
