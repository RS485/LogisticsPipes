/*
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui.modules;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.IInventory;

import org.lwjgl.opengl.GL11;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.module.AdvancedExtractorSneakyGuiPacket;
import logisticspipes.network.packets.module.ModulePropertiesUpdate;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiStringHandlerButton;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import network.rs485.logisticspipes.module.AsyncAdvancedExtractor;
import network.rs485.logisticspipes.property.BooleanProperty;
import network.rs485.logisticspipes.property.PropertyLayer;

public class GuiAdvancedExtractor extends ModuleBaseGui {

	private final AsyncAdvancedExtractor _advancedExtractor;
	private final PropertyLayer propertyLayer;
	private final PropertyLayer.ValuePropertyOverlay<Boolean, BooleanProperty> itemsIncludedOverlay;

	public GuiAdvancedExtractor(IInventory playerInventory, AsyncAdvancedExtractor advancedExtractor) {
		super(null, advancedExtractor);
		_advancedExtractor = advancedExtractor;

		propertyLayer = new PropertyLayer(_advancedExtractor.getProperties());

		itemsIncludedOverlay = propertyLayer.overlay(_advancedExtractor.getItemsIncluded());

		DummyContainer dummy = new DummyContainer(playerInventory, _advancedExtractor.getFilterInventory());
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
	public void initGui() {
		super.initGui();
		//Default item toggle:
		buttonList.clear();
		buttonList.add(new GuiStringHandlerButton(0, width / 2 + 20, height / 2 - 34, 60, 20,
				() -> itemsIncludedOverlay.get() ? "Included" : "Excluded"));

		buttonList.add(new GuiButton(1, width / 2 - 25, height / 2 - 34, 40, 20, "Sneaky"));
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		propertyLayer.unregister();
		if (this.mc.player != null && !propertyLayer.getProperties().isEmpty()) {
			// send update to server, when there are changed properties
			MainProxy.sendPacketToServer(ModulePropertiesUpdate.fromPropertyHolder(propertyLayer).setModulePos(module));
		}
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		switch (guibutton.id) {
			case 0:
				itemsIncludedOverlay.write(BooleanProperty::toggle);
				break;
			case 1:
				MainProxy.sendPacketToServer(PacketHandler.getPacket(AdvancedExtractorSneakyGuiPacket.class)
						.setModulePos(_advancedExtractor));
				break;
		}

	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		mc.fontRenderer.drawString(_advancedExtractor.getFilterInventory().getName(), 8, 6, 0x404040);
		mc.fontRenderer.drawString("Inventory", 8, ySize - 92, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(LogisticsBaseGuiScreen.ITEMSINK);
		int j = guiLeft;
		int k = guiTop;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}

}
