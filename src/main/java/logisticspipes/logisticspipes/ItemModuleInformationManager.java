package logisticspipes.logisticspipes;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.proxy.MainProxy;

public class ItemModuleInformationManager {

	public static void saveInformation(@Nonnull ItemStack stack, LogisticsModule module) {
		if (module == null) {
			return;
		}
		NBTTagCompound nbt = new NBTTagCompound();
		module.writeToNBT(nbt);
		if (nbt.equals(new NBTTagCompound())) {
			return;
		}
		if (MainProxy.isClient()) {
			NBTTagList list = new NBTTagList();
			String info1 = "Please reopen the window";
			String info2 = "to see the information.";
			list.appendTag(new NBTTagString(info1));
			list.appendTag(new NBTTagString(info2));
			if (!stack.hasTagCompound()) {
				stack.setTagCompound(new NBTTagCompound());
			}
			NBTTagCompound tag = Objects.requireNonNull(stack.getTagCompound());
			tag.setTag("informationList", list);
			tag.setDouble("Random-Stack-Prevent", new Random().nextDouble());
			return;
		}
		if (!stack.hasTagCompound()) {
			stack.setTagCompound(new NBTTagCompound());
		}
		NBTTagCompound tag = Objects.requireNonNull(stack.getTagCompound());
		tag.setTag("moduleInformation", nbt);
		if (module instanceof IClientInformationProvider) {
			List<String> information = ((IClientInformationProvider) module).getClientInformation();
			if (information.size() > 0) {
				NBTTagList list = new NBTTagList();
				for (String info : information) {
					list.appendTag(new NBTTagString(info));
				}
				tag.setTag("informationList", list);
			}
		}
		tag.setDouble("Random-Stack-Prevent", new Random().nextDouble());
	}

	public static void readInformation(@Nonnull ItemStack stack, LogisticsModule module) {
		if (module == null) {
			return;
		}
		if (stack.hasTagCompound()) {
			NBTTagCompound nbt = Objects.requireNonNull(stack.getTagCompound());
			if (nbt.hasKey("moduleInformation")) {
				NBTTagCompound moduleInformation = nbt.getCompoundTag("moduleInformation");
				module.readFromNBT(moduleInformation);
			}
		}
	}
}
