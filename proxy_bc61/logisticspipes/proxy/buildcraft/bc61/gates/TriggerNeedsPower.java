package logisticspipes.proxy.buildcraft.bc61.gates;

import logisticspipes.blocks.LogisticsSolderingTileEntity;
import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity;
import logisticspipes.textures.provider.LPActionTriggerIconProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.gates.IGate;
import buildcraft.api.gates.ITileTrigger;
import buildcraft.api.gates.ITriggerParameter;

public class TriggerNeedsPower extends LPTrigger {

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
	public boolean isTriggerActive(IGate gate, ITriggerParameter[] trigger) {
		for(ForgeDirection dir: ForgeDirection.VALID_DIRECTIONS) {
			TileEntity tile = gate.getPipe().getAdjacentTile(dir);
			if(tile instanceof LogisticsPowerJunctionTileEntity) {
				LogisticsPowerJunctionTileEntity LPJTE = (LogisticsPowerJunctionTileEntity)tile;
				if(LPJTE.needMorePowerTriggerCheck) return true;
			}
			if(tile instanceof LogisticsSolderingTileEntity) {
				LogisticsSolderingTileEntity LSTE = (LogisticsSolderingTileEntity)tile;
				if(LSTE.hasWork) return true;
			}
		}
		return false;
	}

	@Override
	public boolean requiresParameter() {
		return false;
	}	
}