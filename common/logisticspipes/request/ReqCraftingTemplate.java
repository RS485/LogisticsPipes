package logisticspipes.request;

import net.minecraft.item.ItemStack;

import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import network.rs485.logisticspipes.routing.request.Resource;

public interface ReqCraftingTemplate extends CraftingTemplate {

	void addRequirement(Resource requirement, IAdditionalTargetInformation info);

	void addByproduct(ItemStack byproductItem);
}
