package appeng.api;

import net.minecraft.item.ItemStack;

/**
 * Lets you manipulate existing recipes.
 */
public interface IAppEngGrinderRecipe {
	public ItemStack getInput();
	public void setInput( ItemStack i );

	public ItemStack getOutput();
	public void setOutput( ItemStack o );

	public int getEnergyCost();
	public void setEnergyCost(int c);
}
