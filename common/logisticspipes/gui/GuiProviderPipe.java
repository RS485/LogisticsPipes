/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui;

import logisticspipes.interfaces.IGuiIDHandlerProvider;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.module.ProviderPipeIncludePacket;
import logisticspipes.network.packets.module.ProviderPipeNextModePacket;
import logisticspipes.pipes.PipeItemsProviderLogistics;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyContainer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class GuiProviderPipe extends GuiContainer implements IGuiIDHandlerProvider{
	private IInventory dummyInventory;
	private PipeItemsProviderLogistics logic;
	
	public GuiProviderPipe(IInventory playerInventory, IInventory dummyInventory, PipeItemsProviderLogistics logic) {
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
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
       buttonList.clear();
       buttonList.add(new GuiButton(0, width / 2 + 40, height / 2 - 59, 45, 20, logic.isExcludeFilter() ? "Exclude" : "Include"));
       buttonList.add(new GuiButton(1, width / 2 - 90, height / 2 - 41, 38, 20, "Switch"));
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton.id == 0){
			logic.setFilterExcluded(!logic.isExcludeFilter());
			((GuiButton)buttonList.get(0)).displayString = logic.isExcludeFilter() ? "Exclude" : "Include";
//TODO 		MainProxy.sendPacketToServer(new PacketCoordinates(NetworkConstants.PROVIDER_PIPE_CHANGE_INCLUDE, logic.getX(), logic.getY(), logic.getZ()).getPacket());
			MainProxy.sendPacketToServer(PacketHandler.getPacket(ProviderPipeIncludePacket.class).setPosX(logic.getX()).setPosY(logic.getY()).setPosZ(logic.getZ()));
		} else if (guibutton.id  == 1){
			logic.nextExtractionMode();
//TODO 		MainProxy.sendPacketToServer(new PacketCoordinates(NetworkConstants.PROVIDER_PIPE_NEXT_MODE, logic.getX(), logic.getY(), logic.getZ()).getPacket());
			MainProxy.sendPacketToServer(PacketHandler.getPacket(ProviderPipeNextModePacket.class).setPosX(logic.getX()).setPosY(logic.getY()).setPosZ(logic.getZ()));
		}
		super.actionPerformed(guibutton);
	}
	
	public void refreshInclude() {
		((GuiButton)buttonList.get(0)).displayString = logic.isExcludeFilter() ? "Exclude" : "Include";
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		fontRenderer.drawString(dummyInventory.getInvName(), xSize / 2 - fontRenderer.getStringWidth(dummyInventory.getInvName())/2, 6, 0x404040);
		fontRenderer.drawString("Inventory", 18, ySize - 102, 0x404040);
		fontRenderer.drawString("Mode: " + logic.getExtractionMode().getExtractionModeString(), 9, ySize - 112, 0x404040);
	}
	
	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/supplier.png");
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.func_110577_a(TEXTURE);
		int j = guiLeft;
		int k = guiTop;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_ProviderPipe_ID;
	}
	
}
