package logisticspipes.gui.popup;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.imageio.ImageIO;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.routing.order.IOrderInfoProvider;
import logisticspipes.routing.order.LinkedLogisticsOrderList;
import logisticspipes.utils.Color;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.SimpleGraphics;
import logisticspipes.utils.gui.SubGuiScreen;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.string.ChatColor;
import logisticspipes.utils.string.StringUtils;

public class RequestMonitorPopup extends SubGuiScreen {

	private enum ZOOM_LEVEL {
		NORMAL(1, 165, 224, 1, 0, 0, 0),
		LEVEL_1(0.5F, 330, 465, 1, 50, -200, 100),
		LEVEL_2(0.25F, 660, 950, 2, 100, -400, -100);

		ZOOM_LEVEL(float zoom, int bottom, int right, int line, int moveY, int maxX, int maxY) {
			this.zoom = zoom;
			bottomRenderBorder = bottom;
			rightRenderBorder = right;
			this.line = line;
			this.moveY = moveY;
			this.maxX = maxX;
			this.maxY = maxY;
		}

		final float zoom;
		final int bottomRenderBorder;
		final int rightRenderBorder;
		final int line;
		final int moveY;
		final int maxX;
		final int maxY;

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

	private static final ResourceLocation achievementTextures = new ResourceLocation("logisticspipes", "textures/gui/gui_border.png");
	private final PipeBlockRequestTable _table;
	private final int orderId;

	private int isMouseButtonDown;
	private int mouseX;
	private int mouseY;
	private double guiMapX;
	private double guiMapY;
	private int minY = -230;
	private int maxY = 0;
	private int minX = -800;
	private int maxX = 800;
	private ZOOM_LEVEL zoom = ZOOM_LEVEL.NORMAL;
	private Object[] tooltip = null;

	public RequestMonitorPopup(PipeBlockRequestTable table, int orderId) {
		super(256, 202, 0, 0);
		_table = table;
		this.orderId = orderId;
		guiMapY = -200;
		Mouse.getDWheel(); // Reset DWheel on GUI open
	}

	private static void mirror(int[] par0ArrayOfInteger, int width, int height) {
		int[] aint1 = new int[width];
		int k = height / 2;

		for (int l = 0; l < k; ++l) {
			System.arraycopy(par0ArrayOfInteger, l * width, aint1, 0, width);
			System.arraycopy(par0ArrayOfInteger, (height - 1 - l) * width, par0ArrayOfInteger, l * width, width);
			System.arraycopy(aint1, 0, par0ArrayOfInteger, (height - 1 - l) * width, width);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		buttonList.add(new GuiButton(0, width / 2 - 90, height / 2 + 74, 80, 20, "Close"));
		buttonList.add(new GuiButton(1, width / 2 + 10, height / 2 + 74, 80, 20, "Save as Image"));
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == 0) {
			exitGui();
		} else if (button.id == 1) {
			saveTreeToImage();
		}
	}

	@Override
	protected void renderToolTips(int mouseX, int mouseY, float par3) {
		if (tooltip != null) {
			GuiGraphics.displayItemToolTip(tooltip, zLevel, guiLeft, guiTop, true);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {

	}

	@Override
	protected void renderGuiBackground(int mouseX, int mouseY) {
		if (!_table.watchedRequests.containsKey(orderId)) {
			exitGui();
			return;
		}
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

		if (guiMapY < minY) {
			guiMapY = minY;
		}
		if (guiMapY > maxY) {
			guiMapY = maxY;
		}
		if (guiMapX > maxX) {
			guiMapX = maxX;
		}
		if (guiMapX < minX) {
			guiMapX = minX;
		}

		int dWheel = Mouse.getDWheel();
		if (dWheel < 0) {
			zoom = zoom.next();
		} else if (dWheel > 0) {
			zoom = zoom.prev();
		}
		createBoundary();
		drawTransparentBack();
		drawMap(mouseX, mouseY);
	}

	private void createBoundary() {
		int size = _table.watchedRequests.get(orderId).getValue2().getTreeRootSize();
		minX = -size * (40 / 2) + (int) (75 * (zoom.zoom));
		maxX = -minX + zoom.maxX;
		maxY = -100;
		findLowest(_table.watchedRequests.get(orderId).getValue2(), -200);
	}

	private void drawTransparentBack() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		SimpleGraphics.drawGradientRect(0, 0, width, height, Color.BLANK, Color.BLANK, 0.0);
	}

	private void findLowest(LinkedLogisticsOrderList list, int lowerLimit) {
		lowerLimit += 48;
		for (LinkedLogisticsOrderList sub : list.getSubOrders()) {
			findLowest(sub, lowerLimit);
		}
		if (maxY < (lowerLimit + 10) * zoom.zoom) {
			maxY = (int) ((lowerLimit + 10) * zoom.zoom) + zoom.maxY;
		}
	}

	private void saveTreeToImage() {
		int useWidth = mc.displayWidth;
		int useHeight = mc.displayHeight;
		int left = minX - (width / 2);
		int top = minY;
		int right = maxX - (width / 2);
		int bottom = maxY;

		int k = useWidth * useHeight;
		IntBuffer pixels = BufferUtils.createIntBuffer(k);
		int[] intArray = new int[k];

		int imgPosX = 0;
		int imgPosY = 0;
		for (int x = left; x < right + width; x += width) {
			imgPosY = 0;
			for (int y = top; y < bottom + height; y += height) {
				imgPosY += useHeight;
			}
			imgPosX += useWidth;
		}
		BufferedImage bufferedimage = new BufferedImage(imgPosX, imgPosY, 1);

		imgPosX = 0;

		//Clear everything
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glLoadIdentity();
		mc.entityRenderer.setupOverlayRendering();
		drawForSreenShot(0, 0);

		//Start Creating the Image
		for (int x = left; x < right + width; x += width) {
			imgPosY = 0;
			for (int y = top; y < bottom + height; y += height) {
				GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
				mc.entityRenderer.setupOverlayRendering();
				drawForSreenShot(y, x);
				pixels.clear();
				GL11.glReadPixels(0, 0, useWidth, useHeight, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixels);
				pixels.get(intArray);
				RequestMonitorPopup.mirror(intArray, useWidth, useHeight);
				bufferedimage.setRGB(imgPosX, imgPosY, Math.min(useWidth, bufferedimage.getWidth() - imgPosX), Math.min(useHeight, bufferedimage.getHeight() - imgPosY), intArray, 0, useWidth);
				imgPosY += useHeight;
			}
			imgPosX += useWidth;
		}
		saveImage(bufferedimage);
	}

	private void saveImage(BufferedImage bufferedimage) {
		File screenShotsFolder = new File(Minecraft.getMinecraft().mcDataDir, "screenshots");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
		String s = dateFormat.format(new Date());
		int i = 1;
		while (true) {
			File canidate = new File(screenShotsFolder, s + (i == 1 ? "" : "_" + i) + ".png");
			if (!canidate.exists()) {
				try {
					ImageIO.write(bufferedimage, "png", canidate);
					Minecraft.getMinecraft().player.sendChatMessage("Saved tree view as " + canidate.getName());
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
			++i;
		}
	}

	private void drawForSreenShot(int top, int left) {
		left *= -1;
		top *= -1;
		GL11.glPushMatrix();
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glTranslated(0, 0, 100);
		Gui.drawRect(0, 0, 10000, 10000, 0xff000000);
		GL11.glColor4f(0.7F, 0.7F, 0.7F, 1.0F);
		for (int yVar = 0; yVar * 16 < height; yVar++) {
			for (int xVar = 0; xVar * 16 < width; xVar++) {
				TextureAtlasSprite icon = getTexture(Blocks.STONE);
				mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
				drawTexturedModalRect(xVar * 16, yVar * 16, icon, 16, 16);
			}
		}
		GL11.glTranslated(0, 0, 600);

		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		LinkedLogisticsOrderList list = _table.watchedRequests.get(orderId).getValue2();
		if (!list.isEmpty()) {
			SimpleGraphics.drawVerticalLine(left + 8, top + 0, top + 17, Color.GREEN, zoom.line);
		}
		renderLinkedOrderListLines(list, left, top + 17);
		for (Float progress : list.getProgresses()) {
			int pos = (int) (29.0F * progress);
			drawProgressPoint(left + 8, top + pos, 0xff00ff00);
		}
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);

		RenderHelper.enableGUIStandardItemLighting();
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glColor4f(0.7F, 0.7F, 0.7F, 1.0F);
		mc.getTextureManager().bindTexture(RequestMonitorPopup.achievementTextures);
		String s = Integer.toString(orderId);
		if (!list.isEmpty()) {
			drawTexturedModalRect(left - 5, top - 40 + 17, 0, 202, 26, 26);
			mc.fontRenderer.drawStringWithShadow(s, left + 9 - mc.fontRenderer.getStringWidth(s) / 2, top - 30 + 17, 16777215);
		} else {
			drawTexturedModalRect(left - 5, top - 18 + 17, 0, 202, 26, 26);
			mc.fontRenderer.drawStringWithShadow(s, left + 9 - mc.fontRenderer.getStringWidth(s) / 2, top - 18 + 10 + 17, 16777215);
		}
		renderLinkedOrderListItems(list, left, top + 17, 0, 0);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_BLEND);

		GL11.glPopMatrix();
		zLevel = 0.0F;
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_LIGHTING);
		RenderHelper.disableStandardItemLighting();
	}

	private void drawMap(int par1, int par2) {
		tooltip = null;
		int mapX = (int) Math.floor(guiMapX);
		int mapY = (int) Math.floor(guiMapY - zoom.moveY);
		int leftSide = ((width - xSize) / 2);
		int topSide = ((height - ySize) / 2);

		GL11.glTranslatef(0.0F, 0.0F, 100.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(RequestMonitorPopup.achievementTextures);
		drawTexturedModalRect(leftSide, topSide, 0, 0, xSize, ySize);
		GL11.glTranslatef(0.0F, 0.0F, -100.0F);

		guiTop *= 1 / zoom.zoom;
		guiLeft *= 1 / zoom.zoom;
		xSize *= 1 / zoom.zoom;
		ySize *= 1 / zoom.zoom;
		leftSide *= 1 / zoom.zoom;
		topSide *= 1 / zoom.zoom;
		par1 *= 1 / zoom.zoom;
		par2 *= 1 / zoom.zoom;

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

		int moveBackgroundX = (mapX - minX) % 16;
		int moveBackgroundY = (mapY - minY) % 16;
		GL11.glColor4f(0.7F, 0.7F, 0.7F, 1.0F);
		for (int yVar = 0; yVar * 16 - moveBackgroundY < zoom.bottomRenderBorder; yVar++) {
			for (int xVar = 0; xVar * 16 - moveBackgroundX < zoom.rightRenderBorder; xVar++) {
				TextureAtlasSprite icon = getTexture(Blocks.STONE);
				mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
				drawTexturedModalRect(innerLeftSide + xVar * 16 - moveBackgroundX, innerTopSide + yVar * 16 - moveBackgroundY, icon, 16, 16);
			}
		}

		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		LinkedLogisticsOrderList list = _table.watchedRequests.get(orderId).getValue2();
		if (!list.isEmpty()) {
			SimpleGraphics.drawVerticalLine(innerLeftSide - mapX + 110, innerTopSide - mapY - 197, innerTopSide - mapY - 180, Color.GREEN, zoom.line);
		}
		renderLinkedOrderListLines(list, innerLeftSide - mapX + 102, innerTopSide - mapY - 180);
		for (Float progress : list.getProgresses()) {
			int pos = (int) (29.0F * progress);
			drawProgressPoint(innerLeftSide - mapX + 110, innerTopSide - mapY - 197 + pos, 0xff00ff00);
		}
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);

		RenderHelper.enableGUIStandardItemLighting();
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glColor4f(0.7F, 0.7F, 0.7F, 1.0F);
		mc.getTextureManager().bindTexture(RequestMonitorPopup.achievementTextures);
		String s = Integer.toString(orderId);
		if (!list.isEmpty()) {
			drawTexturedModalRect(innerLeftSide - mapX + 97, innerTopSide - mapY - 220, 0, 202, 26, 26);
			mc.fontRenderer.drawStringWithShadow(s, innerLeftSide - mapX + 111 - mc.fontRenderer.getStringWidth(s) / 2, innerTopSide - mapY - 210, 16777215);
		} else {
			drawTexturedModalRect(innerLeftSide - mapX + 97, innerTopSide - mapY - 162, 0, 202, 26, 26);
			mc.fontRenderer.drawStringWithShadow(s, innerLeftSide - mapX + 111 - mc.fontRenderer.getStringWidth(s) / 2, innerTopSide - mapY - 152, 16777215);
		}
		renderLinkedOrderListItems(list, innerLeftSide - mapX + 102, innerTopSide - mapY - 180, par1, par2);
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
		mc.getTextureManager().bindTexture(RequestMonitorPopup.achievementTextures);
		drawTexturedModalRect(leftSide, topSide, 0, 0, xSize, ySize);
		GL11.glPopMatrix();
		zLevel = 0.0F;
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
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
			mc.getTextureManager().bindTexture(RequestMonitorPopup.achievementTextures);
			drawTexturedModalRect(startLeft - 5, yPos - 5, 0, 202, 26, 26);
			GL11.glColor4f(0.7F, 0.7F, 0.7F, 1.0F);
			renderItemAt(aList.getAsDisplayItem(), startLeft, yPos);
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

	private void renderItemAt(ItemIdentifierStack item, int x, int y) {
		if (guiLeft < x && x < guiLeft + xSize - 16 && guiTop < y && y < guiTop + ySize - 16) {
			itemRender.renderItemAndEffectIntoGUI(item.getItem().makeNormalStack(1), x, y);
			itemRender.renderItemOverlayIntoGUI(fontRenderer, item.getItem().makeNormalStack(1), x, y, "");
			String s = StringUtils.getFormatedStackSize(item.getStackSize(), false);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			itemRender.zLevel = 0.0F;
			// Draw number
			mc.fontRenderer.drawStringWithShadow(s, x + 17 - mc.fontRenderer.getStringWidth(s), y + 9, 16777215);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glEnable(GL11.GL_LIGHTING);
		}
	}

	protected void drawProgressPoint(int x, int y, int color) {
		int line = zoom.line + 1;
		Gui.drawRect(x - line + 1, y - line + 1, x + line, y + line, color);
	}

	private TextureAtlasSprite getTexture(Block blockIn) {
		return Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getTexture(blockIn.getDefaultState());
	}
}
