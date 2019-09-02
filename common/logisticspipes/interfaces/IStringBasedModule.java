package logisticspipes.interfaces;

import java.util.List;

import logisticspipes.utils.item.ItemIdentifier;

import net.minecraft.nbt.NBTTagCompound;

public interface IStringBasedModule {

	List<String> getStringList();

	String getStringForItem(ItemIdentifier ident);

	void listChanged();

	void readFromNBT(NBTTagCompound nbt);
}
