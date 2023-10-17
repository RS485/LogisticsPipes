/*
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui;

import java.io.IOException;
import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import logisticspipes.modules.ModuleActiveSupplier;
import logisticspipes.modules.ModuleActiveSupplier.PatternMode;
import logisticspipes.modules.ModuleActiveSupplier.SupplyMode;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.module.ModulePropertiesUpdate;
import logisticspipes.network.packets.pipe.SlotFinderOpenGuiPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.Color;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import logisticspipes.utils.gui.SmallGuiButton;
import network.rs485.logisticspipes.property.BooleanProperty;
import network.rs485.logisticspipes.property.EnumProperty;
import network.rs485.logisticspipes.property.IntListProperty;
import network.rs485.logisticspipes.property.PropertyLayer;
import network.rs485.logisticspipes.util.TextUtil;

public class GuiSupplierPipe extends LogisticsBaseGuiScreen {

	private static final String PREFIX = "gui.supplierpipe.";
	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/supplier.png");
	private final boolean hasPatternUpgrade;
	private final PropertyLayer propertyLayer;
	private final ModuleActiveSupplier supplierModule;
	private final PropertyLayer.PropertyOverlay<List<Integer>, IntListProperty> slotAssignmentPatternOverlay;
	private final PropertyLayer.ValuePropertyOverlay<SupplyMode, EnumProperty<SupplyMode>> requestModeOverlay;
	private final PropertyLayer.ValuePropertyOverlay<PatternMode, EnumProperty<PatternMode>> patternModeOverlay;
	private PropertyLayer.ValuePropertyOverlay<Boolean, BooleanProperty> limitedPropertyOverlay;

	public GuiSupplierPipe(IInventory playerInventory, IInventory dummyInventory, ModuleActiveSupplier module,
			Boolean flag, int[] slots) {
		super(null);
		hasPatternUpgrade = flag;
		supplierModule = module;

		propertyLayer = new PropertyLayer(supplierModule.getProperties());

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
		slotAssignmentPatternOverlay = propertyLayer.overlay(supplierModule.slotAssignmentPattern);
		slotAssignmentPatternOverlay.write((p) -> p.replaceContent(slots));
		xSize = 194;
		ySize = 186;
		patternModeOverlay = propertyLayer
				.overlay(supplierModule.patternMode);
		requestModeOverlay = propertyLayer.overlay(supplierModule.requestMode);
		limitedPropertyOverlay = propertyLayer.overlay(supplierModule.isLimited);
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		propertyLayer.unregister();
		if (this.mc.player != null && !propertyLayer.getProperties().isEmpty()) {
			// send update to server, when there are changed properties
			MainProxy.sendPacketToServer(
					ModulePropertiesUpdate.fromPropertyHolder(propertyLayer).setModulePos(supplierModule));
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		String name;
		if (hasPatternUpgrade) {
			name = TextUtil.translate(GuiSupplierPipe.PREFIX + "TargetInvPattern");
		} else {
			name = TextUtil.translate(GuiSupplierPipe.PREFIX + "TargetInv");
		}
		mc.fontRenderer.drawString(name, xSize / 2 - mc.fontRenderer.getStringWidth(name) / 2, 6, 0x404040);
		mc.fontRenderer
				.drawString(TextUtil.translate(GuiSupplierPipe.PREFIX + "Inventory"), 18, ySize - 102, 0x404040);
		mc.fontRenderer
				.drawString(TextUtil.translate(GuiSupplierPipe.PREFIX + "RequestMode"), xSize - 140, ySize - 112,
						0x404040);
		if (hasPatternUpgrade) {
			slotAssignmentPatternOverlay.read((slotAssignments) -> {
				for (int i = 0; i < slotAssignments.size(); i++) {
					mc.fontRenderer.drawString(Integer.toString(slotAssignments.get(i)), 22 + i * 18, 55, 0x404040);
				}
				return null;
			});
		}
	}

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
		buttonList.add(new GuiButton(0, width / 2 + 35, height / 2 - 25, 50, 20, getModeText()));
		if (hasPatternUpgrade) {
			buttonList.add(new SmallGuiButton(1, guiLeft + 5, guiTop + 68, 45, 10, getLimitationText()));
			for (int i = 0; i < 9; i++) {
				buttonList.add(new SmallGuiButton(i + 2, guiLeft + 18 + i * 18, guiTop + 40, 17, 10, "Set"));
			}
		}
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) throws IOException {
		if (guibutton.id == 0) {
			if (hasPatternUpgrade) {
				final PatternMode newMode = patternModeOverlay.write(EnumProperty::next);
				buttonList.get(0).displayString = newMode.toString();
			} else {
				final SupplyMode newMode = requestModeOverlay.write(EnumProperty::next);
				buttonList.get(0).displayString = newMode.toString();
			}
		} else if (hasPatternUpgrade) {
			if (guibutton.id == 1) {
				limitedPropertyOverlay.write(BooleanProperty::toggle);
				buttonList.get(1).displayString = getLimitationText();
			} else if (guibutton.id >= 2 && guibutton.id <= 10) {
				MainProxy.sendPacketToServer(
						PacketHandler.getPacket(SlotFinderOpenGuiPacket.class).setSlot(guibutton.id - 2)
								.setModulePos(supplierModule));
			}
		}
		super.actionPerformed(guibutton);
	}

	public void refreshMode() {
		buttonList.get(0).displayString = getModeText();
		if (hasPatternUpgrade) {
			limitedPropertyOverlay = propertyLayer.overlay(supplierModule.isLimited);
			buttonList.get(1).displayString = getLimitationText();
		}
	}

	@Nonnull
	private String getLimitationText() {
		return limitedPropertyOverlay.get() ? "Limited" : "Unlimited";
	}

	private String getModeText() {
		return (hasPatternUpgrade ? patternModeOverlay : requestModeOverlay).get().toString();
	}

}
