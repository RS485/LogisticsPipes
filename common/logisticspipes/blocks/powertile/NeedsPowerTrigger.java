package logisticspipes.blocks.powertile;


import logisticspipes.textures.Textures;
import net.minecraft.tileentity.TileEntity;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.api.gates.Trigger;

public class NeedsPowerTrigger extends Trigger{

	public NeedsPowerTrigger(int id) {
		super(id);
	}

	@Override
	public int getIndexInTexture() {
		// TODO Auto-generated method stub
		return 1 * 16 + 1 ;
	}
	
	@Override
	public String getTextureFile() {
		// TODO Auto-generated method stub
		return Textures.LOGISTICSACTIONTRIGGERS_TEXTURE_FILE;
	}

	@Override
	public boolean hasParameter() {
		return false;
	}

	@Override
	public String getDescription() {
		return "LogisticsNetworkNeedsMorePower";
	}

	@Override
	public boolean isTriggerActive(TileEntity tile,
			ITriggerParameter parameter) {
		if(tile instanceof LogisticsPowerJuntionTileEntity_BuildCraft){
			LogisticsPowerJuntionTileEntity_BuildCraft LPJTE= (LogisticsPowerJuntionTileEntity_BuildCraft)tile;
			return LPJTE.needMorePowerTriggerCheck;
		}
		return false;
	}	
}