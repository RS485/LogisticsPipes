package logisticspipes.asm.bc;

import logisticspipes.renderer.LogisticsRenderPipe;
import buildcraft.transport.TravelingItem;

public class RenderItemInPipeHook {
	public static void renderItemInPipe(TravelingItem travellingItem, double x, double y, double z, float light) {
		if(travellingItem != null && travellingItem.getItemStack() != null && travellingItem.getItemStack().hasTagCompound()) {
			if(travellingItem.getItemStack().getTagCompound().getString("LogsitcsPipes_ITEM_ON_TRANSPORTATION").equals("YES")) {
				if(LogisticsRenderPipe.boxRenderer != null) {
					LogisticsRenderPipe.boxRenderer.doRenderItem(travellingItem.getItemStack(), light, x, y + 0.25, z);
				}
			}
		}
	}
}
