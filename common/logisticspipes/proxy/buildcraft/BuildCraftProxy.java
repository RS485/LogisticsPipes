package logisticspipes.proxy.buildcraft;

import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.buildcraft.recipeprovider.AssemblyTable;
import logisticspipes.proxy.buildcraft.subproxies.BCPipeCapabilityProvider;
import logisticspipes.proxy.buildcraft.subproxies.IBCPipeCapabilityProvider;
import logisticspipes.proxy.interfaces.IBCProxy;
import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.proxy.specialinventoryhandler.BuildCraftTransactorHandler;
import logisticspipes.recipes.CraftingParts;

public class BuildCraftProxy implements IBCProxy {

	public BuildCraftProxy() {

	}

	@Override
	public void registerPipeInformationProvider() {
		//SimpleServiceLocator.pipeInformationManager.registerProvider(TilePipeHolder.class, BCPipeInformationProvider.class);
	}

	@Override
	public void initProxy() {
		//PipeEventBus.registerGlobalHandler(new BCEventHandler());
	}

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public boolean isInstalled() {
		return true;
	}

	@Override
	public CraftingParts getRecipeParts() {
		return null;
	}

	@Override
	public void addCraftingRecipes(CraftingParts parts) {}

	@Override
	public Class<? extends ICraftingRecipeProvider> getAssemblyTableProviderClass() {
		return AssemblyTable.class;
	}

	@Override
	public void registerInventoryHandler() {
		SimpleServiceLocator.inventoryUtilFactory.registerHandler(new BuildCraftTransactorHandler());
	}

	@Override
	public IBCPipeCapabilityProvider getIBCPipeCapabilityProvider(LogisticsTileGenericPipe pipe) {
		return new BCPipeCapabilityProvider(pipe);
	}
}