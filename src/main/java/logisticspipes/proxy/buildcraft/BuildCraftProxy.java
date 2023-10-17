package logisticspipes.proxy.buildcraft;

import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.transport.tile.TilePipeHolder;

import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity;
import logisticspipes.pipes.basic.ItemInsertionHandler;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.buildcraft.recipeprovider.AssemblyTable;
import logisticspipes.proxy.buildcraft.subproxies.BCPipeCapabilityProvider;
import logisticspipes.proxy.buildcraft.subproxies.IBCPipeCapabilityProvider;
import logisticspipes.proxy.interfaces.IBCProxy;
import logisticspipes.proxy.interfaces.ICraftingRecipeProvider;
import logisticspipes.proxy.specialinventoryhandler.BuildCraftTransactorHandler;
import logisticspipes.recipes.CraftingParts;
import logisticspipes.routing.ItemRoutingInformation;
import logisticspipes.transport.LPTravelingItem;

public class BuildCraftProxy implements IBCProxy {

	public BuildCraftProxy() {

	}

	@Override
	public void registerPipeInformationProvider() {
		SimpleServiceLocator.pipeInformationManager.registerProvider(TilePipeHolder.class, BCPipeInformationProvider.class);
	}

	@Override
	public void initProxy() {
		LogisticsTileGenericPipe.pipeInventoryConnectionChecker.addSupportedClassType(TileBC_Neptune.class);

		ItemInsertionHandler.ACCEPTORS.add((pipe, from, stack) -> {
			if (!stack.isEmpty() && stack.hasTagCompound() && stack.getTagCompound().hasKey("logisticspipes:routingdata_buildcraft")) {
				NBTTagCompound routingData = stack.getTagCompound().getCompoundTag("logisticspipes:routingdata_buildcraft");
				ItemRoutingInformation info = ItemRoutingInformation.restoreFromNBT(routingData);
				LPTravelingItem item = new LPTravelingItem.LPTravelingItemServer(info);
				item.output = from.getOpposite();
				return pipe.acceptItem(item, null);
			}
			return false;
		});
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

	@Override
	public boolean isBuildCraftPipe(TileEntity tile) {
		return tile instanceof TilePipeHolder;
	}
}