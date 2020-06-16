/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui;

import java.io.IOException;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import lombok.Getter;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import logisticspipes.LPItems;
import logisticspipes.gui.modules.ModuleBaseGui;
import logisticspipes.gui.popup.GuiSelectSatellitePopup;
import logisticspipes.items.ItemUpgrade;
import logisticspipes.modules.ModuleCrafter;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.cpipe.CPipeCleanupImport;
import logisticspipes.network.packets.cpipe.CPipeCleanupToggle;
import logisticspipes.network.packets.pipe.CraftingPipeSetSatellitePacket;
import logisticspipes.pipes.upgrades.CraftingByproductUpgrade;
import logisticspipes.pipes.upgrades.CraftingCleanupUpgrade;
import logisticspipes.pipes.upgrades.FluidCraftingUpgrade;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.gui.extention.GuiExtention;
import logisticspipes.utils.string.StringUtils;

public class GuiCraftingPipe extends ModuleBaseGui {

	private static final String PREFIX = "gui.crafting.";

	@Getter
	private final ModuleCrafter craftingModule;
	private final EntityPlayer _player;
	private final GuiButton[] normalButtonArray;
	private final GuiButton[][] advancedSatButtonArray;
	private final GuiButton[][] liquidGuiParts;
	private final boolean isAdvancedSat;
	private final int liquidCrafter;
	private final boolean hasByproductExtractor;
	private final int cleanupSize;
	private final int[] fluidSlotIDs;
	private final int byproductSlotID;
	private final int[] cleanupSlotIDs;

	private GuiButton cleanupModeButton;

	public GuiCraftingPipe(EntityPlayer player, IInventory dummyInventory, ModuleCrafter module, boolean isAdvancedSat, int liquidCrafter, int[] amount, boolean hasByproductExtractor, boolean isFuzzy, int cleanupSize, boolean cleanupExclude) {
		super(null, module);
		_player = player;
		this.isAdvancedSat = isAdvancedSat;
		this.liquidCrafter = liquidCrafter;
		this.hasByproductExtractor = hasByproductExtractor;
		this.cleanupSize = cleanupSize;
		module.cleanupModeIsExclude = cleanupExclude;

		if (!hasByproductExtractor) {
			xSize = 177;
		} else {
			xSize = 217;
		}

		if (!isAdvancedSat) {
			ySize = 187;
		} else {
			ySize = 187 + 30;
		}

		DummyContainer dummy = new DummyContainer(player.inventory, dummyInventory);
		dummy.addNormalSlotsForPlayerInventory(8, ySize - 82);

		// Input slots
		for (int l = 0; l < 9; l++) {
			if (isFuzzy) {
				dummy.addFuzzyDummySlot(l, 8 + l * 18, 18, module.fuzzyCraftingFlagArray[l]);
			} else {
				dummy.addDummySlot(l, 8 + l * 18, 18);
			}
		}

		// Output slot
		int yPosOutput = 55;
		if (isAdvancedSat) yPosOutput = 105;
		if (isFuzzy) {
			dummy.addFuzzyDummySlot(9, 85, yPosOutput, module.outputFuzzyFlags);
		} else {
			dummy.addDummySlot(9, 85, yPosOutput);
		}

		liquidGuiParts = new GuiButton[liquidCrafter][];
		fluidSlotIDs = new int[liquidCrafter];

		for (int i = 0; i < liquidCrafter; i++) {
			int liquidLeft;
			if (isAdvancedSat) {
				liquidLeft = -40;
			} else {
				liquidLeft = -(liquidCrafter * 40) + (i * 40);
			}
			fluidSlotIDs[i] = extentionControllerLeft.registerControlledSlot(dummy.addFluidSlot(i, module.getFluidInventory(), liquidLeft + 11, 24));
		}

		if (hasByproductExtractor) {
			byproductSlotID = extentionControllerLeft.registerControlledSlot(dummy.addDummySlot(10, -26, 29));
		} else {
			byproductSlotID = -1;
		}

		cleanupSlotIDs = new int[cleanupSize * 3];
		for (int y = 0; y < cleanupSize; y++) {
			for (int x = 0; x < 3; x++) {
				cleanupSlotIDs[y * 3 + x] = extentionControllerLeft.registerControlledSlot(dummy.addDummySlot(y * 3 + x, module.getCleanupInventory(), x * 18 - 57, y * 18 + 13));
			}
		}

		inventorySlots = dummy;
		craftingModule = module;
		craftingModule.setFluidAmount(amount);
		normalButtonArray = new GuiButton[7];
		advancedSatButtonArray = new GuiButton[9][2];
		for (int i = 0; i < 9; i++) {
			advancedSatButtonArray[i] = new GuiButton[2];
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		extentionControllerLeft.clear();
		buttonList.clear();
		FluidCraftingExtention extention = null;
		if (!isAdvancedSat) {
			if (liquidCrafter != 0) {
				extention = new FluidCraftingExtention(0);
			}
			addButton(normalButtonArray[0] = new SmallGuiButton(0, (width - xSize) / 2 + 125, (height - ySize) / 2 + 57, 37, 10, StringUtils.translate(PREFIX + "Select")));
			normalButtonArray[0].enabled = craftingModule.getSlot().isInWorld();
			addButton(normalButtonArray[1] = new SmallGuiButton(3, (width - xSize) / 2 + 39, (height - ySize) / 2 + 50, 37, 10, StringUtils.translate(GuiCraftingPipe.PREFIX + "Import")));
			addButton(normalButtonArray[2] = new SmallGuiButton(4, (width - xSize) / 2 + 6, (height - ySize) / 2 + 50, 28, 10, StringUtils.translate(GuiCraftingPipe.PREFIX + "Open")));
			addButton(normalButtonArray[3] = new SmallGuiButton(20, (width - xSize) / 2 + 155, (height - ySize) / 2 + 85, 10, 10, ">"));
			addButton(normalButtonArray[4] = new SmallGuiButton(21, (width - xSize) / 2 + 120, (height - ySize) / 2 + 85, 10, 10, "<"));
			if (liquidCrafter != 0) {
				extention.registerButton(extentionControllerLeft.registerControlledButton(addButton(normalButtonArray[5] = new SmallGuiButton(22, guiLeft - (liquidCrafter * 40) / 2 - 18, guiTop + 158, 37, 10, StringUtils.translate(PREFIX + "Select")))));
			}
		} else {
			for (int i = 0; i < 9; i++) {
				addButton(advancedSatButtonArray[i][0] = new SmallGuiButton(30 + i, (width - xSize) / 2 + 9 + 18 * i, (height - ySize) / 2 + 75, 17, 10, StringUtils.translate(PREFIX + "Sel")));
			}
			addButton(normalButtonArray[1] = new SmallGuiButton(3, (width - xSize) / 2 + 39, (height - ySize) / 2 + 100, 37, 10, StringUtils.translate(GuiCraftingPipe.PREFIX + "Import")));
			addButton(normalButtonArray[2] = new SmallGuiButton(4, (width - xSize) / 2 + 6, (height - ySize) / 2 + 100, 28, 10, StringUtils.translate(GuiCraftingPipe.PREFIX + "Open")));
			addButton(normalButtonArray[3] = new SmallGuiButton(20, (width - xSize) / 2 + 155, (height - ySize) / 2 + 105, 10, 10, ">"));
			addButton(normalButtonArray[4] = new SmallGuiButton(21, (width - xSize) / 2 + 120, (height - ySize) / 2 + 105, 10, 10, "<"));
		}
		for (int i = 0; i < liquidCrafter; i++) {
			if (isAdvancedSat) {
				extention = new FluidCraftingExtention(i);
			}
			int liquidLeft;
			if (isAdvancedSat) {
				liquidLeft = guiLeft - 40;
			} else {
				liquidLeft = guiLeft - (liquidCrafter * 40) + (i * 40);
			}
			liquidGuiParts[i] = new GuiButton[10];
			extention.registerButton(extentionControllerLeft.registerControlledButton(addButton(liquidGuiParts[i][0] = new SmallGuiButton(100 + 10 * i + 0, liquidLeft + 22, guiTop + 65, 10, 10, "+"))));
			extention.registerButton(extentionControllerLeft.registerControlledButton(addButton(liquidGuiParts[i][1] = new SmallGuiButton(100 + 10 * i + 1, liquidLeft + 22, guiTop + 85, 10, 10, "+"))));
			extention.registerButton(extentionControllerLeft.registerControlledButton(addButton(liquidGuiParts[i][2] = new SmallGuiButton(100 + 10 * i + 2, liquidLeft + 22, guiTop + 105, 10, 10, "+"))));
			extention.registerButton(extentionControllerLeft.registerControlledButton(addButton(liquidGuiParts[i][3] = new SmallGuiButton(100 + 10 * i + 3, liquidLeft + 22, guiTop + 125, 10, 10, "+"))));
			extention.registerButton(extentionControllerLeft.registerControlledButton(addButton(liquidGuiParts[i][4] = new SmallGuiButton(100 + 10 * i + 4, liquidLeft + 8, guiTop + 65, 10, 10, "-"))));
			extention.registerButton(extentionControllerLeft.registerControlledButton(addButton(liquidGuiParts[i][5] = new SmallGuiButton(100 + 10 * i + 5, liquidLeft + 8, guiTop + 85, 10, 10, "-"))));
			extention.registerButton(extentionControllerLeft.registerControlledButton(addButton(liquidGuiParts[i][6] = new SmallGuiButton(100 + 10 * i + 6, liquidLeft + 8, guiTop + 105, 10, 10, "-"))));
			extention.registerButton(extentionControllerLeft.registerControlledButton(addButton(liquidGuiParts[i][7] = new SmallGuiButton(100 + 10 * i + 7, liquidLeft + 8, guiTop + 125, 10, 10, "-"))));
			if (isAdvancedSat) {
				final SmallGuiButton advancedSatelliteSelector = new SmallGuiButton(100 + 10 * i + 8, liquidLeft + 2, guiTop + 160, 37, 10, StringUtils.translate(PREFIX + "Select"));
				advancedSatelliteSelector.enabled = craftingModule.getSlot().isInWorld();
				extention.registerButton(extentionControllerLeft.registerControlledButton(addButton(liquidGuiParts[i][8] = advancedSatelliteSelector)));
				extentionControllerLeft.addExtention(extention);
			}
			extention.registerSlot(fluidSlotIDs[i]);
		}
		if (!isAdvancedSat && liquidCrafter != 0) {
			extentionControllerLeft.addExtention(extention);
		}
		if (hasByproductExtractor) {
			ByproductExtention byproductExtention = new ByproductExtention();
			byproductExtention.registerSlot(byproductSlotID);
			extentionControllerLeft.addExtention(byproductExtention);
		}
		if (cleanupSize > 0) {
			CleanupExtention cleanupExtention = new CleanupExtention();
			cleanupExtention.registerButton(extentionControllerLeft.registerControlledButton(addButton(cleanupModeButton = new SmallGuiButton(24, guiLeft - 56, guiTop + 18 + (18 * cleanupSize), 50, 10, StringUtils.translate(GuiCraftingPipe.PREFIX + (craftingModule.cleanupModeIsExclude ? "Exclude" : "Include"))))));
			cleanupExtention.registerButton(extentionControllerLeft.registerControlledButton(addButton(new SmallGuiButton(25, guiLeft - 56, guiTop + 32 + (18 * cleanupSize), 50, 10, StringUtils.translate(GuiCraftingPipe.PREFIX + "Import")))));
			for (int i = 0; i < cleanupSize * 3; i++) {
				cleanupExtention.registerSlot(cleanupSlotIDs[i]);
			}
			extentionControllerLeft.addExtention(cleanupExtention);
		}
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) throws IOException {
		/*
		if(5 <= guibutton.id && guibutton.id < 11) {
			_pipe.handleStackMove(guibutton.id - 5);
		}
		 */
		if (30 <= guibutton.id && guibutton.id < 40) {
			openSubGuiForSatelliteSelection(10 + (guibutton.id - 30), false);
		}
		if (100 <= guibutton.id && guibutton.id < 200) {
			int i = guibutton.id - 100;
			int action = i % 10;
			i -= action;
			i /= 10;
			if (action < 8) {
				int amount = 0;
				switch (action) {
					case 0:
						amount = 1;
						break;
					case 1:
						amount = 10;
						break;
					case 2:
						amount = 100;
						break;
					case 3:
						amount = 1000;
						break;
					case 4:
						amount = -1;
						break;
					case 5:
						amount = -10;
						break;
					case 6:
						amount = -100;
						break;
					case 7:
						amount = -1000;
						break;
					default:
						break;
				}
				craftingModule.changeFluidAmount(amount, i, _player);
			} else if (action == 8) {
				openSubGuiForSatelliteSelection(110 + i, true);
			}
		}
		switch (guibutton.id) {
			case 0:
				openSubGuiForSatelliteSelection(0, false);
				break;
			case 3:
				craftingModule.importFromCraftingTable(_player);
				break;
			case 4:
				craftingModule.openAttachedGui(_player);
				//LogisticsEventListener.addGuiToReopen(_pipe.getX(), _pipe.getY(), _pipe.getZ(), 0); //TODO reactivate this
				break;
			case 20:
				craftingModule.priorityUp(_player);
				break;
			case 21:
				craftingModule.priorityDown(_player);
				break;
			case 22:
				openSubGuiForSatelliteSelection(100, true);
				break;
			case 24:
				MainProxy.sendPacketToServer(PacketHandler.getPacket(CPipeCleanupToggle.class).setModulePos(craftingModule));
				break;
			case 25:
				MainProxy.sendPacketToServer(PacketHandler.getPacket(CPipeCleanupImport.class).setModulePos(craftingModule));
				break;
			default:
				super.actionPerformed(guibutton);
		}
	}

	private void openSubGuiForSatelliteSelection(int id, boolean fluidSatellite) {
		if (module.getSlot().isInWorld()) {
			this.setSubGui(new GuiSelectSatellitePopup(module.getBlockPos(), fluidSatellite, uuid ->
					MainProxy.sendPacketToServer(PacketHandler.getPacket(CraftingPipeSetSatellitePacket.class).setPipeID(uuid).setInteger(id).setModulePos(module))));
		}
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		inventorySlots.onContainerClosed(_player);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		GL11.glDisable(GL11.GL_LIGHTING);
		GlStateManager.color(1.0F,1.0F,1.0F,1.0F);
		mc.fontRenderer.drawString(StringUtils.translate(GuiCraftingPipe.PREFIX + "Inputs"), 18, 7, 0x404040);
		mc.fontRenderer.drawString(StringUtils.translate(GuiCraftingPipe.PREFIX + "Inventory"), 10, ySize - 93, 0x404040);

		if (!isAdvancedSat) {
			mc.fontRenderer.drawString(StringUtils.translate(GuiCraftingPipe.PREFIX + "Output"), 77, 40, 0x404040);
			mc.fontRenderer.drawString(StringUtils.translate(GuiCraftingPipe.PREFIX + "Satellite"), 123, 7, 0x404040);
			if (craftingModule.clientSideSatelliteNames.satelliteName.isEmpty()) {
				mc.fontRenderer.drawString(StringUtils.translate(GuiCraftingPipe.PREFIX + "Off"), 135, 43, 0x404040);
			} else {
				String name = craftingModule.clientSideSatelliteNames.satelliteName;
				name = StringUtils.getCuttedString(name, 55, mc.fontRenderer);
				mc.fontRenderer.drawString(name, 143 - (mc.fontRenderer.getStringWidth(name) / 2), 43, 0x404040);
			}
			mc.fontRenderer.drawString(StringUtils.translate(GuiCraftingPipe.PREFIX + "Priority") + ":", 123, 75, 0x404040);
			mc.fontRenderer.drawString("" + craftingModule.priority, 143 - (mc.fontRenderer.getStringWidth("" + craftingModule.priority) / 2), 87, 0x404040);
		} else {
			for (int i = 0; i < 9; i++) {
				if (craftingModule.clientSideSatelliteNames.advancedSatelliteNameArray[i].isEmpty()) {
					mc.fontRenderer.drawString(StringUtils.translate(GuiCraftingPipe.PREFIX + "Off"), 9 + (i * 18), 57, 0x404040);
				} else {
					String name = craftingModule.clientSideSatelliteNames.advancedSatelliteNameArray[i];
					name = StringUtils.getCuttedString(name, 40, mc.fontRenderer);
					GlStateManager.pushMatrix();
					GlStateManager.translate(20 + (i * 18), 39, 0);
					GlStateManager.rotate(90, 0, 0, 1);
					mc.fontRenderer.drawString(name, 0, 0, 0x404040);
					GL11.glPopMatrix();
				}
			}
			mc.fontRenderer.drawString(StringUtils.translate(GuiCraftingPipe.PREFIX + "Output"), 77, 90, 0x404040);
			mc.fontRenderer.drawString(StringUtils.translate(GuiCraftingPipe.PREFIX + "Priority") + ":", 123, 95, 0x404040);
			mc.fontRenderer.drawString("" + craftingModule.priority, 143 - (mc.fontRenderer.getStringWidth("" + craftingModule.priority) / 2), 107, 0x404040);
		}
		GL11.glEnable(GL11.GL_LIGHTING);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, guiLeft + xSize - (hasByproductExtractor ? 40 : 0), guiTop + ySize, zLevel, true, true, true, true, true);

		if (!isAdvancedSat) {
			Gui.drawRect(guiLeft + 115, guiTop + 4, guiLeft + 170, guiTop + 70, 0xff8B8B8B);
		}

		for (int i = 0; i < 9; i++) {
			GuiGraphics.drawSlotBackground(mc, guiLeft + 7 + (18 * i), guiTop + 17);
		}
		if (!isAdvancedSat) {
			GuiGraphics.drawBigSlotBackground(mc, guiLeft + 80, guiTop + 50);
		} else {
			GuiGraphics.drawBigSlotBackground(mc, guiLeft + 80, guiTop + 100);
		}
		GuiGraphics.drawPlayerInventoryBackground(mc, guiLeft + 8, guiTop + ySize - 82);

		super.renderExtentions();
	}

	public void onCleanupModeChange() {
		cleanupModeButton.displayString = StringUtils.translate(GuiCraftingPipe.PREFIX + (craftingModule.cleanupModeIsExclude ? "Exclude" : "Include"));
	}

	private final class FluidCraftingExtention extends GuiExtention {

		private final int id;

		public FluidCraftingExtention(int id) {
			this.id = id;
		}

		@Override
		public int getFinalWidth() {
			if (isAdvancedSat) {
				return 42;
			} else {
				return 2 + liquidCrafter * 40;
			}
		}

		@Override
		public int getFinalHeight() {
			return 175;
		}

		@Override
		public void renderForground(int left, int top) {
			if (!isFullyExtended()) {
				GL11.glEnable(GL12.GL_RESCALE_NORMAL);
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240 / 1.0F, 240 / 1.0F);
				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				RenderHelper.enableGUIStandardItemLighting();
				ItemStack stack = new ItemStack(ItemUpgrade.getAndCheckUpgrade(LPItems.upgrades.get(FluidCraftingUpgrade.getName())));
				itemRender.renderItemAndEffectIntoGUI(stack, left + 5, top + 5);
				itemRender.renderItemOverlayIntoGUI(fontRenderer, stack, left + 5, top + 5, "");
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glDisable(GL11.GL_DEPTH_TEST);
			}
			itemRender.zLevel = 0.0F;

			if (!isAdvancedSat && liquidCrafter > 1 && !isFullyExtended()) {
				String s = Integer.toString(liquidCrafter);
				mc.fontRenderer.drawStringWithShadow(s, left + 22 - fontRenderer.getStringWidth(s), top + 14, 16777215);
			}
			if (isFullyExtended()) {
				if (liquidCrafter > 1 && !isAdvancedSat) {
					for (int i = 1; i < liquidCrafter; i++) {
						int xLine = left + 2 + (i * 40);
						Gui.drawRect(xLine, top + 3, xLine + 1, top + 138, 0xff8B8B8B);
					}
				}
				if (!isAdvancedSat) {
					Gui.drawRect(left + 3, top + 138, left + 2 + (liquidCrafter * 40), top + 139, 0xff8B8B8B);
				}
				if (!isAdvancedSat) {
					for (int i = 0; i < liquidCrafter; i++) {
						int liquidLeft = left + i * 40;
						renderFluidText(liquidLeft, top, i);
					}
					if (craftingModule.clientSideSatelliteNames.liquidSatelliteName.isEmpty()) {
						Gui.drawRect(left + 3, top + 3, left + 3 + (liquidCrafter * 40), top + 138, 0xAA8B8B8B);
						mc.fontRenderer.drawString(StringUtils.translate(GuiCraftingPipe.PREFIX + "Off"), left + (liquidCrafter * 40) / 2 - 5, top + 145, 0x404040);
						for (int i = 0; i < liquidCrafter; i++) {
							for (int j = 0; j < 8; j++) {
								liquidGuiParts[i][j].enabled = false;
							}
						}
					} else {
						mc.fontRenderer.drawString(craftingModule.clientSideSatelliteNames.liquidSatelliteName, left + (liquidCrafter * 40) / 2 + 3 - (fontRenderer.getStringWidth(craftingModule.clientSideSatelliteNames.liquidSatelliteName) / 2), top + 145, 0x404040);
						for (int i = 0; i < liquidCrafter; i++) {
							for (int j = 0; j < 8; j++) {
								liquidGuiParts[i][j].enabled = true;
							}
						}
					}
				} else {
					renderFluidText(left, top, id);
				}
			}
		}

		private void renderFluidText(int left, int top, int i) {
			GuiGraphics.drawSlotBackground(mc, left + 12, top + 19);
			mc.fontRenderer.drawString(Integer.toString(craftingModule.getFluidAmount()[i]), left + 22 - (fontRenderer.getStringWidth(Integer.toString(craftingModule.getFluidAmount()[i])) / 2), top + 40, 0x404040);
			mc.fontRenderer.drawString("1", left + 19, top + 53, 0x404040);
			mc.fontRenderer.drawString("10", left + 16, top + 73, 0x404040);
			mc.fontRenderer.drawString("100", left + 13, top + 93, 0x404040);
			mc.fontRenderer.drawString("1000", left + 10, top + 113, 0x404040);
			if (isAdvancedSat) {
				if (craftingModule.clientSideSatelliteNames.liquidSatelliteNameArray[i].isEmpty()) {
					Gui.drawRect(left + 3, top + 3, left + 42, top + 138, 0xAA8B8B8B);
					mc.fontRenderer.drawString(StringUtils.translate(GuiCraftingPipe.PREFIX + "Off"), left + 15, top + 146, 0x404040);
					for (int j = 0; j < 8; j++) {
						liquidGuiParts[i][j].enabled = false;
					}
				} else {
					String name = craftingModule.clientSideSatelliteNames.liquidSatelliteNameArray[i];
					name = StringUtils.getCuttedString(name, 40, mc.fontRenderer);
					mc.fontRenderer.drawString(name, left + 22 - (fontRenderer.getStringWidth(name) / 2), top + 146, 0x404040);
					for (int j = 0; j < 8; j++) {
						liquidGuiParts[i][j].enabled = true;
					}
				}
				Gui.drawRect(left + 3, top + 138, left + 42, top + 139, 0xff8B8B8B);
			}
			if (craftingModule.getFluidInventory().getStackInSlot(i).isEmpty() && !((!isAdvancedSat && craftingModule.clientSideSatelliteNames.liquidSatelliteName.isEmpty()) || (isAdvancedSat && craftingModule.clientSideSatelliteNames.liquidSatelliteNameArray[i].isEmpty()))) {
				Gui.drawRect(left + 3, top + 50, left + 42, top + 138, 0xAA8B8B8B);
				for (int j = 0; j < 8; j++) {
					liquidGuiParts[i][j].enabled = false;
				}
			}
		}

		@Override
		public boolean renderSelectSlot(int slotId) {
			if ((isAdvancedSat && craftingModule.clientSideSatelliteNames.liquidSatelliteNameArray[id].isEmpty()) || (!isAdvancedSat && craftingModule.clientSideSatelliteNames.liquidSatelliteName.isEmpty())) {
				return false;
			}
			return super.renderSelectSlot(slotId);
		}
	}

	private final class ByproductExtention extends GuiExtention {

		@Override
		public int getFinalWidth() {
			return 40;
		}

		@Override
		public int getFinalHeight() {
			return 55;
		}

		@Override
		public void renderForground(int left, int top) {
			if (!isFullyExtended()) {
				GL11.glEnable(GL12.GL_RESCALE_NORMAL);
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240 / 1.0F, 240 / 1.0F);
				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				RenderHelper.enableGUIStandardItemLighting();
				ItemStack stack = new ItemStack(ItemUpgrade.getAndCheckUpgrade(LPItems.upgrades.get(CraftingByproductUpgrade.getName())));
				itemRender.renderItemAndEffectIntoGUI(stack, left + 5, top + 5);
				itemRender.renderItemOverlayIntoGUI(fontRenderer, stack, left + 5, top + 5, "");
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				itemRender.zLevel = 0.0F;
			} else {
				GuiGraphics.drawBigSlotBackground(mc, left + 9, top + 20);
				fontRenderer.drawString(StringUtils.translate(GuiCraftingPipe.PREFIX + "Extra"), left + 9, top + 8, 0x404040);
			}
		}
	}

	private final class CleanupExtention extends GuiExtention {

		@Override
		public int getFinalWidth() {
			return 66;
		}

		@Override
		public int getFinalHeight() {
			return cleanupSize * 18 + 16 + 30;
		}

		@Override
		public void renderForground(int left, int top) {
			if (!isFullyExtended()) {
				GL11.glEnable(GL12.GL_RESCALE_NORMAL);
				OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240 / 1.0F, 240 / 1.0F);
				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				RenderHelper.enableGUIStandardItemLighting();
				ItemStack stack = new ItemStack(ItemUpgrade.getAndCheckUpgrade(LPItems.upgrades.get(CraftingCleanupUpgrade.getName())));
				itemRender.renderItemAndEffectIntoGUI(stack, left + 5, top + 5);
				itemRender.renderItemOverlayIntoGUI(fontRenderer, stack, left + 5, top + 5, "");
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				itemRender.zLevel = 0.0F;
			} else {
				for (int y = 0; y < cleanupSize; y++) {
					for (int x = 0; x < 3; x++) {
						GuiGraphics.drawSlotBackground(mc, left + 8 + x * 18, top + 8 + y * 18);
					}
				}
			}
		}
	}
}
