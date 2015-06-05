package logisticspipes.recipes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

public enum CraftingDependency {
	Basic,
	DistanceRequest(Basic),
	Passthrough(DistanceRequest),
	Security(Basic),
	Information_System(DistanceRequest),
	Advanced_Information(Information_System),
	Fast_Crafting(DistanceRequest),
	Crafting_Master(Fast_Crafting),
	Modular_Pipes(DistanceRequest),
	Large_Chasie(Modular_Pipes),
	Mod_Modules(Modular_Pipes),
	Sink_Modules(Modular_Pipes),
	Active_Modules(Modular_Pipes),
	High_Tech_Modules(Active_Modules),
	Upgrades(DistanceRequest),
	Basic_Liquid(DistanceRequest),
	Active_Liquid(Upgrades, Basic_Liquid),
	Power_Distribution(Upgrades);

	private CraftingDependency[] dependencies;
	private List<ItemStack> results = new ArrayList<ItemStack>();

	private CraftingDependency(CraftingDependency... dependencies) {
		this.dependencies = dependencies;
	}

	public CraftingDependency[] getDependencies() {
		return dependencies;
	}

	public void addStack(ItemStack stack) {
		results.add(stack.copy());
	}
}
