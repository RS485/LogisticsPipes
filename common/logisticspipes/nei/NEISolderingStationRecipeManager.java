package logisticspipes.nei;

import logisticspipes.config.SolderingStationRecipes;
import logisticspipes.config.SolderingStationRecipes.SolderingStationRecipe;
import logisticspipes.gui.GuiSolderingStation;
import net.minecraft.src.GuiContainer;
import net.minecraft.src.GuiCrafting;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import codechicken.nei.NEIClientUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.ShapedRecipeHandler;

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
}
