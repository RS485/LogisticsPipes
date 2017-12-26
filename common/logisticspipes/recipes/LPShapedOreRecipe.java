package logisticspipes.recipes;

import logisticspipes.LPConstants;
import logisticspipes.config.Configs;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.PlayerIdentifier;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import net.minecraftforge.oredict.ShapedOreRecipe;

public class LPShapedOreRecipe extends ShapedOreRecipe {

	private final CraftingDependency dependent;

	public LPShapedOreRecipe(ResourceLocation registryName, ItemStack result, CraftingDependency dependent, Object... recipe) {
		super(new ResourceLocation(LPConstants.LP_MOD_ID, "group.mainRecipeGroup"), result, recipe);
		this.dependent = dependent;
		dependent.addStack(result);
		setRegistryName(registryName);
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv) {
		if (Configs.ENABLE_RESEARCH_SYSTEM) {
			PlayerIdentifier name = SimpleServiceLocator.craftingPermissionManager.getPlayerID(inv);
			if (name == null || name.equals("")) {
				return null;
			}
			if (!SimpleServiceLocator.craftingPermissionManager.isAllowedFor(dependent, name)) {
				return null;
			}
		}
		return super.getCraftingResult(inv);
	}

	@Override
	public boolean matches(InventoryCrafting inv, World world) {
		if (Configs.ENABLE_RESEARCH_SYSTEM) {
			PlayerIdentifier name = SimpleServiceLocator.craftingPermissionManager.getPlayerID(inv);
			if (name == null || name.equals("")) {
				return false;
			}
			if (!SimpleServiceLocator.craftingPermissionManager.isAllowedFor(dependent, name)) {
				return false;
			}
		}
		return super.matches(inv, world);
	}

	@Override
	public ItemStack getRecipeOutput() {
		if (MainProxy.isClient() && Configs.ENABLE_RESEARCH_SYSTEM) {
			if (!SimpleServiceLocator.craftingPermissionManager.clientSidePermission.contains(dependent)) {
				return null;
			}
		}
		return super.getRecipeOutput();
	}
}
