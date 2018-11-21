package logisticspipes.proxy.buildcraft;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.transport.tile.TilePipeHolder;

import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.buildcraft.recipeprovider.AssemblyTable;
import logisticspipes.proxy.buildcraft.subproxies.BCPipeCapabilityProvider;
import logisticspipes.proxy.buildcraft.subproxies.IBCPipeCapabilityProvider;
import logisticspipes.proxy.interfaces.IBCProxy;
import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.proxy.specialinventoryhandler.BuildCraftTransactorHandler;
import logisticspipes.recipes.CraftingParts;

import javax.annotation.Nonnull;

public class BuildCraftProxy implements IBCProxy {

	public BuildCraftProxy() {

	}

	@Override
	public void registerPipeInformationProvider() {
		SimpleServiceLocator.pipeInformationManager.registerProvider(TilePipeHolder.class, BCPipeInformationProvider.class);
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

	@Override
	public Object createMjReceiver(@Nonnull LogisticsPowerJunctionTileEntity te) {
		return new IMjReceiver() {
			@Override
			public long getPowerRequested() {
				return te.freeSpace() / LogisticsPowerJunctionTileEntity.MJMultiplier * MjAPI.MJ;
			}

			@Override
			public long receivePower(long l, boolean b) {
				long freeMj = te.freeSpace() / LogisticsPowerJunctionTileEntity.MJMultiplier * MjAPI.MJ;
				long needs = Math.min(freeMj, l);
				if (!b) {
					te.addEnergy(((float) needs) * LogisticsPowerJunctionTileEntity.MJMultiplier / MjAPI.MJ);
				}
				return l - needs;
			}

			@Override
			public boolean canConnect(@Nonnull IMjConnector iMjConnector) {
				return true;
			}
		};
	}
}