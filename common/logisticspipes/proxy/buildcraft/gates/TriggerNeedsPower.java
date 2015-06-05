package logisticspipes.proxy.buildcraft.gates;

import logisticspipes.blocks.LogisticsSolderingTileEntity;
import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity;
import logisticspipes.textures.provider.LPActionTriggerIconProvider;

import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.ITriggerExternal;

public class TriggerNeedsPower extends LPTrigger implements ITriggerExternal {

	public TriggerNeedsPower() {
		super("LogisticsPipes:trigger.nodeRequestsRecharge");
	}

	@Override
	public int getIconIndex() {
		return LPActionTriggerIconProvider.triggerPowerNeededIconIndex;
	}

	@Override
	public String getDescription() {
		return "Needs More Power";
	}

	@Override
	public boolean isTriggerActive(TileEntity tile, ForgeDirection dir, IStatementContainer paramIStatementContainer, IStatementParameter[] paramArrayOfIStatementParameter) {
		if (tile instanceof LogisticsPowerJunctionTileEntity) {
			LogisticsPowerJunctionTileEntity LPJTE = (LogisticsPowerJunctionTileEntity) tile;
			if (LPJTE.needMorePowerTriggerCheck) {
				return true;
			}
		}
		if (tile instanceof LogisticsSolderingTileEntity) {
			LogisticsSolderingTileEntity LSTE = (LogisticsSolderingTileEntity) tile;
			if (LSTE.hasWork) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean requiresParameter() {
		return false;
	}
}
