/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import logisticspipes.modules.ModuleActiveSupplier;
import logisticspipes.modules.ModuleActiveSupplier.PatternMode;
import logisticspipes.modules.ModuleActiveSupplier.SupplyMode;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.module.SupplierPipeLimitedPacket;
import logisticspipes.network.packets.module.SupplierPipeModePacket;
import logisticspipes.network.packets.pipe.SlotFinderOpenGuiPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.Color;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.string.StringUtils;

public class GuiSupplierPipe extends LogisticsBaseGuiScreen {

	private static final String PREFIX = "gui.supplierpipe.";

	private ModuleActiveSupplier module;
	private final boolean hasPatternUpgrade;

	public GuiSupplierPipe(IInventory playerInventory, IInventory dummyInventory, ModuleActiveSupplier module, Boolean flag, int[] slots) {
		super(null);
		hasPatternUpgrade = flag;

		DummyContainer dummy = new DummyContainer(playerInventory, dummyInventory);
		dummy.addNormalSlotsForPlayerInventory(18, 97);

		if (hasPatternUpgrade) {
			for (int i = 0; i < 9; i++) {
				dummy.addDummySlot(i, 18 + i * 18, 20);
			}
		} else {
			int xOffset = 72;
			int yOffset = 18;
			for (int row = 0; row < 3; row++) {
				for (int column = 0; column < 3; column++) {
					dummy.addDummySlot(column + row * 3, xOffset + column * 18, yOffset + row * 18);
				}
			}
		}
		inventorySlots = dummy;
		module.slotArray = slots;
		this.module = module;
		xSize = 194;
		ySize = 186;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		String name;
		if (hasPatternUpgrade) {
			name = StringUtils.translate(GuiSupplierPipe.PREFIX + "TargetInvPattern");
		} else {
			name = StringUtils.translate(GuiSupplierPipe.PREFIX + "TargetInv");
		}
		mc.fontRenderer.drawString(name, xSize / 2 - mc.fontRenderer.getStringWidth(name) / 2, 6, 0x404040);
		mc.fontRenderer.drawString(StringUtils.translate(GuiSupplierPipe.PREFIX + "Inventory"), 18, ySize - 102, 0x404040);
		mc.fontRenderer.drawString(StringUtils.translate(GuiSupplierPipe.PREFIX + "RequestMode"), xSize - 140, ySize - 112, 0x404040);
		if (hasPatternUpgrade) {
			for (int i = 0; i < 9; i++) {
				mc.fontRenderer.drawString(Integer.toString(module.slotArray[i]), 22 + i * 18, 55, 0x404040);
			}
		}
	}

	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/supplier.png");

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		if (!hasPatternUpgrade) {
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			mc.renderEngine.bindTexture(GuiSupplierPipe.TEXTURE);
			int j = guiLeft;
			int k = guiTop;
			drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
		} else {
			GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
			GL11.glTranslated(guiLeft, guiTop, 0);
			for (int i = 0; i < 9; i++) {
				GuiGraphics.drawSlotBackground(mc, 17 + i * 18, 19);
				Slot slot = inventorySlots.getSlot(36 + i);
				if (slot.getHasStack() && slot.getStack().getCount() > 64) {
					drawRect(18 + i * 18, 20, 34 + i * 18, 36, Color.RED);
				}
			}
			GuiGraphics.drawPlayerInventoryBackground(mc, 18, 97);
			GL11.glTranslated(-guiLeft, -guiTop, 0);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		buttonList.add(new GuiButton(0, width / 2 + 35, height / 2 - 25, 50, 20, (hasPatternUpgrade ? module.getPatternMode() : module.getSupplyMode()).toString()));
		if (hasPatternUpgrade) {
			buttonList.add(new SmallGuiButton(1, guiLeft + 5, guiTop + 68, 45, 10, module.isLimited() ? "Limited" : "Unlimited"));
			for (int i = 0; i < 9; i++) {
				buttonList.add(new SmallGuiButton(i + 2, guiLeft + 18 + i * 18, guiTop + 40, 17, 10, "Set"));
			}
		}
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) throws IOException {
		if (guibutton.id == 0) {
			if (hasPatternUpgrade) {
				int currentMode = module.getPatternMode().ordinal() + 1;
				if (currentMode >= PatternMode.values().length) {
					currentMode = 0;
				}
				module.setPatternMode(PatternMode.values()[currentMode]);
				buttonList.get(0).displayString = module.getPatternMode().toString();
			} else {
				int currentMode = module.getSupplyMode().ordinal() + 1;
				if (currentMode >= SupplyMode.values().length) {
					currentMode = 0;
				}
				module.setSupplyMode(SupplyMode.values()[currentMode]);
				buttonList.get(0).displayString = module.getSupplyMode().toString();
			}
			MainProxy.sendPacketToServer(PacketHandler.getPacket(SupplierPipeModePacket.class).setModulePos(module));
		} else if (hasPatternUpgrade) {
			if (guibutton.id == 1) {
				module.setLimited(!module.isLimited());
				buttonList.get(1).displayString = module.isLimited() ? "Limited" : "Unlimited";
				MainProxy.sendPacketToServer(PacketHandler.getPacket(SupplierPipeLimitedPacket.class).setLimited(module.isLimited()).setModulePos(module));
			} else if (guibutton.id >= 2 && guibutton.id <= 10) {
				MainProxy.sendPacketToServer(PacketHandler.getPacket(SlotFinderOpenGuiPacket.class).setSlot(guibutton.id - 2).setModulePos(module));
			}
		}
		super.actionPerformed(guibutton);
	}

	public void refreshMode() {
		buttonList.get(0).displayString = (hasPatternUpgrade ? module.getPatternMode() : module.getSupplyMode()).toString();
		if (hasPatternUpgrade) {
			buttonList.get(1).displayString = module.isLimited() ? "Limited" : "Unlimited";
		}
	}
}
