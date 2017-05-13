/*
package logisticspipes.proxy.buildcraft.gates;

import java.util.Collection;
import java.util.LinkedList;

import javax.annotation.Nonnull;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.ITriggerExternal;
import buildcraft.api.statements.ITriggerInternal;
import buildcraft.api.statements.ITriggerInternalSided;
import buildcraft.api.statements.ITriggerProvider;

import logisticspipes.blocks.LogisticsSolderingTileEntity;
import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.pipes.PipeItemsFluidSupplier;
import logisticspipes.pipes.PipeItemsSupplierLogistics;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.buildcraft.BuildCraftProxy;
import logisticspipes.proxy.buildcraft.subproxies.LPBCTileGenericPipe;

public class LogisticsTriggerProvider implements ITriggerProvider {

	@Override
	public void addInternalTriggers(Collection<ITriggerInternal> triggers, IStatementContainer pipe) {
		if (pipe.getTile() instanceof LPBCTileGenericPipe) {
			LogisticsTileGenericPipe lPipe = ((LPBCTileGenericPipe) pipe.getTile()).getLpPipe();;
			if (lPipe.pipe instanceof PipeItemsSupplierLogistics || lPipe.pipe instanceof PipeItemsFluidSupplier) {
				triggers.add(BuildCraftProxy.LogisticsFailedTrigger);
			}
			if (lPipe.pipe instanceof PipeItemsCraftingLogistics) {
				triggers.add(BuildCraftProxy.LogisticsCraftingTrigger);
			}
			if (lPipe.pipe instanceof CoreRoutedPipe) {
				//Only show this conditional on Gates that can accept parameters
				triggers.add(BuildCraftProxy.LogisticsHasDestinationTrigger);
			}
		}
	}

	@Override
	public void addInternalSidedTriggers(Collection<ITriggerInternalSided> triggers, IStatementContainer container, @Nonnull EnumFacing side) {

	}

	@Override
	public void addExternalTriggers(Collection<ITriggerExternal> triggers, @Nonnull EnumFacing side, TileEntity tile) {
		if (tile instanceof LogisticsPowerJunctionTileEntity || tile instanceof LogisticsSolderingTileEntity) {
			triggers.add(BuildCraftProxy.LogisticsNeedPowerTrigger);
		}
	}
}
*/