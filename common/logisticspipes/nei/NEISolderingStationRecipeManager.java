package logisticspipes.nei;

import java.util.List;

import logisticspipes.gui.GuiSolderingStation;
import logisticspipes.recipes.SolderingStationRecipes;
import logisticspipes.recipes.SolderingStationRecipes.SolderingStationRecipe;
import net.minecraft.src.Container;
import net.minecraft.src.CraftingManager;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.IRecipe;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.ShapedRecipes;
import codechicken.nei.DefaultOverlayRenderer;
import codechicken.nei.NEIClientUtils;
import codechicken.nei.NEICompatibility;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.ShapedRecipeHandler;
import codechicken.nei.recipe.weakDependancy_Forge;
import codechicken.nei.recipe.ShapedRecipeHandler.CachedShapedRecipe;

public class NEISolderingStationRecipeManager extends ShapedRecipeHandler {

	private ShapedRecipeHandler.CachedShapedRecipe getShape(SolderingStationRecipe recipe) {
		ShapedRecipeHandler.CachedShapedRecipe shape = new ShapedRecipeHandler.CachedShapedRecipe(0, 0, null, recipe.result);
		for(int x = 0; x < 3; x++)
		{
			for(int y = 0; y < 3; y++)
			{
				if(recipe.source[y*3 + x] == null)
				{
					continue;
				}
				PositionedStack stack = new PositionedStack(recipe.source[y*3 + x], 25+x*18, 6+y*18);
				stack.setMaxSize(1);
				shape.ingredients.add(stack);
			}
		}
		PositionedStack stack = new PositionedStack(new ItemStack(Item.ingotIron,1), 88, 6);
		stack.setMaxSize(1);
		shape.ingredients.add(stack);
		shape.result.relx = 122;
		shape.result.rely = 36;
		return shape;
	}
	
	@Override
	public void loadCraftingRecipes(ItemStack result) {
		for(SolderingStationRecipe recipe: SolderingStationRecipes.getRecipes()) {
			if(NEIClientUtils.areStacksSameTypeCrafting(recipe.result, result)) {
		        this.arecipes.add(getShape(recipe));
			}
		}
	}
	
	@Override
	public Class<? extends GuiContainer> getGuiClass()
	{
		return GuiSolderingStation.class;
	}

	@Override
	public String getRecipeName()
	{
		return "Soldering Station";
	}

	@Override
	public String getGuiTexture()
	{
		return "/logisticspipes/gui/soldering_station.png";
	}


	@Override
	public boolean hasOverlay(GuiContainer gui, Container container, int recipe)
	{
		return false;
	}
	
	@Override
	public void loadUsageRecipes(ItemStack ingredient) {
		for(SolderingStationRecipe recipe: SolderingStationRecipes.getRecipes()) {
			for(ItemStack source : recipe.source) {
				if(NEIClientUtils.areStacksSameTypeCrafting(source, ingredient)) {
			        this.arecipes.add(getShape(recipe));
			        break;
				}
			}
		}
	}
	

	
	@Override
	public void loadCraftingRecipes(String outputId, Object... results) {
		if(outputId.equals("crafting") && getClass() == NEISolderingStationRecipeManager.class) {
			for(SolderingStationRecipe recipe: SolderingStationRecipes.getRecipes()) {
				this.arecipes.add(getShape(recipe));
			}
		} else {
			super.loadCraftingRecipes(outputId, results);
		}
	}
}
