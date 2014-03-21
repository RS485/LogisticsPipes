package logisticspipes.asm;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import logisticspipes.LogisticsPipes;
import logisticspipes.proxy.SimpleServiceLocator;
import net.minecraft.item.ItemStack;
import buildcraft.transport.TravelerSet;
import buildcraft.transport.TravelingItem;


public class LogisticsASMHookClass {
	
	public static Field toLoad;
	
	public static void callingClearedMethod() {
		throw new RuntimeException("This Method should never be called");
	}
	
	public static void clearInvalidFluidContainers(TravelerSet items) {
		try {
        	if(toLoad == null) {
        		toLoad = TravelerSet.class.getDeclaredField("toLoad");
        		toLoad.setAccessible(true);
        	}
        	HashSet<TravelingItem> toLoadSet = (HashSet<TravelingItem>) toLoad.get(items);
        	Iterator<TravelingItem> iterator = toLoadSet.iterator();
        	while(iterator.hasNext()) {
        		TravelingItem item = iterator.next();
        		ItemStack stack = item.getItemStack();
        		if(stack != null && SimpleServiceLocator.logisticsFluidManager.getFluidFromContainer(stack) == null) {
        			iterator.remove();
        		}
        	}
        } catch(Exception e) {
        	LogisticsPipes.log.severe(e.getMessage());
        	LogisticsPipes.log.severe(Arrays.toString(e.getStackTrace()));
        	e.printStackTrace();
        }
	}
}
