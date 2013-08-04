package logisticspipes.gates;

import java.util.ArrayList;

import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.textures.provider.LPActionTriggerIconProvider;
import logisticspipes.utils.ItemIdentifier;
import net.minecraft.item.ItemStack;
import buildcraft.api.gates.ITriggerParameter;
import buildcraft.transport.ITriggerPipe;
import buildcraft.transport.Pipe;

public class TriggerHasDestination extends LPTrigger implements ITriggerPipe {

	public TriggerHasDestination(int id) {
		super(id,"LogisticsPipes.trigger.hasDestination");
	}

	@Override
	public int getIconIndex() {
		return LPActionTriggerIconProvider.triggerHasDestinationIconIndex;
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
