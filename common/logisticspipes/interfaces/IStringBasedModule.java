package logisticspipes.interfaces;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import logisticspipes.utils.item.ItemIdentifier;

public interface IStringBasedModule {
	public List<String> getStringList();
	public String getStringForItem(ItemIdentifier ident);
	public void listChanged();
	public void readFromNBT(NBTTagCompound nbt);
}
