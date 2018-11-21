package logisticspipes.proxy.buildcraft;

import net.minecraft.item.ItemStack;

import buildcraft.transport.pipe.flow.TravellingItem;
import lombok.Getter;
import lombok.Setter;

import logisticspipes.routing.ItemRoutingInformation;

public class LPRoutedBCTravelingItem extends TravellingItem {

	public LPRoutedBCTravelingItem(ItemStack stack) {
		super(stack);
	}

	@Getter
	@Setter
	private ItemRoutingInformation routingInformation;

	@Override
	public boolean canMerge(TravellingItem with) {
		return false;
	}
}