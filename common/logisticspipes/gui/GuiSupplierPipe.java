/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui;

import logisticspipes.interfaces.IGuiIDHandlerProvider;
import logisticspipes.logic.LogicSupplier;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.SupPipeNextMode;
import logisticspipes.network.packets.abstracts.ModernPacket;
import logisticspipes.network.packets.old.PacketCoordinates;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyContainer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.IInventory;

import org.lwjgl.opengl.GL11;

public class GuiSupplierPipe extends GuiContainer implements IGuiIDHandlerProvider {
	
	private IInventory dummyInventory;
	private LogicSupplier logic; 
	
	public GuiSupplierPipe(IInventory playerInventory, IInventory dummyInventory, LogicSupplier logic) {
		super(null);
		
		
		DummyContainer dummy = new DummyContainer(playerInventory, dummyInventory);
		dummy.addNormalSlotsForPlayerInventory(18, 97);
		
		int xOffset = 72;
		int yOffset = 18;
		
		for (int row = 0; row < 3; row++){
			for (int column = 0; column < 3; column++){
				dummy.addDummySlot(column + row * 3, xOffset + column * 18, yOffset + row * 18);					
			}
		}
		this.inventorySlots = dummy; 

		this.dummyInventory = dummyInventory;
		this.logic = logic;
		xSize = 194;
		ySize = 186;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		fontRenderer.drawString(dummyInventory.getInvName(), xSize / 2 - fontRenderer.getStringWidth(dummyInventory.getInvName())/2, 6, 0x404040);
		fontRenderer.drawString("Inventory", 18, ySize - 102, 0x404040);
		fontRenderer.drawString("Partial requests:", xSize - 140, ySize - 112, 0x404040);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture("/logisticspipes/gui/supplier.png");
		int j = guiLeft;
		int k = guiTop;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		buttonList.add(new GuiButton(0, width / 2 + 45, height / 2 - 25, 30, 20, logic.isRequestingPartials() ? "Yes" : "No"));
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		// TODO Auto-generated method stub
		if (guibutton.id == 0){
			logic.setRequestingPartials(!logic.isRequestingPartials());
			((GuiButton)buttonList.get(0)).displayString = logic.isRequestingPartials() ? "Yes" : "No";
			final ModernPacket packet = PacketHandler.getPacket(SupPipeNextMode.class).setPosX(logic.xCoord).setPosY(logic.yCoord).setPosZ(logic.zCoord);
			MainProxy.sendPacketToServer(packet.getPacket());
		}
		super.actionPerformed(guibutton);
		
	}
	
	public void refreshMode() {
		((GuiButton)buttonList.get(0)).displayString = logic.isRequestingPartials() ? "Yes" : "No";
	}
	
	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		logic.pause = false;
		
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_SupplierPipe_ID;
	}

}
