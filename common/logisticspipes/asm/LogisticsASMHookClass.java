package logisticspipes.asm;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import logisticspipes.Configs;
import logisticspipes.LogisticsPipes;
import logisticspipes.items.LogisticsFluidContainer;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.item.ItemIdentifierStack;
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
        	@SuppressWarnings("unchecked")
			HashSet<TravelingItem> toLoadSet = (HashSet<TravelingItem>) toLoad.get(items);
        	Iterator<TravelingItem> iterator = toLoadSet.iterator();
        	while(iterator.hasNext()) {
        		TravelingItem item = iterator.next();
        		ItemStack stack = item.getItemStack();
        		if(stack != null && stack.getItem() instanceof LogisticsFluidContainer && SimpleServiceLocator.logisticsFluidManager.getFluidFromContainer(ItemIdentifierStack.getFromStack(stack)) == null) {
        			iterator.remove();
        		}
        	}
        } catch(Exception e) {
        	LogisticsPipes.log.severe(e.getMessage());
        	LogisticsPipes.log.severe(Arrays.toString(e.getStackTrace()));
        	e.printStackTrace();
        }
	}
	
	public static String getCrashReportAddition() {
		StringBuilder string = new StringBuilder();
		if(Configs.TE_PIPE_SUPPORT) {
			string.append("YOU HAVE ENABLED THE LP SUPPORT FOR TE CONDUITS.");
			string.append("\n");
			string.append("DON'T REPORT BUGS TO TE IN THIS CONFIGURATION. LP MODIFIES TE, SO THEY COULD BE CAUSED BY LP.");
			string.append("\n");
			string.append("DISABLE THE SUPPORT AND TRY TO REPRODUCE THE BUG.");
			string.append("\n");
			string.append("IF YOU CAN ONLY REPRODUCE THE BUG WITH THE SUPPORT ENABLED PLEASE REPORT IT TO LP.");
			string.append("\n\n");
		}
		return string.toString();
	}
}
