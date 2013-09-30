package logisticspipes.recipes;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class LPShapelessOreRecipe extends ShapelessOreRecipe {

	private final CraftingDependency dependent;
	
	public LPShapelessOreRecipe(ItemStack result, CraftingDependency dependent, Object[] recipe) {
		super(result, recipe);
		this.dependent = dependent;
		dependent.addStack(result);
	}
/*
	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		String name = CraftingPermissionManager.getPlayerName(inv);
		if(name == null || name.equals("")) return null;
		if(!CraftingPermissionManager.isAllowedFor(dependent, name)) {
			return null;
		}
		return super.getCraftingResult(inv);
	}

	@Override
	public boolean matches(InventoryCrafting inv, World world) {
		String name = CraftingPermissionManager.getPlayerName(inv);
		if(name == null || name.equals("")) return false;
		if(!CraftingPermissionManager.isAllowedFor(dependent, name)) {
			return false;
		}
		return super.matches(inv, world);
	}

	@Override
	public ItemStack getRecipeOutput() {
		if(MainProxy.isClient()) {
			if(!CraftingPermissionManager.clientSidePermission.contains(dependent)) {
				return null;
			}
		}
		return super.getRecipeOutput();
	}//*/
}
