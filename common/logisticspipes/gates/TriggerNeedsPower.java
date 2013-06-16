package logisticspipes.gates;

import logisticspipes.blocks.LogisticsSolderingTileEntity;
import logisticspipes.blocks.powertile.LogisticsPowerJuntionTileEntity;
import logisticspipes.textures.provider.LPActionTriggerIconProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.api.gates.ITriggerParameter;

public class TriggerNeedsPower extends LPTrigger{

	public TriggerNeedsPower(int id) {
		super(id);
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
		if(tile instanceof LogisticsPowerJuntionTileEntity) {
			LogisticsPowerJuntionTileEntity LPJTE = (LogisticsPowerJuntionTileEntity)tile;
			return LPJTE.needMorePowerTriggerCheck;
		}
		if(tile instanceof LogisticsSolderingTileEntity) {
			LogisticsSolderingTileEntity LSTE = (LogisticsSolderingTileEntity)tile;
			return LSTE.hasWork;
		}
		return false;
	}	
}