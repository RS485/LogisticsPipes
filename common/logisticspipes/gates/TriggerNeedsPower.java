package logisticspipes.gates;


import logisticspipes.blocks.LogisticsSolderingTileEntity;
import logisticspipes.blocks.powertile.LogisticsPowerJuntionTileEntity_BuildCraft;
import logisticspipes.textures.Textures;
import net.minecraft.tileentity.TileEntity;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.core.triggers.BCTrigger;

public class TriggerNeedsPower extends BCTrigger{

	public TriggerNeedsPower(int id) {
		super(id);
	}

	@Override
	public int getIconIndex() {
		return 1 * 16 + 1 ;
	}
	

	@Override
	public boolean hasParameter() {
		return false;
	}

	@Override
	public String getDescription() {
		return "Needs More Power";
	}
/*TODO: fixme
	@Override
	public boolean isTriggerActive(TileEntity tile, ITriggerParameter parameter) {
		if(tile instanceof LogisticsPowerJuntionTileEntity_BuildCraft) {
			LogisticsPowerJuntionTileEntity_BuildCraft LPJTE = (LogisticsPowerJuntionTileEntity_BuildCraft)tile;
			return LPJTE.needMorePowerTriggerCheck;
		}
		if(tile instanceof LogisticsSolderingTileEntity) {
			LogisticsSolderingTileEntity LSTE = (LogisticsSolderingTileEntity)tile;
			return LSTE.hasWork;
		}
		return false;
	}	
	*/ 
}