package logisticspipes.logisticspipes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import logisticspipes.interfaces.IClientInformationProvider;
import logisticspipes.modules.LogisticsModule;
import logisticspipes.proxy.MainProxy;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

public class ItemModuleInformationManager {
	
	private static final List<String> Filter = new ArrayList<String>();
	static {
		Filter.add("moduleInformation");
		Filter.add("informationList");
		Filter.add("Random-Stack-Prevent");
	}
	
	public static void saveInfotmation(ItemStack itemStack, LogisticsModule module) {
		if(module == null) return;
		NBTTagCompound nbt = new NBTTagCompound();
        module.writeToNBT(nbt);
        if(nbt.equals(new NBTTagCompound())) {
        	return;
        }
        if(MainProxy.isClient()) {
			 NBTTagList list = new NBTTagList();
			String info1 = "Please reopen the window";
			String info2 = "to see the information.";
    		list.appendTag(new NBTTagString(null ,info1));
    		list.appendTag(new NBTTagString(null ,info2));
    		if(!itemStack.hasTagCompound()) {
            	itemStack.setTagCompound(new NBTTagCompound("tag"));
            }
    		NBTTagCompound stacktag = itemStack.getTagCompound();
    		stacktag.setTag("informationList", list);
    		stacktag.setDouble("Random-Stack-Prevent", new Random().nextDouble());
    		return;
		}
        if(!itemStack.hasTagCompound()) {
        	itemStack.setTagCompound(new NBTTagCompound("tag"));
        }
        NBTTagCompound stacktag = itemStack.getTagCompound();
        stacktag.setCompoundTag("moduleInformation", nbt);
        if(module instanceof IClientInformationProvider) {
        	List<String> information = ((IClientInformationProvider)module).getClientInformation();
        	if(information.size() > 0) {
        		NBTTagList list = new NBTTagList();
        		for(String info:information) {
        			list.appendTag(new NBTTagString(null ,info));
        		}
        		stacktag.setTag("informationList", list);
        	}
        }
		stacktag.setDouble("Random-Stack-Prevent", new Random().nextDouble());
	}
	
	public static void readInformation(ItemStack itemStack, LogisticsModule module) {
		if(module == null) return;
		if(itemStack.hasTagCompound()) {
			NBTTagCompound nbt = itemStack.getTagCompound();
			if(nbt.hasKey("moduleInformation")) {
				NBTTagCompound moduleInformation = nbt.getCompoundTag("moduleInformation");
				module.readFromNBT(moduleInformation);
			}
			
		}
	}
	
	public static void removeInformation(ItemStack itemStack) {
		if(itemStack == null) return;
		if(itemStack.hasTagCompound()) {
			NBTTagCompound nbt = itemStack.getTagCompound();
			Collection<?> collection = nbt.getTags();
			nbt = new NBTTagCompound("tag");
			for(Object obj:collection) {
				if(obj instanceof NBTBase) {
					if(!Filter.contains(((NBTBase)obj).getName())) {
						nbt.setTag(((NBTBase)obj).getName(), ((NBTBase)obj));
					}
				}
			}
			itemStack.setTagCompound(nbt);
		}
	}
}
