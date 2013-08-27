/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui.modules;

import logisticspipes.modules.ModuleElectricManager;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.module.ElectricManagerPacket;
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

public class GuiElectricManager extends GuiWithPreviousGuiContainer {

	private final ModuleElectricManager _module;
	private final int slot;


	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
	    //Default item toggle:
		buttonList.clear();
		buttonList.add(new GuiStringHandlerButton(0, width / 2 - 6, height / 2 - 34, 88, 20, new GuiStringHandlerButton.StringHandler() {
			@Override
			public String getContent() {
				return _module.isDischargeMode() ? "Discharge Items" : "Charge Items";
			}
		}));
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		switch(guibutton.id)
		{
			case 0:
				_module.setDischargeMode(!_module.isDischargeMode());
				if(slot >= 0) {
//TODO 				MainProxy.sendPacketToServer(new PacketModuleInteger(NetworkConstants.ELECTRIC_MANAGER_SET, pipe.getX(), pipe.getY(), pipe.getZ(), slot - 1, (_module.isDischargeMode() ? 1 : 0)).getPacket());
					MainProxy.sendPacketToServer(PacketHandler.getPacket(ElectricManagerPacket.class).setInteger2(slot - 1).setInteger((_module.isDischargeMode() ? 1 : 0)).setPosX(pipe.getX()).setPosY(pipe.getY()).setPosZ(pipe.getZ()));
				} else {
//TODO 				MainProxy.sendPacketToServer(new PacketModuleInteger(NetworkConstants.ELECTRIC_MANAGER_SET, _module.getX(), _module.getY(), _module.getZ(), slot, (_module.isDischargeMode() ? 1 : 0)).getPacket());
					MainProxy.sendPacketToServer(PacketHandler.getPacket(ElectricManagerPacket.class).setInteger2(slot).setInteger((_module.isDischargeMode() ? 1 : 0)).setPosX(_module.getX()).setPosY(_module.getY()).setPosZ(_module.getZ()));
				}
				break;
		}
	}

	public GuiElectricManager(IInventory playerInventory, CoreRoutedPipe pipe, ModuleElectricManager module, GuiScreen previousGui, int slot) {
		super(null,pipe,previousGui);
		_module = module;
		this.slot = slot;
		DummyContainer dummy = new DummyContainer(playerInventory, _module.getFilterInventory());
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
		fontRenderer.drawString(_module.getFilterInventory().getInvName(), 8, 6, 0x404040);
		fontRenderer.drawString("Inventory", 8, ySize - 92, 0x404040);
	}
	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/itemsink.png");
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
		return GuiIDs.GUI_Module_ElectricManager_ID;
	}
}
