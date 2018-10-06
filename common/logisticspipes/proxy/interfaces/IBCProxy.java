package logisticspipes.proxy.interfaces;

import logisticspipes.asm.IgnoreDisabledProxy;
import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.buildcraft.subproxies.IBCPipeCapabilityProvider;
import logisticspipes.recipes.CraftingParts;

import javax.annotation.Nonnull;

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

	Object createMjReceiver(@Nonnull LogisticsPowerJunctionTileEntity te);
}
