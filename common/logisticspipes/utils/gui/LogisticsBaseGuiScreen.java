/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils.gui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import logisticspipes.LPConstants;
import logisticspipes.asm.ModDependentInterface;
import logisticspipes.asm.ModDependentMethod;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.gui.DummyContainerSlotClick;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.extention.GuiExtentionController;
import logisticspipes.utils.gui.extention.GuiExtentionController.GuiSide;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import codechicken.nei.VisiblityData;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.api.TaggedInventoryArea;

@ModDependentInterface(modId={"NotEnoughItems"}, interfacePath={"codechicken.nei.INEIGuiHandler"})
public abstract class LogisticsBaseGuiScreen extends GuiContainer implements ISubGuiControler, INEIGuiHandler {
	
	public enum Colors
	{
		White,
		Black,
		LightGrey,
		MiddleGrey,
		DarkGrey,
		Red
	}
	
	protected static final ResourceLocation ITEMSINK = new ResourceLocation("logisticspipes", "textures/gui/itemsink.png");
	protected static final ResourceLocation SUPPLIER = new ResourceLocation("logisticspipes", "textures/gui/supplier.png");
	protected static final ResourceLocation CHASSI1 = new ResourceLocation("logisticspipes", "textures/gui/itemsink.png");
	
	protected int right;
	protected int bottom;
	protected int xCenter;
	protected int yCenter;
	protected final int xCenterOffset;
	protected final int yCenterOffset;
	
	private SubGuiScreen subGui;
	protected List<IRenderSlot> slots = new ArrayList<IRenderSlot>();
	protected GuiExtentionController extentionControllerLeft = new GuiExtentionController(GuiSide.LEFT);
	protected GuiExtentionController extentionControllerRight = new GuiExtentionController(GuiSide.RIGHT);
	private GuiButton selectedButton;
	
	public LogisticsBaseGuiScreen(int xSize, int ySize, int xCenterOffset, int yCenterOffset){
		this(new DummyContainer(null, null), xSize, ySize, xCenterOffset, yCenterOffset);
	}

	public LogisticsBaseGuiScreen(Container container){
		super(container);
		this.xCenterOffset = 0;
		this.yCenterOffset = 0;
	}

	public LogisticsBaseGuiScreen(Container container, int xSize, int ySize, int xCenterOffset, int yCenterOffset){
		super(container);
		this.xSize = xSize;
		this.ySize = ySize;
		this.xCenterOffset = xCenterOffset;
		this.yCenterOffset = yCenterOffset;
	}
	
	@Override
	public void initGui() {
		super.initGui();
		this.guiLeft =  width/2 - xSize/2 + xCenterOffset;
		this.guiTop = height/2 - ySize/2  + yCenterOffset;
		
		this.right = width/2 + xSize/2 + xCenterOffset;
		this.bottom = height/2 + ySize/2 + yCenterOffset;
		
		this.xCenter = (right + guiLeft) / 2;
		this.yCenter = (bottom + guiTop) / 2;
		extentionControllerLeft.setMaxBottom(bottom);
		extentionControllerRight.setMaxBottom(bottom);
	}
	
	@Override
	public boolean hasSubGui() {
		return subGui != null;
	}
	
	@Override
	public SubGuiScreen getSubGui() {
		return subGui;
	}
	
	@Override
	public void setSubGui(SubGuiScreen gui) {
		if(subGui == null) {
			subGui = gui;
			subGui.register(this);
			subGui.setWorldAndResolution(this.mc, this.width, this.height);
			subGui.initGui();
		}
	}

	@Override
	public void setWorldAndResolution(Minecraft mc, int width, int height) {
		super.setWorldAndResolution(mc, width, height);
		if(subGui != null)
			subGui.setWorldAndResolution(mc, width, height);
	}
	
	@Override
	public void resetSubGui() {
		subGui = null;
	}

	@Override
	public void drawDefaultBackground() {
		if(subGui == null) {
			super.drawDefaultBackground();
		}
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		checkButtons();
		if(subGui != null) {
			//Save Mouse Pos
			int x = Mouse.getX();
			int y = Mouse.getY();
			//Set Pos 0,0
			try {
				Field fX = Mouse.class.getDeclaredField("x");
				Field fY = Mouse.class.getDeclaredField("y");
				fX.setAccessible(true);
				fY.setAccessible(true);
				fX.set(null, 0);
				fY.set(null, 0);
			} catch (Exception e) {
				if(LPConstants.DEBUG) e.printStackTrace();
			}
			//Draw super class (maybe NEI)
			super.drawScreen(0, 0, par3);
			//Resore Mouse Pos
			try {
				Field fX = Mouse.class.getDeclaredField("x");
				Field fY = Mouse.class.getDeclaredField("y");
				fX.setAccessible(true);
				fY.setAccessible(true);
				fX.set(null, x);
				fY.set(null, y);
			} catch (Exception e) {
				if(LPConstants.DEBUG) e.printStackTrace();
			}
	        RenderHelper.disableStandardItemLighting();
			GL11.glTranslatef(0.0F, 0.0F, 101.0F);
			if(!subGui.hasSubGui()) {
		        GL11.glDisable(GL11.GL_DEPTH_TEST);
				super.drawDefaultBackground();
			    GL11.glEnable(GL11.GL_DEPTH_TEST);
				}
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			subGui.drawScreen(par1, par2, par3);
		    GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glTranslatef(0.0F, 0.0F, -101.0F);
	        RenderHelper.enableStandardItemLighting();
		} else {
			super.drawScreen(par1, par2, par3);
	        RenderHelper.disableStandardItemLighting();
			for(IRenderSlot slot:slots) {
				int mouseX = par1 - guiLeft;
				int mouseY = par2 - guiTop;
				int mouseXMax = mouseX - slot.getSize();
				int mouseYMax = mouseY - slot.getSize();
				if(slot.getXPos() < mouseX && slot.getXPos() > mouseXMax && slot.getYPos() < mouseY && slot.getYPos() > mouseYMax) {
					if(slot.displayToolTip()) {
						if(slot.getToolTipText() != null && !slot.getToolTipText().equals("")) {
							ArrayList<String> list = new ArrayList<String>();
							list.add(slot.getToolTipText());
							BasicGuiHelper.drawToolTip(par1, par2, list, EnumChatFormatting.WHITE, false);
						}
					}
				}
			}
	        RenderHelper.enableStandardItemLighting();
		}
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		renderExtentions();
	}

	protected void renderExtentions() {
		extentionControllerLeft.render(guiLeft, guiTop);
		extentionControllerRight.render(guiLeft + xSize, guiTop);		
	}

	@Override
	protected void func_146977_a(Slot slot) {
		if(extentionControllerLeft.renderSlot(slot) && extentionControllerRight.renderSlot(slot)) {
			super.func_146977_a(slot);
		}
	}

	@Override
	protected boolean isMouseOverSlot(Slot par1Slot, int par2, int par3) {
		if(!extentionControllerLeft.renderSelectSlot(par1Slot)) return false;
		if(!extentionControllerRight.renderSelectSlot(par1Slot)) return false;
		return super.isMouseOverSlot(par1Slot, par2, par3);
	}

	@SuppressWarnings("unchecked")
	protected void checkButtons() {
		for(GuiButton button:(List<GuiButton>) this.buttonList) {
			if(extentionControllerLeft.renderButtonControlled(button)) {
				button.visible = extentionControllerLeft.renderButton(button);
			}
			if(extentionControllerRight.renderButtonControlled(button)) {
				button.visible = extentionControllerRight.renderButton(button);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public GuiButton addButton(GuiButton button) {
		this.buttonList.add(button);
		return button;
	}

	@Override
    public final void handleMouseInput() {
		if(subGui != null) {
			subGui.handleMouseInput();
		} else {
			this.handleMouseInputSub();
		}
    }
	
	public void handleMouseInputSub() {
		super.handleMouseInput();
	}
	
	@Override
	public final void handleKeyboardInput() {
		if(subGui != null) {
			subGui.handleKeyboardInput();
		} else {
			super.handleKeyboardInput();
		}
	}

	public void addRenderSlot(IRenderSlot slot) {
		this.slots.add(slot);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		if(par1 < guiLeft) {
			extentionControllerLeft.mouseOver(par1, par2);
		}
		if(par1 > guiLeft + xSize) {
			extentionControllerRight.mouseOver(par1, par2);
		}
		for(IRenderSlot slot:slots) {
			if(slot instanceof IItemTextureRenderSlot) {
				if(slot.drawSlotBackground()) 
					BasicGuiHelper.drawSlotBackground(mc, slot.getXPos(), slot.getYPos());
				if(((IItemTextureRenderSlot)slot).drawSlotIcon() && !((IItemTextureRenderSlot)slot).customRender(mc, zLevel)) 
					BasicGuiHelper.renderIconAt(mc, slot.getXPos() + 1, slot.getYPos() + 1, zLevel,  ((IItemTextureRenderSlot)slot).getTextureIcon());
			} else if(slot instanceof ISmallColorRenderSlot) {
				if(slot.drawSlotBackground())
					BasicGuiHelper.drawSmallSlotBackground(mc, slot.getXPos(), slot.getYPos());
				if(((ISmallColorRenderSlot)slot).drawColor()) 
					drawRect(slot.getXPos() + 1, slot.getYPos() + 1, slot.getXPos() + 7, slot.getYPos() + 7, ((ISmallColorRenderSlot)slot).getColor());
			}
		}
	}

	@Override
	protected void mouseClicked(int par1, int par2, int par3) {
		for(IRenderSlot slot:slots) {
			int mouseX = par1 - guiLeft;
			int mouseY = par2 - guiTop;
			int mouseXMax = mouseX - slot.getSize();
			int mouseYMax = mouseY - slot.getSize();
			if(slot.getXPos() < mouseX && slot.getXPos() > mouseXMax && slot.getYPos() < mouseY && slot.getYPos() > mouseYMax) {
				slot.mouseClicked(par3);
				return;
			}
		}
		boolean handledButton = false;
		if (par3 == 0) {
			for (int l = 0; l < this.buttonList.size(); ++l) {
				GuiButton guibutton = (GuiButton) this.buttonList.get(l);
				if (guibutton.mousePressed(this.mc, par1, par2)) {
					this.selectedButton = guibutton;
					guibutton.func_146113_a(this.mc.getSoundHandler());
					this.actionPerformed(guibutton);
					handledButton = true;
					break;
				}
			}
		}
		if(!handledButton) {
			super.mouseClicked(par1, par2, par3);
		}
		if(par3 == 0 && par1 < guiLeft && !mouseCanPressButton(par1, par2) && !isOverSlot(par1, par2)) {
			extentionControllerLeft.mouseClicked(par1, par2, par3);
		}
		if(par3 == 0 && par1 > guiLeft + xSize && !mouseCanPressButton(par1, par2) && !isOverSlot(par1, par2)) {
			extentionControllerRight.mouseClicked(par1, par2, par3);
		}
	}

	protected void mouseMovedOrUp(int par1, int par2, int par3) {
		if (this.selectedButton != null && par3 == 0) {
			this.selectedButton.mouseReleased(par1, par2);
			this.selectedButton = null;
		} else {
			super.mouseMovedOrUp(par1, par2, par3);
		}
	}

	private boolean mouseCanPressButton(int par1, int par2) {
		for (int l = 0; l < this.buttonList.size(); ++l) {
			GuiButton guibutton = (GuiButton) this.buttonList.get(l);
			if (guibutton.mousePressed(this.mc, par1, par2)) {
				return true;
			}
		}
		return false;
	}

	private boolean isOverSlot(int par1, int par2) {
		for (int k = 0; k < this.inventorySlots.inventorySlots.size(); ++k) {
			Slot slot = (Slot) this.inventorySlots.inventorySlots.get(k);
			if (this.isMouseOverSlot(slot, par1, par2)) {
				return true;
			}
		}
		return false;
	}

	public void drawPoint(int x, int y, int color){
		drawRect(x, y, x+1, y+1, color);
	}
	
	public void drawPoint(int x, int y, Colors color){
		drawRect(x, y, x+1, y+1, BasicGuiHelper.ConvertEnumToColor(color));
	}
	
	public void drawRect(int x1, int y1, int x2, int y2, Colors color){
		drawRect(x1, y1, x2, y2, BasicGuiHelper.ConvertEnumToColor(color));
	}
	
	public void drawLine(int x1, int y1, int x2, int y2, Colors color){
		int lasty = y1;
		for (int dx = 0; x1 + dx < x2; dx++){
			int plotx = x1 + dx;
			int ploty = y1 + (y2 - y1) / (x2-x1-1) * dx;
			drawPoint(plotx, ploty, color);
			while(lasty < ploty){
				drawPoint(plotx,++lasty, color);
			}
			while (lasty > ploty){
				drawPoint(plotx, --lasty, color);
			}
		}
		while(lasty < y2){
			drawPoint(x2,++lasty, color);
		}
		while (lasty > y2){
			drawPoint(x2, --lasty, color);
		}
	}
	
	public void closeGui() {
		this.keyTyped(' ', 1);
	}
	
	public Minecraft getMC() {
		return mc;
	}

	@Override
	public LogisticsBaseGuiScreen getBaseScreen() {
		return this;
	}
	
	@Override
	@ModDependentMethod(modId="NotEnoughItems")
	public List<TaggedInventoryArea> getInventoryAreas(GuiContainer gui) {
		return null;
	}

	@Override
	@ModDependentMethod(modId="NotEnoughItems")
	public Iterable<Integer> getItemSpawnSlots(GuiContainer gui, ItemStack stack) {
		return null;
	}

	@Override
	@ModDependentMethod(modId="NotEnoughItems")
	public boolean handleDragNDrop(GuiContainer gui, int mouseX, int mouseY, ItemStack stack, int button) {
		if(gui instanceof LogisticsBaseGuiScreen && gui.inventorySlots instanceof DummyContainer && stack != null) {
			Slot result = null;
			int pos = -1;
			for (int k = 0; k < this.inventorySlots.inventorySlots.size(); ++k) {
				Slot slot = (Slot) this.inventorySlots.inventorySlots.get(k);
				if (this.isMouseOverSlot(slot, mouseX, mouseY)) {
					result = slot;
					pos = k;
					break;
				}
			}
			if(result != null) {
				if(result instanceof DummySlot || result instanceof ColorSlot || result instanceof FluidSlot) {
					((DummyContainer)gui.inventorySlots).handleDummyClick(result, pos, stack, button, 0, this.mc.thePlayer);
					MainProxy.sendPacketToServer(PacketHandler.getPacket(DummyContainerSlotClick.class).setSlotId(pos).setStack(stack).setButton(button));
					return true;
				}
			}
		}
		return false;
	}

	@Override
	@ModDependentMethod(modId="NotEnoughItems")
	public boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int w, int h) {
		if(gui instanceof LogisticsBaseGuiScreen) {
			return ((LogisticsBaseGuiScreen)gui).extentionControllerRight.isOverPanel(x, y, w, h);
		}
		return false;
	}

	@Override
	@ModDependentMethod(modId="NotEnoughItems")
	public VisiblityData modifyVisiblity(GuiContainer gui, VisiblityData currentVisibility) {
		return null;
	}
}
