package logisticspipes.gates;


import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import logisticspipes.blocks.LogisticsSolderingTileEntity;
import logisticspipes.blocks.powertile.LogisticsPowerJuntionTileEntity_BuildCraft;
import logisticspipes.textures.Textures;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;

import buildcraft.api.core.IIconProvider;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.core.triggers.BCTrigger;
import buildcraft.transport.Pipe;
import buildcraft.transport.TileGenericPipe;

public class TriggerNeedsPower implements ITrigger{
	int id;
	public TriggerNeedsPower(int id) {
		this.id = id;
	}
	
	@Override
	public Icon getTextureIcon()  {
		return Textures.LOGISTICSACTIONTRIGGERS_NEEDS_POWER_ICON;
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
	public boolean isTriggerActive(TileEntity tile, ITriggerParameter parameter) {
		if(!(tile instanceof TileGenericPipe))
			return false;
		Pipe pipe = ((TileGenericPipe)tile).pipe;
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

	@Override
	public int getId() {
		return id;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITriggerParameter createParameter() {
		// TODO Auto-generated method stub
		return null;
	}	
}