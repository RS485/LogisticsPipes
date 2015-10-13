package logisticspipes.proxy.buildcraft;

import buildcraft.api.core.EnumColor;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.render.PipeTransportItemsRenderer;
import logisticspipes.renderer.LogisticsRenderPipe;

public class BCLPPipeTransportItemsRenderer extends PipeTransportItemsRenderer {
	@Override
	public void doRenderItem(TravelingItem travellingItem, double x, double y, double z, float light, EnumColor color) {
		if (travellingItem != null && travellingItem.getItemStack() != null && travellingItem.getItemStack().hasTagCompound()) {
			if (travellingItem.getItemStack().getTagCompound().getString("LogsitcsPipes_ITEM_ON_TRANSPORTATION").equals("YES")) {
				if (LogisticsRenderPipe.boxRenderer != null) {
					LogisticsRenderPipe.boxRenderer.doRenderItem(travellingItem.getItemStack(), light, x, y + 0.25, z, 1.0D);
				}
			}
		}
		super.doRenderItem(travellingItem, x, y, z, light, color);
	}
}
