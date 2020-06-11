package logisticspipes.logic.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import logisticspipes.logic.LogicController;
import logisticspipes.routing.order.IOrderInfoProvider;
import logisticspipes.routing.order.LinkedLogisticsOrderList;
import logisticspipes.utils.Color;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import logisticspipes.utils.gui.SimpleGraphics;
import logisticspipes.utils.string.ChatColor;

public class LogicLayoutGui extends LogisticsBaseGuiScreen {

	private enum ZOOM_LEVEL {
		NORMAL(1, 165, 224, 1, 0),
		LEVEL_1(0.5F, 330, 465, 1, 50),
		LEVEL_2(0.25F, 660, 950, 2, 100);

		ZOOM_LEVEL(float zoom, int bottom, int right, int line, int moveY) {
			this.zoom = zoom;
			bottomRenderBorder = bottom;
			rightRenderBorder = right;
			this.line = line;
			this.moveY = moveY;
		}

		final float zoom;
		final int bottomRenderBorder;
		final int rightRenderBorder;
		final int line;
		final int moveY;

		ZOOM_LEVEL next() {
			int id = ordinal();
			if (id + 1 >= ZOOM_LEVEL.values().length) {
				return this;
			} else {
				return ZOOM_LEVEL.values()[id + 1];
			}
		}

		ZOOM_LEVEL prev() {
			int id = ordinal();
			if (id - 1 < 0) {
				return this;
			} else {
				return ZOOM_LEVEL.values()[id - 1];
			}
		}
	}

	private static final ResourceLocation achievementTextures = new ResourceLocation("textures/gui/achievement/achievement_background.png");

	private final ItemRenderer renderitem = new ItemRenderer(mc);

	private int isMouseButtonDown;
	private int mouseX;
	private int mouseY;
	private double guiMapX;
	private double guiMapY;
	private ZOOM_LEVEL zoom = ZOOM_LEVEL.NORMAL;

	private Object[] tooltip = null;

	public LogicLayoutGui(LogicController controller, EntityPlayer player) {
		super(256, 202 + 90, 0, 0);
		guiMapY = -200;
		Mouse.getDWheel(); // Reset DWheel on GUI open
		DummyContainer dummy = new DummyContainer(player.inventory, null);
		dummy.addNormalSlotsForPlayerInventory(50, 205);
		inventorySlots = dummy;
	}

	@Override
	public void initGui() {
		super.initGui();
		/*
		buttonList.clear();
		this.buttonList.add(new GuiButton(0, this.width / 2 + 45, this.height / 2 + 74, 80, 20, "Close"));
		 */
	}

	@Override
	protected void actionPerformed(GuiButton button) {}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (Mouse.isButtonDown(0)) {
			int k = (width - xSize) / 2;
			int l = (height - ySize) / 2;
			int i1 = k + 8;
			int j1 = l + 17;

			if ((isMouseButtonDown == 0 || isMouseButtonDown == 1) && mouseX >= i1 && mouseX < i1 + 224 && mouseY >= j1 && mouseY < j1 + 155) {
				if (isMouseButtonDown == 0) {
					isMouseButtonDown = 1;
				} else {
					guiMapX -= (double) (mouseX - this.mouseX) / zoom.zoom;
					guiMapY -= (double) (mouseY - this.mouseY) / zoom.zoom;
				}

				this.mouseX = mouseX;
				this.mouseY = mouseY;
			}

		} else {
			isMouseButtonDown = 0;
		}

		int dWheel = Mouse.getDWheel();
		if (dWheel < 0) {
			zoom = zoom.next();
		} else if (dWheel > 0) {
			zoom = zoom.prev();
		}
		GL11.glTranslatef(0.0F, 0.0F, 100.0F);
		if (tooltip != null) {
			GuiGraphics.displayItemToolTip(tooltip, zLevel, guiLeft, guiTop, true);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		super.drawGuiContainerBackgroundLayer(f, i, j);
		drawTransparentBack();
		drawMap(i, j);
		GuiGraphics.drawGuiBackGround(getMC(), guiLeft, guiTop + 180, right, bottom, zLevel, true, false, true, true, true);
		GuiGraphics.drawPlayerInventoryBackground(getMC(), guiLeft + 50, guiTop + 205);
	}

	private void drawTransparentBack() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		SimpleGraphics.drawGradientRect(0, 0, width, height, Color.BLANK, Color.BLANK, 0.0);
	}

	private void drawMap(int par1, int par2) {
		tooltip = null;
		int mapX = (int) Math.floor(guiMapX);
		int mapY = (int) Math.floor(guiMapY - zoom.moveY);
		int leftSide = ((width - xSize) / 2);
		int topSide = ((height - ySize) / 2);

		GL11.glTranslatef(0.0F, 0.0F, 100.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(LogicLayoutGui.achievementTextures);
		drawTexturedModalRect(leftSide, topSide, 0, 0, 256, 202);
		GL11.glTranslatef(0.0F, 0.0F, -100.0F);

		guiTop *= 1 / zoom.zoom;
		guiLeft *= 1 / zoom.zoom;
		xSize *= 1 / zoom.zoom;
		ySize *= 1 / zoom.zoom;
		leftSide *= 1 / zoom.zoom;
		topSide *= 1 / zoom.zoom;

		int innerLeftSide = leftSide + 16;
		int innerTopSide = topSide + 17;
		zLevel = 0.0F;

		GL11.glDepthFunc(GL11.GL_GEQUAL);
		GL11.glPushMatrix();
		GL11.glScalef(zoom.zoom, zoom.zoom, 1);
		GL11.glTranslatef(0.0F, 0.0F, -100.0F);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);

		int moveBackgroundX = (mapX) % 16 + (mapX < 0 ? 16 : 0);
		int moveBackgroundY = (mapY) % 16 + (mapY < 0 ? 16 : 0);
		GL11.glColor4f(0.7F, 0.7F, 0.7F, 1.0F);
		for (int yVar = 0; yVar * 16 - moveBackgroundY < zoom.bottomRenderBorder; yVar++) {
			for (int xVar = 0; xVar * 16 - moveBackgroundX < zoom.rightRenderBorder; xVar++) {
				/*
				TextureAtlasSprite icon = Blocks.STONE.getIcon(0, 0);
				mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
				drawTexturedModalRect(innerLeftSide + xVar * 16 - moveBackgroundX, innerTopSide + yVar * 16 - moveBackgroundY, icon, 16, 16);
				*/
			}
		}

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glDisable(GL11.GL_TEXTURE_2D);

		GL11.glPushMatrix();
		GL11.glLoadIdentity();
		//Draw Content
		//Lines

		//Draw Background

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);

		RenderHelper.enableGUIStandardItemLighting();
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glColor4f(0.7F, 0.7F, 0.7F, 1.0F);

		mc.getTextureManager().bindTexture(LogicLayoutGui.achievementTextures);

		//Draw Content
		//Items

		GL11.glPopMatrix();

		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_BLEND);

		guiTop *= zoom.zoom;
		guiLeft *= zoom.zoom;
		xSize *= zoom.zoom;
		ySize *= zoom.zoom;
		leftSide *= zoom.zoom;
		topSide *= zoom.zoom;

		GL11.glScalef(1 / zoom.zoom, 1 / zoom.zoom, 1);

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(LogicLayoutGui.achievementTextures);
		drawTexturedModalRect(leftSide, topSide, 0, 0, 256, 202);

		GL11.glPopMatrix();
		zLevel = 0.0F;
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		//GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_LIGHTING);
		RenderHelper.disableStandardItemLighting();
	}

	private void renderLinkedOrderListItems(LinkedLogisticsOrderList list, int xPos, int yPos, int par1, int par2) {
		int size = list.size();
		int startLeft = -(size - 1) * (30 / 2) + xPos;
		yPos += 13;
		for (IOrderInfoProvider aList : list) {
			if (aList.isInProgress()) {
				GL11.glColor4f(0.1F, 0.9F, 0.1F, 1.0F);
			} else {
				GL11.glColor4f(0.7F, 0.7F, 0.7F, 1.0F);
			}
			GL11.glEnable(GL11.GL_LIGHTING);
			mc.getTextureManager().bindTexture(LogicLayoutGui.achievementTextures);
			drawTexturedModalRect(startLeft - 5, yPos - 5, 0, 202, 26, 26);
			GL11.glColor4f(0.7F, 0.7F, 0.7F, 1.0F);
			//renderItemAt(aList.getAsDisplayItem(), startLeft, yPos);
			if (aList.isInProgress() && aList.getMachineProgress() != 0) {
				Gui.drawRect(startLeft - 4, yPos + 20, startLeft + 20, yPos + 24, 0xff000000);
				Gui.drawRect(startLeft - 3, yPos + 21, startLeft + 19, yPos + 23, 0xffffffff);
				Gui.drawRect(startLeft - 3, yPos + 21, startLeft - 3 + (22 * aList
						.getMachineProgress() / 100), yPos + 23, 0xffff0000);
			}
			if (startLeft - 10 < par1 && par1 < startLeft + 20 && yPos - 6 < par2 && par2 < yPos + 20) {
				if (guiLeft < par1 && par1 < guiLeft + xSize - 16 && guiTop < par2 && par2 < guiTop + ySize - 16) {
					IOrderInfoProvider order = aList;
					List<String> tooltipList = new ArrayList<>();
					tooltipList.add(ChatColor.BLUE + "Request Type: " + ChatColor.YELLOW + order.getType().name());
					tooltipList.add(ChatColor.BLUE + "Send to Router ID: " + ChatColor.YELLOW + order.getRouterId());
					tooltip = new Object[] { (int) (par1 * zoom.zoom - 10), (int) (par2 * zoom.zoom), order
							.getAsDisplayItem().makeNormalStack(), true, tooltipList };
				}
			}
			startLeft += 30;
		}
		startLeft = xPos + 20 - list.getSubTreeRootSize() * (40 / 2);
		if (!list.getSubOrders().isEmpty()) {
			for (int i = 0; i < list.getSubOrders().size(); i++) {
				startLeft += list.getSubOrders().get(i).getTreeRootSize() * (40 / 2);
				renderLinkedOrderListItems(list.getSubOrders().get(i), startLeft - 20, yPos + 48, par1, par2);
				startLeft += list.getSubOrders().get(i).getTreeRootSize() * (40 / 2);
			}
		}
	}

	private void renderLinkedOrderListLines(LinkedLogisticsOrderList list, int xPos, int yPos) {
		int size = list.size();
		if (list.isEmpty()) {
			size = 1;
		}
		int startLeft = -(size - 1) * (30 / 2) + xPos;
		yPos += 13;
		int left = startLeft;
		for (int i = 0; i < list.size(); i++) {
			SimpleGraphics.drawVerticalLine(startLeft + 8, yPos - 13, yPos - 3, Color.GREEN, zoom.line);
			if (!list.getSubOrders().isEmpty()) {
				SimpleGraphics.drawVerticalLine(startLeft + 8, yPos + 18, yPos + 28, Color.GREEN, zoom.line);
			}
			startLeft += 30;
		}
		if (!list.isEmpty()) {
			SimpleGraphics.drawHorizontalLine(left + 8, startLeft - 22, yPos - 13, Color.GREEN, zoom.line);
		}
		if (!list.getSubOrders().isEmpty()) {
			if (!list.isEmpty()) {
				SimpleGraphics.drawHorizontalLine(left + 8, startLeft - 22, yPos + 28, Color.GREEN, zoom.line);
				startLeft -= 30;
			}
			SimpleGraphics.drawVerticalLine(left + ((startLeft - left) / 2) + 8, yPos + 28, yPos + 38, Color.GREEN, zoom.line);
			startLeft = xPos + 20 - list.getSubTreeRootSize() * (40 / 2);
			left = startLeft;
			for (int i = 0; i < list.getSubOrders().size(); i++) {
				startLeft += list.getSubOrders().get(i).getTreeRootSize() * (40 / 2);
				SimpleGraphics.drawVerticalLine(startLeft - 12, yPos + 38, yPos + 48, Color.GREEN, zoom.line);
				drawPointFor(list, xPos, yPos, i, startLeft);
				renderLinkedOrderListLines(list.getSubOrders().get(i), startLeft - 20, yPos + 48);
				startLeft += list.getSubOrders().get(i).getTreeRootSize() * (40 / 2);
			}
			if (!list.getSubOrders().isEmpty()) {
				left += list.getSubOrders().get(0).getTreeRootSize() * (40 / 2);
				startLeft -= list.getSubOrders().get(list.getSubOrders().size() - 1).getTreeRootSize() * (40 / 2);
			}
			SimpleGraphics.drawHorizontalLine(left - 12, startLeft - 12, yPos + 38, Color.GREEN, zoom.line);
		}
	}

	private void drawPointFor(LinkedLogisticsOrderList list, int xPos, int yPos, int i, int startLeft) {
		float totalLine = 10 + 1 + 10 + 1 + Math.abs(startLeft - (xPos + 20)) + 10 + 1 + 10;
		for (Float point : list.getSubOrders().get(i).getProgresses()) {
			int pos = (int) (totalLine * (1.0F - point));
			if (pos < 13) {
				int newSize = list.getSubOrders().get(i).size();
				int newStartLeft = -(newSize - 1) * (30 / 2) + startLeft - 20;
				for (int j = 0; j < newSize; j++) {
					drawProgressPoint(newStartLeft + 8, yPos + 48 + 12 - pos, 0xff00ff00);
					newStartLeft += 30;
				}
			} else if (pos < 10 + 1 + 10 + 1) {
				pos -= 10;
				drawProgressPoint(startLeft - 20 + 8, yPos + 38 + 12 - pos, 0xff00ff00);
			} else if (pos < Math.abs(startLeft - (xPos + 20)) + 10 + 1 + 10 + 1) {
				pos -= 10 + 1 + 10 + 1;
				if (startLeft < xPos + 20) {
					pos *= -1;
				}
				drawProgressPoint(startLeft - 12 - pos, yPos + 38, 0xff00ff00);
			} else if (pos < Math.abs(startLeft - (xPos + 20)) + 10 + 1 + 10 + 1 + 10 + 1) {
				pos -= 10 + 1 + 10 + 1 + Math.abs(startLeft - (xPos + 20)) + 10 + 1;
				drawProgressPoint(xPos + 8, yPos + 27 - pos, 0xff00ff00);
			} else if (pos < Math.abs(startLeft - (xPos + 20)) + 10 + 1 + 10 + 1 + 10 + 1 + 10 + 1) {
				pos -= 10 + 1 + 10 + 1 + Math.abs(startLeft - (xPos + 20)) + 10 + 1 + 10 + 1;
				int newSize = list.size();
				int newStartLeft = -(newSize - 1) * (30 / 2) + xPos;
				for (int j = 0; j < newSize; j++) {
					drawProgressPoint(newStartLeft + 8, yPos + 16 - pos, 0xff00ff00);
					newStartLeft += 30;
				}
			}
		}
	}

	protected void drawProgressPoint(int x, int y, int color) {
		int line = zoom.line + 1;
		Gui.drawRect(x - line + 1, y - line + 1, x + line, y + line, color);
	}
}
