/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui.modules;

import logisticspipes.modules.ModuleAdvancedExtractor;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.module.AdvancedExtractorIncludePacket;
import logisticspipes.network.packets.module.AdvancedExtractorSneakyGuiPacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiStringHandlerButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import buildcraft.transport.Pipe;

public class GuiAdvancedExtractor extends GuiWithPreviousGuiContainer {

	private final ModuleAdvancedExtractor _advancedExtractor;
	private final int slot;
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		//Default item toggle:
		buttonList.clear();
		buttonList.add(new GuiStringHandlerButton(0, width / 2 + 20, height / 2 - 34, 60, 20, new GuiStringHandlerButton.StringHandler(){
			@Override
			public String getContent() {
				return _advancedExtractor.areItemsIncluded() ? "Included" : "Excluded";
			}}));

		buttonList.add(new GuiButton(1, width / 2 - 25, height / 2 - 34, 40, 20, "Sneaky"));
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		switch(guibutton.id)
		{
			case 0:
				_advancedExtractor.setItemsIncluded(!_advancedExtractor.areItemsIncluded());
				if(slot >= 0) {
//TODO 				MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.ADVANCED_EXTRACTOR_MODULE_INCLUDED_SET, pipe.getX(), pipe.getY(), pipe.getZ(), (_advancedExtractor.areItemsIncluded() ? 1 : 0) + (slot * 10)).getPacket());
					MainProxy.sendPacketToServer(PacketHandler.getPacket(AdvancedExtractorIncludePacket.class).setInteger((_advancedExtractor.areItemsIncluded() ? 1 : 0) + (slot * 10)).setPosX(pipe.getX()).setPosY(pipe.getY()).setPosZ(pipe.getZ()));
				} else {
//TODO 				MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.ADVANCED_EXTRACTOR_MODULE_INCLUDED_SET, 0, -1, 0, (_advancedExtractor.areItemsIncluded() ? 1 : 0) + (slot * 10)).getPacket());	
					MainProxy.sendPacketToServer(PacketHandler.getPacket(AdvancedExtractorIncludePacket.class).setInteger((_advancedExtractor.areItemsIncluded() ? 1 : 0) + (slot * 10)).setPosX(0).setPosY(-1).setPosZ(0));
				}
				break;
			case 1:
				if(slot >= 0) {
//TODO 				MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.ADVANCED_EXTRACTOR_MODULE_SNEAKY_GUI, pipe.getX(), pipe.getY(), pipe.getZ(), slot).getPacket());
					MainProxy.sendPacketToServer(PacketHandler.getPacket(AdvancedExtractorSneakyGuiPacket.class).setInteger(slot).setPosX(pipe.getX()).setPosY(pipe.getY()).setPosZ(pipe.getZ()));
				} else {
//TODO 				MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.ADVANCED_EXTRACTOR_MODULE_SNEAKY_GUI, _advancedExtractor.getX(), -1, _advancedExtractor.getZ(), slot).getPacket());
					MainProxy.sendPacketToServer(PacketHandler.getPacket(AdvancedExtractorSneakyGuiPacket.class).setInteger(slot).setPosX(_advancedExtractor.getX()).setPosY(-1).setPosZ(_advancedExtractor.getZ()));
				}
				break;
		}
		
	}
	
	public GuiAdvancedExtractor(IInventory playerInventory, CoreRoutedPipe pipe, ModuleAdvancedExtractor advancedExtractor, GuiScreen previousGui, int slot) {
		super(null,pipe,previousGui);
		_advancedExtractor = advancedExtractor;
		this.slot = slot;
		DummyContainer dummy = new DummyContainer(playerInventory, _advancedExtractor.getFilterInventory());
		dummy.addNormalSlotsForPlayerInventory(8, 60);

		//Pipe slots
		for(int pipeSlot = 0; pipeSlot < 9; pipeSlot++){
			dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
		}

		this.inventorySlots = dummy;
		xSize = 175;
		ySize = 142;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		fontRenderer.drawString(_advancedExtractor.getFilterInventory().getInvName(), 8, 6, 0x404040);
		fontRenderer.drawString("Inventory", 8, ySize - 92, 0x404040);
	}
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.func_110577_a(ITEMSINK);
		int j = guiLeft;
		int k = guiTop;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Module_Advanced_Extractor_ID + (slot * 100);
	}
	
	public void setInclude(boolean flag) {
		_advancedExtractor.setItemsIncluded(flag);
	}
}
