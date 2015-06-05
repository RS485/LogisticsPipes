package logisticspipes.nei;

import java.awt.Rectangle;

import logisticspipes.gui.GuiSolderingStation;
import logisticspipes.recipes.SolderingStationRecipes;
import logisticspipes.recipes.SolderingStationRecipes.SolderingStationRecipe;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;

import codechicken.nei.NEIServerUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.ShapedRecipeHandler;

public class NEISolderingStationRecipeManager extends ShapedRecipeHandler {

	private ShapedRecipeHandler.CachedShapedRecipe getShape(SolderingStationRecipe recipe) {
		ShapedRecipeHandler.CachedShapedRecipe shape = new ShapedRecipeHandler.CachedShapedRecipe(0, 0, null, recipe.result);
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				if (recipe.source[y * 3 + x] == null) {
					continue;
				}
				PositionedStack stack = new PositionedStack(recipe.source[y * 3 + x], 39 + x * 18, 6 + y * 18);
				stack.setMaxSize(1);
				shape.ingredients.add(stack);
			}
		}
		PositionedStack stack = new PositionedStack(new ItemStack(Items.iron_ingot, 1), 102, 6);
		stack.setMaxSize(1);
		shape.ingredients.add(stack);
		shape.result.relx = 136;
		shape.result.rely = 36;
		return shape;
	}

	@Override
	public void loadCraftingRecipes(ItemStack result) {
		for (SolderingStationRecipe recipe : SolderingStationRecipes.getRecipes()) {
			if (NEIServerUtils.areStacksSameTypeCrafting(recipe.result, result)) {
				arecipes.add(getShape(recipe));
			}
		}
	}

	@Override
	public void loadTransferRects() {
		transferRects.add(new RecipeTransferRect(new Rectangle(101, 27, 24, 26), "solderingstation"));
	}

	@Override
	public Class<? extends GuiContainer> getGuiClass() {
		return GuiSolderingStation.class;
	}

	@Override
	public String getRecipeName() {
		return "Soldering Station";
	}

	@Override
	public String getGuiTexture() {
		return "logisticspipes:textures/gui/soldering_station_nei.png";
	}

	@Override
	public boolean hasOverlay(GuiContainer gui, Container container, int recipe) {
		return false;
	}

	@Override
	public void loadUsageRecipes(ItemStack ingredient) {
		for (SolderingStationRecipe recipe : SolderingStationRecipes.getRecipes()) {
			for (ItemStack source : recipe.source) {
				if (NEIServerUtils.areStacksSameTypeCrafting(source, ingredient)) {
					arecipes.add(getShape(recipe));
					break;
				}
			}
		}
	}

	@Override
	public void loadCraftingRecipes(String outputId, Object... results) {
		if (outputId.equals("solderingstation") && getClass() == NEISolderingStationRecipeManager.class) {
			for (SolderingStationRecipe recipe : SolderingStationRecipes.getRecipes()) {
				arecipes.add(getShape(recipe));
			}
		} else {
			super.loadCraftingRecipes(outputId, results);
		}
	}
}
