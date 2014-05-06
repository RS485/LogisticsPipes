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
import logisticspipes.utils.string.StringUtil;
import net.minecraft.entity.player.EntityPlayer;

public class GuiRoutingStats extends KraphtBaseGuiScreen {
	private static final String PREFIX = "gui.routingstats.";
	
	private IRouter _router;
	public GuiRoutingStats(IRouter router, EntityPlayer entityPlayer) {
		super(170, 200, 0, 0);
		_router = router;
		this.inventorySlots = new DummyContainer(entityPlayer.inventory, null);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);

		String pipeName = ItemIdentifier.get(_router.getPipe().item, 0, null).getFriendlyName();
		mc.fontRenderer.drawString(pipeName, (170 - mc.fontRenderer.getStringWidth(pipeName))/2, 10, 0x83601c);
		
		int sessionxCenter = 85;
		int lifetimexCenter = 130;
		
		mc.fontRenderer.drawString(StringUtil.translate(PREFIX + "Session"), sessionxCenter - mc.fontRenderer.getStringWidth(StringUtil.translate(PREFIX + "Session")) / 2, 40, 0x303030);
		mc.fontRenderer.drawString(StringUtil.translate(PREFIX + "Lifetime"), lifetimexCenter - mc.fontRenderer.getStringWidth(StringUtil.translate(PREFIX + "Lifetime")) / 2, 40, 0x303030);
		mc.fontRenderer.drawString(StringUtil.translate(PREFIX + "Sent") + ":", 60 - mc.fontRenderer.getStringWidth(StringUtil.translate(PREFIX + "Sent") + ":"), 55, 0x303030);
		mc.fontRenderer.drawString(StringUtil.translate(PREFIX + "Recieved") + ":", 60 - mc.fontRenderer.getStringWidth(StringUtil.translate(PREFIX + "Recieved") + ":"), 70, 0x303030);
		mc.fontRenderer.drawString(StringUtil.translate(PREFIX + "Relayed") + ":", 60 - mc.fontRenderer.getStringWidth(StringUtil.translate(PREFIX + "Relayed") + ":"), 85, 0x303030);
		CoreRoutedPipe pipe = _router.getPipe();
		mc.fontRenderer.drawString(pipe.stat_session_sent+"", sessionxCenter - mc.fontRenderer.getStringWidth(pipe.stat_session_sent+"")/2, 55, 0x303030);
		mc.fontRenderer.drawString(pipe.stat_session_recieved+"", sessionxCenter - mc.fontRenderer.getStringWidth(pipe.stat_session_recieved+"")/2, 70, 0x303030);
		mc.fontRenderer.drawString(pipe.stat_session_relayed+"", sessionxCenter - mc.fontRenderer.getStringWidth(pipe.stat_session_relayed+"")/2, 85, 0x303030);
		
		mc.fontRenderer.drawString(pipe.stat_lifetime_sent+"", lifetimexCenter - mc.fontRenderer.getStringWidth(pipe.stat_lifetime_sent+"")/2, 55, 0x303030);
		mc.fontRenderer.drawString(pipe.stat_lifetime_recieved+"", lifetimexCenter - mc.fontRenderer.getStringWidth(pipe.stat_lifetime_recieved+"")/2, 70, 0x303030);
		mc.fontRenderer.drawString(pipe.stat_lifetime_relayed+"", lifetimexCenter - mc.fontRenderer.getStringWidth(pipe.stat_lifetime_relayed+"")/2, 85, 0x303030);
		
		mc.fontRenderer.drawString(StringUtil.translate(PREFIX + "RoutingTableSize") + ":", 110 - mc.fontRenderer.getStringWidth(StringUtil.translate(PREFIX + "RoutingTableSize") + ":"), 120, 0x303030);
				
		mc.fontRenderer.drawString(pipe.server_routing_table_size+"", 130 - mc.fontRenderer.getStringWidth(pipe.server_routing_table_size+"")/2, 120, 0x303030);
				
		
		String escString = StringUtil.translate(PREFIX + "exit"); 
		mc.fontRenderer.drawString(escString, (170 - mc.fontRenderer.getStringWidth(escString)) / 2, 180, 0x404040);
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
