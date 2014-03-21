/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui;

import logisticspipes.network.GuiIDs;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.routing.IRouter;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.KraphtBaseGuiScreen;
import logisticspipes.utils.item.ItemIdentifier;
import net.minecraft.entity.player.EntityPlayer;

public class GuiRoutingStats extends KraphtBaseGuiScreen {
	
	private IRouter _router;
	public GuiRoutingStats(IRouter router, EntityPlayer entityPlayer) {
		super(170, 200, 0, 0);
		_router = router;
		this.inventorySlots = new DummyContainer(entityPlayer.inventory, null);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);

		String pipeName = ItemIdentifier.get(_router.getPipe().itemID, 0, null).getFriendlyName();
		fontRendererObj.drawString(pipeName, (170 - fontRendererObj.getStringWidth(pipeName))/2, 10, 0x83601c);
		
		int sessionxCenter = 85;
		int lifetimexCenter = 130;
		
		fontRendererObj.drawString("Session", sessionxCenter - fontRendererObj.getStringWidth("Session") / 2, 40, 0x303030);
		fontRendererObj.drawString("Lifetime", lifetimexCenter - fontRendererObj.getStringWidth("Lifetime") / 2, 40, 0x303030);
		fontRendererObj.drawString("Sent:", 60 - fontRendererObj.getStringWidth("Sent:"), 55, 0x303030);
		fontRendererObj.drawString("Recieved:", 60 - fontRendererObj.getStringWidth("Recieved:"), 70, 0x303030);
		fontRendererObj.drawString("Relayed:", 60 - fontRendererObj.getStringWidth("Relayed:"), 85, 0x303030);
		CoreRoutedPipe pipe = _router.getPipe();
		fontRendererObj.drawString(pipe.stat_session_sent+"", sessionxCenter - fontRendererObj.getStringWidth(pipe.stat_session_sent+"")/2, 55, 0x303030);
		fontRendererObj.drawString(pipe.stat_session_recieved+"", sessionxCenter - fontRendererObj.getStringWidth(pipe.stat_session_recieved+"")/2, 70, 0x303030);
		fontRendererObj.drawString(pipe.stat_session_relayed+"", sessionxCenter - fontRendererObj.getStringWidth(pipe.stat_session_relayed+"")/2, 85, 0x303030);
		
		fontRendererObj.drawString(pipe.stat_lifetime_sent+"", lifetimexCenter - fontRendererObj.getStringWidth(pipe.stat_lifetime_sent+"")/2, 55, 0x303030);
		fontRendererObj.drawString(pipe.stat_lifetime_recieved+"", lifetimexCenter - fontRendererObj.getStringWidth(pipe.stat_lifetime_recieved+"")/2, 70, 0x303030);
		fontRendererObj.drawString(pipe.stat_lifetime_relayed+"", lifetimexCenter - fontRendererObj.getStringWidth(pipe.stat_lifetime_relayed+"")/2, 85, 0x303030);
		
		fontRendererObj.drawString("RoutingTableSize:", 110 - fontRendererObj.getStringWidth("RoutingTableSize:"), 120, 0x303030);
				
		fontRendererObj.drawString(pipe.server_routing_table_size+"", 130 - fontRendererObj.getStringWidth(pipe.server_routing_table_size+"")/2, 120, 0x303030);
				
		
		String escString = "Press <ESC> to exit"; 
		fontRendererObj.drawString(escString, (170 - fontRendererObj.getStringWidth(escString)) / 2, 180, 0x404040);
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_RoutingStats_ID;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
	}

}
