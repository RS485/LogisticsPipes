package logisticspipes.proxy.buildcraft.gates;

import logisticspipes.blocks.LogisticsSolderingTileEntity;
import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity;
import logisticspipes.textures.provider.LPActionTriggerIconProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.gates.ITileTrigger;
import buildcraft.api.gates.ITriggerParameter;

public class TriggerNeedsPower extends LPTrigger implements ITileTrigger{

	public TriggerNeedsPower() {
		super("LogisticsPipes:trigger.nodeRequestsRecharge");
	}

	@Override
	public int getIconIndex() {
		return LPActionTriggerIconProvider.triggerPowerNeededIconIndex;
	}
	

	@Override
	public boolean hasParameter() {
		return false;
	}

	@Override
	public String getDescription() {
		return "Needs More Power";
	}
	@Override
	public boolean isTriggerActive(ForgeDirection side, TileEntity tile, ITriggerParameter parameter) {
		if(tile instanceof LogisticsPowerJunctionTileEntity) {
			LogisticsPowerJunctionTileEntity LPJTE = (LogisticsPowerJunctionTileEntity)tile;
			return LPJTE.needMorePowerTriggerCheck;
		}
		if(tile instanceof LogisticsSolderingTileEntity) {
			LogisticsSolderingTileEntity LSTE = (LogisticsSolderingTileEntity)tile;
			return LSTE.hasWork;
		}
		return false;
	}

	@Override
	public boolean requiresParameter() {
		return false;
	}	
}