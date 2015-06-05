/**
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui.modules;

import logisticspipes.modules.ModuleElectricManager;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.module.ElectricManagerPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiStringHandlerButton;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class GuiElectricManager extends ModuleBaseGui {

	private final ModuleElectricManager _module;

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
		switch (guibutton.id) {
			case 0:
				_module.setDischargeMode(!_module.isDischargeMode());
				MainProxy.sendPacketToServer(PacketHandler.getPacket(ElectricManagerPacket.class).setFlag(_module.isDischargeMode()).setModulePos(_module));
				break;
		}
	}

	public GuiElectricManager(IInventory playerInventory, ModuleElectricManager module) {
		super(null, module);
		_module = module;
		DummyContainer dummy = new DummyContainer(playerInventory, _module.getFilterInventory());
		dummy.addNormalSlotsForPlayerInventory(8, 60);

		//Pipe slots
		for (int pipeSlot = 0; pipeSlot < 9; pipeSlot++) {
			dummy.addDummySlot(pipeSlot, 8 + pipeSlot * 18, 18);
		}
		inventorySlots = dummy;
		xSize = 175;
		ySize = 142;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		mc.fontRenderer.drawString(_module.getFilterInventory().getInventoryName(), 8, 6, 0x404040);
		mc.fontRenderer.drawString("Inventory", 8, ySize - 92, 0x404040);
	}

	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/itemsink.png");

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(GuiElectricManager.TEXTURE);
		int j = guiLeft;
		int k = guiTop;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}
}
