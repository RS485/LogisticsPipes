package logisticspipes.gates;

import java.util.ArrayList;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.textures.Textures;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.item.ItemStack;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.core.triggers.BCTrigger;
import buildcraft.transport.ITriggerPipe;
import buildcraft.transport.Pipe;

public class TriggerHasDestination extends BCTrigger implements ITriggerPipe {

	public TriggerHasDestination(int id) {
		super(id);
	}

	@Override
	public int getIconIndex() {
		return 2 * 16 + 0;
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
	public boolean isTriggerActive(Pipe pipe, ITriggerParameter parameter) {
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

	
}
