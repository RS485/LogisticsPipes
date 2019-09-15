package logisticspipes.request;

import java.util.List;

import net.minecraft.item.ItemStack;

import logisticspipes.interfaces.routing.Crafter;
import logisticspipes.interfaces.routing.IAdditionalTargetInformation;
import logisticspipes.interfaces.routing.ItemCrafter;
import logisticspipes.utils.tuples.Tuple2;
import network.rs485.logisticspipes.routing.request.Resource;

public interface CraftingTemplate extends Comparable<CraftingTemplate> {

	List<Tuple2<Resource, IAdditionalTargetInformation>> getComponents(int nCraftingSets);

	List<ExtraPromise> getByproducts(int workSets);

	int getResultStackSize();

	Promise generatePromise(int nCraftingSetsNeeded);

	Crafter getCrafter();

	int getPriority();

	boolean canCraft(Resource requestType);

	Resource getResultItem();

	int comparePriority(int priority);

	int compareStack(ItemStack stack);

	int compareCrafter(ItemCrafter crafter);
}
