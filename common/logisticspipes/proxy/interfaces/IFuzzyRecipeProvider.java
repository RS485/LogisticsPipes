package logisticspipes.proxy.interfaces;

import net.minecraft.tileentity.TileEntity;

import network.rs485.logisticspipes.inventory.SlotAccess;
import network.rs485.logisticspipes.property.BitSetProperty;

public interface IFuzzyRecipeProvider extends ICraftingRecipeProvider {

	void importFuzzyFlags(TileEntity tile, SlotAccess slotAccess, BitSetProperty fuzzyFlags);

}
