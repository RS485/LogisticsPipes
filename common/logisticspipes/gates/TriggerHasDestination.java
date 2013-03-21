package logisticspipes.gates;

import java.util.ArrayList;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.textures.Textures;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.core.triggers.BCTrigger;
import buildcraft.transport.ITriggerPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.TileGenericPipe;

public class TriggerHasDestination implements ITrigger {
	int id;
	public TriggerHasDestination(int id) {
		this.id = id;
	}
	
	@Override
	public boolean hasParameter() {
		return true;
	}
	
	@Override
	public String getDescription() {
		return "Item has destination";
	}

	@Override
	public boolean isTriggerActive(TileEntity tile, ITriggerParameter parameter) {
		if(!(tile instanceof TileGenericPipe))
			return false;
		Pipe pipe = ((TileGenericPipe)tile).pipe;
		if (pipe instanceof CoreRoutedPipe) {
			if (parameter != null && parameter.getItem() != null) {
				ItemStack item = parameter.getItem();
				if (SimpleServiceLocator.logisticsManager.hasDestination(ItemIdentifier.get(item), false, ((CoreRoutedPipe) pipe).getRouter().getSimpleID(), new ArrayList<Integer>()) != null) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Icon getTextureIcon()  {
		return Textures.LOGISTICSACTIONTRIGGERS_TEXTURE_FILE;
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
