package logisticspipes.gates;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.textures.Textures;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.core.triggers.BCTrigger;
import buildcraft.transport.ITriggerPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.TileGenericPipe;

public class TriggerCrafting implements ITrigger {
	int id;
	public TriggerCrafting(int id) {
		this.id=id;
	}

	@Override
	public boolean isTriggerActive(TileEntity tile, ITriggerParameter parameter) {
		if(!(tile instanceof TileGenericPipe))
			return false;
		Pipe pipe = ((TileGenericPipe)tile).pipe;
		if (!(pipe instanceof PipeItemsCraftingLogistics)) return false;
		return ((PipeItemsCraftingLogistics)pipe).waitingForCraft;
	}

	@Override
	public String getDescription() {
		return "Pipe Waiting for Crafting";
	}

	@Override
	public Icon getTextureIcon()  {
		return Textures.LOGISTICSACTIONTRIGGERS_CRAFTING_ICON;
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
	public boolean hasParameter() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ITriggerParameter createParameter() {
		// TODO Auto-generated method stub
		return null;
	}

}
