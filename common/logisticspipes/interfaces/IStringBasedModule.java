package logisticspipes.interfaces;

import java.util.List;

import net.minecraft.nbt.CompoundTag;

import logisticspipes.utils.item.ItemIdentifier;

public interface IStringBasedModule {

	List<String> getStringList();

	String getStringForItem(ItemIdentifier ident);

	void listChanged();

	void readFromNBT(CompoundTag nbt);
}
