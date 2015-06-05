package logisticspipes.interfaces;

import java.util.List;

import logisticspipes.utils.item.ItemIdentifier;

import net.minecraft.nbt.NBTTagCompound;

public interface IStringBasedModule {

	public List<String> getStringList();

	public String getStringForItem(ItemIdentifier ident);

	public void listChanged();

	public void readFromNBT(NBTTagCompound nbt);
}
