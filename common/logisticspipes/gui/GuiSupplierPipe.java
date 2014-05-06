/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui;

import logisticspipes.LogisticsEventListener;
import logisticspipes.interfaces.IGuiIDHandlerProvider;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.module.SupplierPipeLimitedPacket;
import logisticspipes.network.packets.module.SupplierPipeModePacket;
import logisticspipes.network.packets.pipe.SlotFinderOpenGuiPacket;
import logisticspipes.pipes.PipeItemsSupplierLogistics;
import logisticspipes.pipes.PipeItemsSupplierLogistics.PatternMode;
import logisticspipes.pipes.PipeItemsSupplierLogistics.SupplyMode;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.KraphtBaseGuiScreen;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.string.StringUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class GuiSupplierPipe extends KraphtBaseGuiScreen implements IGuiIDHandlerProvider {
	private static final String PREFIX = "gui.supplierpipe.";
	
	private PipeItemsSupplierLogistics logic; 
	private final boolean hasPatternUpgrade;
	
	public GuiSupplierPipe(IInventory playerInventory, IInventory dummyInventory, PipeItemsSupplierLogistics logic, Boolean flag, int[] slots) {
		super(null);
		hasPatternUpgrade = flag;
		
		DummyContainer dummy = new DummyContainer(playerInventory, dummyInventory);
		dummy.addNormalSlotsForPlayerInventory(18, 97);
		
		if(hasPatternUpgrade) {
			for(int i = 0; i < 9;i++) {
				dummy.addDummySlot(i, 18 + i * 18, 20);
			}
		} else {
			int xOffset = 72;
			int yOffset = 18;
			for (int row = 0; row < 3; row++){
				for (int column = 0; column < 3; column++){
					dummy.addDummySlot(column + row * 3, xOffset + column * 18, yOffset + row * 18);					
				}
			}
		}
		this.inventorySlots = dummy; 
		logic.slotArray = slots;
		this.logic = logic;
		xSize = 194;
		ySize = 186;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		String name = "";
		if(hasPatternUpgrade) {
			name = StringUtil.translate(PREFIX + "TargetInvPattern");
		} else {
			name = StringUtil.translate(PREFIX + "TargetInv");
		}
		mc.fontRenderer.drawString(name, xSize / 2 - mc.fontRenderer.getStringWidth(name)/2, 6, 0x404040);
		mc.fontRenderer.drawString(StringUtil.translate(PREFIX + "Inventory"), 18, ySize - 102, 0x404040);
		mc.fontRenderer.drawString(StringUtil.translate(PREFIX + "RequestMode"), xSize - 140, ySize - 112, 0x404040);
		if(hasPatternUpgrade) {
			for(int i = 0; i < 9;i++) {
				mc.fontRenderer.drawString(Integer.toString(logic.slotArray[i]), 22 + i * 18, 55, 0x404040);
			}
		}
	}

	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/supplier.png");
	

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		if(!hasPatternUpgrade) {		
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			mc.renderEngine.bindTexture(TEXTURE);
			int j = guiLeft;
			int k = guiTop;
			drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
		} else {
			BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
			GL11.glTranslated(guiLeft, guiTop, 0);
			for(int i = 0; i< 9;i++) {
				BasicGuiHelper.drawSlotBackground(mc, 17 + i*18, 19);
				Slot slot = this.inventorySlots.getSlot(36 + i);
				if(slot != null && slot.getHasStack() && slot.getStack().stackSize > 64) {
					drawRect(18 + i*18, 20, 34 + i*18, 36, Colors.Red);
				}
			}
			BasicGuiHelper.drawPlayerInventoryBackground(mc, 18, 97);
			GL11.glTranslated(-guiLeft, -guiTop, 0);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		buttonList.add(new GuiButton(0, width / 2 + 35, height / 2 - 25, 50, 20, (hasPatternUpgrade ? logic.getPatternMode() : logic.getSupplyMode()).toString()));
		if(hasPatternUpgrade) {
			buttonList.add(new SmallGuiButton(1, guiLeft + 5, guiTop + 68, 45, 10, logic.isLimited() ? "Limited" : "Unlimited"));
			for(int i=0;i < 9;i++) {
				buttonList.add(new SmallGuiButton(i + 2, guiLeft + 18 + i*18, guiTop + 40, 17, 10, "Set"));
			}
		}
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton.id == 0) {
			if(hasPatternUpgrade) {
				int currentMode = logic.getPatternMode().ordinal() + 1;
				if(currentMode >= PatternMode.values().length){
					currentMode=0;
				}
				logic.setPatternMode(PatternMode.values()[currentMode]);
				((GuiButton)buttonList.get(0)).displayString = logic.getPatternMode().toString();
			} else {
				int currentMode = logic.getSupplyMode().ordinal() + 1;
				if(currentMode >= SupplyMode.values().length){
					currentMode=0;
				}
				logic.setSupplyMode(SupplyMode.values()[currentMode]);
				((GuiButton)buttonList.get(0)).displayString = logic.getSupplyMode().toString();
			}
			MainProxy.sendPacketToServer(PacketHandler.getPacket(SupplierPipeModePacket.class).setPosX(logic.getX()).setPosY(logic.getY()).setPosZ(logic.getZ()));
		} else if(hasPatternUpgrade) {
			if(guibutton.id == 1) {
				logic.setLimited(!logic.isLimited());
				((GuiButton)buttonList.get(1)).displayString = logic.isLimited() ? "Limited" : "Unlimited";
				MainProxy.sendPacketToServer(PacketHandler.getPacket(SupplierPipeLimitedPacket.class).setLimited(logic.isLimited()).setPosX(logic.getX()).setPosY(logic.getY()).setPosZ(logic.getZ()));
			} else if(guibutton.id >= 2 && guibutton.id <= 10) {
				MainProxy.sendPacketToServer(PacketHandler.getPacket(SlotFinderOpenGuiPacket.class).setSlot(guibutton.id - 2).setPosX(logic.getX()).setPosY(logic.getY()).setPosZ(logic.getZ()));
				LogisticsEventListener.addGuiToReopen(logic.getX(), logic.getY(), logic.getZ(), GuiIDs.GUI_SupplierPipe_ID);
			}
		}
		super.actionPerformed(guibutton);
	}
	
	public void refreshMode() {
		((GuiButton)buttonList.get(0)).displayString = (hasPatternUpgrade ? logic.getPatternMode() : logic.getSupplyMode()).toString();
		if(hasPatternUpgrade) {
			((GuiButton)buttonList.get(1)).displayString = logic.isLimited() ? "Limited" : "Unlimited";
		}
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
