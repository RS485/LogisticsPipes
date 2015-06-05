package logisticspipes.proxy.interfaces;

import net.minecraft.item.ItemStack;

public interface ICraftingParts {

	ItemStack getChipTear1(); // Iron

	ItemStack getChipTear2(); // Gold

	ItemStack getChipTear3(); // Diamond

	Object getGearTear1(); // Iron

	Object getGearTear2(); // Gold

	Object getGearTear3(); // Diamond

	Object getSortingLogic(); // Diamond Pipe

	Object getBasicTransport(); // CobbleStone Pipe

	Object getWaterProof(); // Pipe Waterproof

	Object getExtractorItem(); // Wooden Pipe

	Object getExtractorFluid(); // Fluid Wooden pipe

	Object getBlockDynamo(); // For RF Power Provider

	Object getPowerCoilSilver();

	Object getPowerCoilGold();

}
