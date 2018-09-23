package logisticspipes.proxy.interfaces;

import logisticspipes.asm.IgnoreDisabledProxy;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.buildcraft.subproxies.IBCPipeCapabilityProvider;
import logisticspipes.recipes.CraftingParts;

public interface IBCProxy {

	void registerPipeInformationProvider();

	void initProxy();

	boolean isActive();

	@IgnoreDisabledProxy
	boolean isInstalled();

	CraftingParts getRecipeParts();

	void addCraftingRecipes(CraftingParts parts);

	Class<? extends ICraftingRecipeProvider> getAssemblyTableProviderClass();

	void registerInventoryHandler();

	IBCPipeCapabilityProvider getIBCPipeCapabilityProvider(LogisticsTileGenericPipe pipe);
}
