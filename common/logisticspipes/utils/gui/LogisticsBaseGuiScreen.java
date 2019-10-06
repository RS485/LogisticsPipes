/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.utils.gui;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.api.TaggedInventoryArea;
import lombok.Getter;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import logisticspipes.LPConstants;
import logisticspipes.asm.ModDependentInterface;
import logisticspipes.asm.ModDependentMethod;
import logisticspipes.interfaces.IChainAddList;
import logisticspipes.interfaces.IFuzzySlot;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.gui.DummyContainerSlotClick;
import logisticspipes.network.packets.gui.FuzzySlotSettingsPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.request.resources.DictResource;
import logisticspipes.utils.ChainAddArrayList;
import logisticspipes.utils.Color;
import logisticspipes.utils.gui.extention.GuiExtentionController;
import logisticspipes.utils.gui.extention.GuiExtentionController.GuiSide;
import logisticspipes.utils.string.StringUtils;

@ModDependentInterface(modId = { LPConstants.neiModID }, interfacePath = { "codechicken.nei.api.INEIGuiHandler" })
public abstract class LogisticsBaseGuiScreen extends GuiContainer implements ISubGuiControler, INEIGuiHandler, IGuiAccess {

	protected static final ResourceLocation ITEMSINK = new ResourceLocation("logisticspipes", "textures/gui/itemsink.png");
	protected static final ResourceLocation SUPPLIER = new ResourceLocation("logisticspipes", "textures/gui/supplier.png");
	protected static final ResourceLocation CHASSI1 = new ResourceLocation("logisticspipes", "textures/gui/itemsink.png");

	@Getter
	protected int right;
	@Getter
	protected int bottom;
	protected int xCenter;
	protected int yCenter;
	protected final int xCenterOffset;
	protected final int yCenterOffset;

	private SubGuiScreen subGui;
	protected List<IRenderSlot> slots = new ArrayList<>();
	protected GuiExtentionController extentionControllerLeft = new GuiExtentionController(GuiSide.LEFT);
	protected GuiExtentionController extentionControllerRight = new GuiExtentionController(GuiSide.RIGHT);
	private GuiButton selectedButton;

	private int currentDrawScreenMouseX;
	private int currentDrawScreenMouseY;

	private IFuzzySlot fuzzySlot;
	private boolean fuzzySlotActiveGui;
	private int fuzzySlotGuiHoverTime;
	private Queue<Runnable> renderAtTheEnd = new LinkedList<>();

	public LogisticsBaseGuiScreen(int xSize, int ySize, int xCenterOffset, int yCenterOffset) {
		this(new DummyContainer(null, null), xSize, ySize, xCenterOffset, yCenterOffset);
	}

	public LogisticsBaseGuiScreen(Container container) {
		super(container);
		xCenterOffset = 0;
		yCenterOffset = 0;
	}

	public LogisticsBaseGuiScreen(Container container, int xSize, int ySize, int xCenterOffset, int yCenterOffset) {
		super(container);
		this.xSize = xSize;
		this.ySize = ySize;
		this.xCenterOffset = xCenterOffset;
		this.yCenterOffset = yCenterOffset;
	}

	@Override
	public void initGui() {
		super.initGui();
		guiLeft = width / 2 - xSize / 2 + xCenterOffset;
		guiTop = height / 2 - ySize / 2 + yCenterOffset;

		right = width / 2 + xSize / 2 + xCenterOffset;
		bottom = height / 2 + ySize / 2 + yCenterOffset;

		xCenter = (right + guiLeft) / 2;
		yCenter = (bottom + guiTop) / 2;
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
		if (subGui == null) {
			subGui = gui;
			subGui.register(this);
			subGui.setWorldAndResolution(mc, width, height);
			subGui.initGui();
		}
	}

	@Override
	public void setWorldAndResolution(Minecraft mc, int width, int height) {
		super.setWorldAndResolution(mc, width, height);
		if (subGui != null) {
			subGui.setWorldAndResolution(mc, width, height);
		}
	}

	@Override
	public void resetSubGui() {
		subGui = null;
	}

	@Override
	public void drawDefaultBackground() {
		if (subGui == null) {
			super.drawDefaultBackground();
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		currentDrawScreenMouseX = mouseX;
		currentDrawScreenMouseY = mouseY;
		checkButtons();
		if (subGui != null) {
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
				if (LPConstants.DEBUG) {
					e.printStackTrace();
				}
			}
			//Draw super class (maybe NEI)
			super.drawScreen(0, 0, partialTicks);
			//Resore Mouse Pos
			try {
				Field fX = Mouse.class.getDeclaredField("x");
				Field fY = Mouse.class.getDeclaredField("y");
				fX.setAccessible(true);
				fY.setAccessible(true);
				fX.set(null, x);
				fY.set(null, y);
			} catch (Exception e) {
				if (LPConstants.DEBUG) {
					e.printStackTrace();
				}
			}
			GL11.glPushAttrib(GL11.GL_DEPTH_BUFFER_BIT);
			if (!subGui.hasSubGui()) {
				GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
				super.drawDefaultBackground();
			}
			GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);
			subGui.drawScreen(mouseX, mouseY, partialTicks);
			GL11.glPopAttrib();
		} else {
			drawDefaultBackground();
			super.drawScreen(mouseX, mouseY, partialTicks);
			RenderHelper.disableStandardItemLighting();
			for (IRenderSlot slot : slots) {
				int localMouseX = mouseX - guiLeft;
				int localMouseY = mouseY - guiTop;
				int mouseXMax = localMouseX - slot.getSize();
				int mouseYMax = localMouseY - slot.getSize();
				if (slot.getXPos() < localMouseX && slot.getXPos() > mouseXMax && slot.getYPos() < localMouseY && slot.getYPos() > mouseYMax) {
					if (slot.displayToolTip()) {
						if (slot.getToolTipText() != null && !slot.getToolTipText().equals("")) {
							ArrayList<String> list = new ArrayList<>();
							list.add(slot.getToolTipText());
							GuiGraphics.drawToolTip(mouseX, mouseY, list, TextFormatting.WHITE);
						}
					}
				}
			}
			this.renderHoveredToolTip(mouseX, mouseY);
			RenderHelper.enableStandardItemLighting();
		}
		Runnable run = renderAtTheEnd.poll();
		while (run != null) {
			run.run();
			run = renderAtTheEnd.poll();
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
	protected void drawSlot(Slot slot) {
		if (extentionControllerLeft.renderSlot(slot) && extentionControllerRight.renderSlot(slot)) {
			if (subGui == null) {
				onRenderSlot(slot);
			}
			super.drawSlot(slot);
		}
	}

	private void onRenderSlot(Slot slot) {
		if (slot instanceof IFuzzySlot) {
			final DictResource resource = ((IFuzzySlot) slot).getFuzzyFlags();
			int x1 = slot.xPos;
			int y1 = slot.yPos;
			GL11.glDisable(GL11.GL_LIGHTING);
			if (resource.use_od) {
				Gui.drawRect(x1 + 8, y1 - 1, x1 + 17, y1, 0xFFFF4040);
				Gui.drawRect(x1 + 16, y1, x1 + 17, y1 + 8, 0xFFFF4040);
			}
			if (resource.ignore_dmg) {
				Gui.drawRect(x1 - 1, y1 - 1, x1 + 8, y1, 0xFF40FF40);
				Gui.drawRect(x1 - 1, y1, x1, y1 + 8, 0xFF40FF40);
			}
			if (resource.ignore_nbt) {
				Gui.drawRect(x1 - 1, y1 + 16, x1 + 8, y1 + 17, 0xFF4040FF);
				Gui.drawRect(x1 - 1, y1 + 8, x1, y1 + 17, 0xFF4040FF);
			}
			if (resource.use_category) {
				Gui.drawRect(x1 + 8, y1 + 16, x1 + 17, y1 + 17, 0xFF7F7F40);
				Gui.drawRect(x1 + 16, y1 + 8, x1 + 17, y1 + 17, 0xFF7F7F40);
			}
			GL11.glEnable(GL11.GL_LIGHTING);
			final boolean mouseOver = this.isMouseOverSlot(slot, currentDrawScreenMouseX, currentDrawScreenMouseY);
			if (mouseOver) {
				if (fuzzySlot == slot) {
					fuzzySlotGuiHoverTime++;
					if (fuzzySlotGuiHoverTime >= 10) {
						fuzzySlotActiveGui = true;
					}
				} else {
					fuzzySlot = (IFuzzySlot) slot;
					fuzzySlotGuiHoverTime = 0;
					fuzzySlotActiveGui = false;
				}
			}
			if (fuzzySlotActiveGui && fuzzySlot == slot) {
				if (!mouseOver) {
					//Check within FuzzyGui
					if (!isPointInRegion(slot.xPos, slot.yPos + 16, 60, 52, currentDrawScreenMouseX, currentDrawScreenMouseY)) {
						fuzzySlotActiveGui = false;
						fuzzySlot = null;
					}
				}
				//int posX = -60;
				//int posY = 0;
				final int posX = slot.xPos + guiLeft;
				final int posY = slot.yPos + 17 + guiTop;
				renderAtTheEnd.add(() -> {
					GL11.glDisable(GL11.GL_DEPTH_TEST);
					GL11.glDisable(GL11.GL_LIGHTING);
					GuiGraphics.drawGuiBackGround(mc, posX, posY, posX + 60, posY + 52, zLevel, true, true, true, true, true);
					final String PREFIX = "gui.crafting.";
					mc.fontRenderer.drawString(StringUtils.translate(PREFIX + "OreDict"), posX + 4, posY + 4, (!resource.use_od ? 0x404040 : 0xFF4040));
					mc.fontRenderer.drawString(StringUtils.translate(PREFIX + "IgnDamage"), posX + 4, posY + 14, (!resource.ignore_dmg ? 0x404040 : 0x40FF40));
					mc.fontRenderer.drawString(StringUtils.translate(PREFIX + "IgnNBT"), posX + 4, posY + 26, (!resource.ignore_nbt ? 0x404040 : 0x4040FF));
					mc.fontRenderer.drawString(StringUtils.translate(PREFIX + "OrePrefix"), posX + 4, posY + 38, (!resource.use_category ? 0x404040 : 0x7F7F40));
					GL11.glEnable(GL11.GL_LIGHTING);
					GL11.glEnable(GL11.GL_DEPTH_TEST);
				});
			}
		}
	}

	@Override
	protected boolean isMouseOverSlot(Slot par1Slot, int par2, int par3) {
		if (!extentionControllerLeft.renderSelectSlot(par1Slot)) {
			return false;
		}
		if (!extentionControllerRight.renderSelectSlot(par1Slot)) {
			return false;
		}
		if (isMouseInFuzzyPanel(currentDrawScreenMouseX, currentDrawScreenMouseY)) return false;
		return super.isMouseOverSlot(par1Slot, par2, par3);
	}

	private boolean isMouseInFuzzyPanel(int x, int y) {
		if (!fuzzySlotActiveGui || fuzzySlot == null) return false;
		return isPointInRegion(fuzzySlot.getX(), fuzzySlot.getY() + 16, 60, 52, x, y);
	}

	@SuppressWarnings("unchecked")
	protected void checkButtons() {
		for (GuiButton button : buttonList) {
			if (extentionControllerLeft.renderButtonControlled(button)) {
				button.visible = extentionControllerLeft.renderButton(button);
			}
			if (extentionControllerRight.renderButtonControlled(button)) {
				button.visible = extentionControllerRight.renderButton(button);
			}
		}
	}

	@Nonnull
	@SuppressWarnings("unchecked")
	public <T extends GuiButton> T addButton(@Nonnull T button) {
		buttonList.add(button);
		return button;
	}

	@Override
	public final void handleMouseInput() throws IOException {
		if (subGui != null) {
			subGui.handleMouseInput();
		} else {
			handleMouseInputSub();
		}
	}

	public void handleMouseInputSub() throws IOException {
		super.handleMouseInput();
		int x = Mouse.getEventX() * this.width / this.mc.displayWidth;
		int y = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
		int dWheel = Mouse.getEventDWheel();
		if (dWheel != 0 && !mouseHandled) {
			Optional<DummySlot> slotOpt = this.inventorySlots.inventorySlots.stream().filter(it -> it instanceof DummySlot).map(it -> (DummySlot) it).filter(it -> isMouseOverSlot(it, x, y)).findFirst();
			if (slotOpt.isPresent()) {
				DummySlot slot = slotOpt.get();
				slot.setRedirectCall(true);
				if (slot.getSlotStackLimit() > 0) {
					ItemStack stack = slot.getStack();
					if (!stack.isEmpty()) {
						int buttonActionID = dWheel > 0 ? 1000 : 1001;
						this.mc.playerController.windowClick(this.inventorySlots.windowId, slot.slotNumber, buttonActionID, ClickType.SWAP, this.mc.player);
					}
				}
				slot.setRedirectCall(false);
				mouseHandled = true;
			}
		}
	}

	@Override
	public final void handleKeyboardInput() throws IOException {
		if (subGui != null) {
			subGui.handleKeyboardInput();
		} else {
			super.handleKeyboardInput();
		}
		for (EventListener el : onGuiEvents)
			keyHandled |= el.onKeyboardInput();
	}

	public void addRenderSlot(IRenderSlot slot) {
		slots.add(slot);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		if (par1 < guiLeft) {
			extentionControllerLeft.mouseOver(par1, par2);
		}
		if (par1 > guiLeft + xSize) {
			extentionControllerRight.mouseOver(par1, par2);
		}
		for (IRenderSlot slot : slots) {
			if (slot instanceof IItemTextureRenderSlot) {
				if (slot.drawSlotBackground()) {
					GuiGraphics.drawSlotBackground(mc, slot.getXPos(), slot.getYPos());
				}
				if (((IItemTextureRenderSlot) slot).drawSlotIcon() && !((IItemTextureRenderSlot) slot).customRender(mc, zLevel)) {
					GuiGraphics.renderIconAt(mc, slot.getXPos() + 1, slot.getYPos() + 1, zLevel, ((IItemTextureRenderSlot) slot).getTextureIcon());
				}
			} else if (slot instanceof ISmallColorRenderSlot) {
				if (slot.drawSlotBackground()) {
					GuiGraphics.drawSmallSlotBackground(mc, slot.getXPos(), slot.getYPos());
				}
				if (((ISmallColorRenderSlot) slot).drawColor()) {
					Gui.drawRect(slot.getXPos() + 1, slot.getYPos() + 1, slot.getXPos() + 7, slot.getYPos() + 7, ((ISmallColorRenderSlot) slot).getColor());
				}
			}
		}
	}

	@Override
	protected void mouseClicked(int par1, int par2, int par3) throws IOException {
		for (IRenderSlot slot : slots) {
			int mouseX = par1 - guiLeft;
			int mouseY = par2 - guiTop;
			int mouseXMax = mouseX - slot.getSize();
			int mouseYMax = mouseY - slot.getSize();
			if (slot.getXPos() < mouseX && slot.getXPos() > mouseXMax && slot.getYPos() < mouseY && slot.getYPos() > mouseYMax) {
				slot.mouseClicked(par3);
				return;
			}
		}
		if (isMouseInFuzzyPanel(par1, par2)) {
			final int posX = fuzzySlot.getX() + guiLeft;
			final int posY = fuzzySlot.getY() + 17 + guiTop;
			int sel = -1;
			if (par1 >= posX + 4 && par1 <= posX + 60 - 4) {
				if (par2 >= posY + 4 && par2 <= posY + 52 - 4) {
					sel = (par2 - posY - 4) / 11;
				}
			}
			DictResource resource = fuzzySlot.getFuzzyFlags();
			BitSet set = resource.getBitSet();
			if (sel == 0) {
				resource.use_od = !resource.use_od;
			}
			if (sel == 1) {
				resource.ignore_dmg = !resource.ignore_dmg;
			}
			if (sel == 2) {
				resource.ignore_nbt = !resource.ignore_nbt;
			}
			if (sel == 3) {
				resource.use_category = !resource.use_category;
			}
			MainProxy.sendPacketToServer(PacketHandler.getPacket(FuzzySlotSettingsPacket.class).setSlotNumber(fuzzySlot.getSlotId()).setFlags(resource.getBitSet()));
			resource.loadFromBitSet(set); // Reset to wait for server
			return;
		}
		boolean handledButton = false;
		if (par3 == 0) {
			for (Object aButtonList : buttonList) {
				GuiButton guibutton = (GuiButton) aButtonList;
				if (guibutton.mousePressed(mc, par1, par2)) {
					selectedButton = guibutton;
					guibutton.playPressSound(mc.getSoundHandler());
					actionPerformed(guibutton);
					handledButton = true;
					break;
				}
			}
		}
		if (!handledButton) {
			super.mouseClicked(par1, par2, par3);
		}
		if (par3 == 0 && par1 < guiLeft && !mouseCanPressButton(par1, par2) && !isOverSlot(par1, par2)) {
			extentionControllerLeft.mouseClicked(par1, par2, par3);
		}
		if (par3 == 0 && par1 > guiLeft + xSize && !mouseCanPressButton(par1, par2) && !isOverSlot(par1, par2)) {
			extentionControllerRight.mouseClicked(par1, par2, par3);
		}
	}

	@Override
	protected void mouseReleased(int par1, int par2, int par3) {
		if (selectedButton != null && par3 == 0) {
			selectedButton.mouseReleased(par1, par2);
			selectedButton = null;
		} else if (isMouseInFuzzyPanel(par1 - guiLeft, par2 - guiTop)) {
		} else {
			super.mouseReleased(par1, par2, par3);
		}
	}

	private boolean mouseCanPressButton(int par1, int par2) {
		for (Object aButtonList : buttonList) {
			GuiButton guibutton = (GuiButton) aButtonList;
			if (guibutton.mousePressed(mc, par1, par2)) {
				return true;
			}
		}
		return false;
	}

	private boolean isOverSlot(int par1, int par2) {
		for (int k = 0; k < inventorySlots.inventorySlots.size(); ++k) {
			Slot slot = inventorySlots.inventorySlots.get(k);
			if (isMouseOverSlot(slot, par1, par2)) {
				return true;
			}
		}
		return false;
	}

	public void drawPoint(int x, int y, int color) {
		Gui.drawRect(x, y, x + 1, y + 1, color);
	}

	public void drawPoint(int x, int y, Color color) {
		Gui.drawRect(x, y, x + 1, y + 1, Color.getValue(color));
	}

	public void drawRect(int x1, int y1, int x2, int y2, Color color) {
		Gui.drawRect(x1, y1, x2, y2, Color.getValue(color));
	}

	public void drawLine(int x1, int y1, int x2, int y2, Color color) {
		int lasty = y1;
		for (int dx = 0; x1 + dx < x2; dx++) {
			int plotx = x1 + dx;
			int ploty;

			if (x2 - x1 == 1) ploty = y1 + (y2 - y1) / (x2 - x1) * dx;
			else ploty = y1 + (y2 - y1) / (x2 - x1 - 1) * dx;

			drawPoint(plotx, ploty, color);
			while (lasty < ploty) {
				drawPoint(plotx, ++lasty, color);
			}
			while (lasty > ploty) {
				drawPoint(plotx, --lasty, color);
			}
		}
		while (lasty < y2) {
			drawPoint(x2, ++lasty, color);
		}
		while (lasty > y2) {
			drawPoint(x2, --lasty, color);
		}
	}

	public void closeGui() throws IOException {
		keyTyped(' ', 1);
	}

	@Override
	public Minecraft getMC() {
		return mc;
	}

	@Override
	public LogisticsBaseGuiScreen getBaseScreen() {
		return this;
	}

	@Override
	@ModDependentMethod(modId = LPConstants.neiModID)
	public List<TaggedInventoryArea> getInventoryAreas(GuiContainer gui) {
		return null;
	}

	@Override
	@ModDependentMethod(modId = LPConstants.neiModID)
	public Iterable<Integer> getItemSpawnSlots(GuiContainer gui, ItemStack stack) {
		return null;
	}

	@Override
	@ModDependentMethod(modId = LPConstants.neiModID)
	public boolean handleDragNDrop(GuiContainer gui, int mouseX, int mouseY, @Nonnull ItemStack stack, int button) {
		if (gui instanceof LogisticsBaseGuiScreen && gui.inventorySlots instanceof DummyContainer && !stack.isEmpty()) {
			Slot result = null;
			int pos = -1;
			for (int k = 0; k < inventorySlots.inventorySlots.size(); ++k) {
				Slot slot = inventorySlots.inventorySlots.get(k);
				if (isMouseOverSlot(slot, mouseX, mouseY)) {
					result = slot;
					pos = k;
					break;
				}
			}
			if (result != null) {
				if (result instanceof DummySlot || result instanceof ColorSlot || result instanceof FluidSlot) {
					((DummyContainer) gui.inventorySlots).handleDummyClick(result, pos, stack, button, ClickType.PICKUP, mc.player);
					MainProxy.sendPacketToServer(PacketHandler.getPacket(DummyContainerSlotClick.class).setSlotId(pos).setStack(stack).setButton(button));
					return true;
				}
			}
		}
		return false;
	}

	@Override
	@ModDependentMethod(modId = LPConstants.neiModID)
	public boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int w, int h) {
		if (gui instanceof LogisticsBaseGuiScreen) {
			return ((LogisticsBaseGuiScreen) gui).extentionControllerRight.isOverPanel(x, y, w, h);
		}
		return false;
	}

	public IChainAddList<EventListener> onGuiEvents = new ChainAddArrayList<>();

	public interface EventListener {

		void onUpdateScreen();

		boolean onKeyboardInput();

	}

	public void updateScreen() {
		for (EventListener el : onGuiEvents)
			el.onUpdateScreen();
	}

}
