package logisticspipes.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.round;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import logisticspipes.blocks.stats.LogisticsStatisticsTileEntity;
import logisticspipes.blocks.stats.TrackingTask;
import logisticspipes.gui.popup.GuiAddTracking;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.block.RemoveAmoundTask;
import logisticspipes.network.packets.block.RequestAmountTaskSubGui;
import logisticspipes.network.packets.block.RequestRunningCraftingTasks;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.Color;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.ItemDisplay;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.math.Vec2;
import logisticspipes.utils.string.StringUtils;
import network.rs485.logisticspipes.util.TextUtil;

public class GuiStatistics extends LogisticsBaseGuiScreen {

	private final String PREFIX = "gui.networkstatistics.";

	private int currentTab;
	private final TabTracker tabTracker = new TabTracker();
	private final TabCrafting tabCrafting = new TabCrafting();
	private final List<StatisticsTab> tabs = Arrays.asList(tabTracker, tabCrafting);

	private final LogisticsStatisticsTileEntity tile;

	private int prevMouseDragX;
	private int prevMouseDragY;

	public GuiStatistics(final LogisticsStatisticsTileEntity tile) {
		super(180, 220, 0, 0);
		this.tile = tile;
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);

		super.initGui();
		buttonList.clear();

		tabs.forEach(StatisticsTab::init);

		tabTracker.updateItemList();
	}

	@Override
	public void closeGui() throws IOException {
		super.closeGui();
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public void resetSubGui() {
		super.resetSubGui();
		tabTracker.updateItemList();
	}

	private StatisticsTab getActiveTab() {
		return tabs.get(currentTab);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		getActiveTab().actionPerformed(button);
	}

	@Override
	protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		getActiveTab().onMouseDrag(mouseX, mouseY, mouseX - prevMouseDragX, mouseY - prevMouseDragY);
		prevMouseDragX = mouseX;
		prevMouseDragY = mouseY;
		super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

		drawBG();
		getActiveTab().draw(mouseX, mouseY);

		super.drawGuiContainerBackgroundLayer(f, mouseX, mouseY);
	}

	private void drawBG() {
		// background
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop + 20, right, bottom, zLevel, true);
		GuiGraphics.drawGuiBackGround(mc, guiLeft + (25 * currentTab) + 2, guiTop - 2, guiLeft + 27 + (25 * currentTab), guiTop + 38, zLevel, true, true, true, false, true);

		// tab selector panes
		for (int i = 0; i < tabs.size(); i++) {
			GuiGraphics.drawGuiBackGround(mc, guiLeft + (25 * i) + 2, guiTop - 2, guiLeft + 27 + (25 * i), guiTop + 35, zLevel, false, true, true, false, true);
		}

		// First Tab
		GuiGraphics.drawStatsBackground(mc, guiLeft + 6, guiTop + 3);

		// Second Tab
		RenderHelper.enableGUIStandardItemLighting();
		ItemStack stack = new ItemStack(Blocks.CRAFTING_TABLE, 1);
		GlStateManager.enableDepth();
		itemRender.renderItemAndEffectIntoGUI(stack, guiLeft + 31, guiTop + 3);
		GlStateManager.disableDepth();
		itemRender.zLevel = 0.0F;
	}

	@Override
	protected void keyTyped(char c, int i) throws IOException {
		super.keyTyped(c, i);
		getActiveTab().keyTyped(c, i);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		prevMouseDragX = mouseX;
		prevMouseDragY = mouseY;

		if (mouseButton == 0 && mouseX > guiLeft && mouseX < guiLeft + 220 && mouseY > guiTop && mouseY < guiTop + 20) {
			mouseX -= guiLeft + 3;
			currentTab = max(0, min(mouseX / 25, tabs.size() - 1));
		} else {
			getActiveTab().handleClick(mouseX, mouseY, mouseButton);
			super.mouseClicked(mouseX, mouseY, mouseButton);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		getActiveTab().drawForegroundLayer(mouseX, mouseY);
	}

	@Override
	protected void checkButtons() {
		super.checkButtons();
		tabs.forEach(StatisticsTab::checkButtons);
	}

	@Override
	public void handleMouseInputSub() throws IOException {
		getActiveTab().onMouseScroll(Mouse.getEventDWheel());
		super.handleMouseInputSub();
	}

	public void handlePacket1(List<ItemIdentifierStack> identList) {
		tabTracker.handlePacket(identList);
	}

	public void handlePacket2(List<ItemIdentifierStack> identList) {
		tabCrafting.handlePacket(identList);
	}

	private interface StatisticsTab {

		void init();

		void draw(int mouseX, int mouseY);

		default void drawForegroundLayer(int mouseX, int mouseY) {}

		default void checkButtons() {}

		default void actionPerformed(GuiButton button) {}

		default void keyTyped(char c, int i) {}

		default void handleClick(int mouseX, int mouseY, int mouseButton) {}

		default void onMouseDrag(int x, int y, int dx, int dy) {}

		default void onMouseScroll(int dw) {}

	}

	private class TabTracker implements StatisticsTab {

		private ItemDisplay itemDisplay;

		private float xViewportOffset = -1434;
		private float yViewportOffset;
		private float xViewportScale = 15;
		private float yViewportScale = 15;

		private boolean isDraggingGraph = false;
		private boolean isDraggingXBar = false;
		private boolean isDraggingYBar = false;

		private final List<GuiButton> BUTTONS = new ArrayList<>();

		@Override
		public void init() {
			BUTTONS.add(addButton(new GuiButton(0, guiLeft + 10, guiTop + 70, 20, 20, "<")));
			BUTTONS.add(addButton(new GuiButton(1, guiLeft + 150, guiTop + 70, 20, 20, ">")));
			BUTTONS.add(addButton(new GuiButton(2, guiLeft + 37, guiTop + 70, 40, 20, "Add")));
			BUTTONS.add(addButton(new GuiButton(3, guiLeft + 83, guiTop + 70, 60, 20, "Remove")));

			if (itemDisplay == null) {
				itemDisplay = new ItemDisplay(null, fontRenderer, GuiStatistics.this, null, guiLeft + 10, guiTop + 18, xSize - 20, ySize - 100, 0, 0, 0, new int[] { 1, 10, 64, 64 }, true);
			}
			itemDisplay.reposition(guiLeft + 10, guiTop + 40, xSize - 20, 20, 0, 0);
		}

		@Override
		public void draw(int mouseX, int mouseY) {
			itemDisplay.renderItemArea(zLevel);
			itemDisplay.renderPageNumber(right - 40, guiTop + 28);
			if (itemDisplay.getSelectedItem() != null) {
				TrackingTask task = getSelectedTask();

				if (task != null) {
					GuiGraphics.drawSlotBackground(mc, guiLeft + 10, guiTop + 99);
					RenderHelper.enableGUIStandardItemLighting();
					GlStateManager.enableDepth();
					itemRender.renderItemAndEffectIntoGUI(task.item.makeNormalStack(1), guiLeft + 11, guiTop + 100);
					GlStateManager.disableDepth();
					itemRender.zLevel = 0.0F;
					mc.fontRenderer.drawString(StringUtils.getWithMaxWidth(task.item.getFriendlyName(), 136, fontRenderer), guiLeft + 32, guiTop + 104, Color.getValue(Color.DARKER_GREY), false);

					int xOrigo = xCenter - 72;
					int yOrigo = yCenter + 90;
					GlStateManager.pushMatrix();
					GlStateManager.translate(xOrigo, yOrigo, 0);

					drawLine(0, 0, 150, 0, Color.DARKER_GREY);
					drawLine(0, 0, 0, -80, Color.DARKER_GREY);

					drawLine(-4, -90 + 50 + -40, 0, -90 + 50 + -40, Color.DARKER_GREY);

					drawLine(150, -1, 150, 4, Color.DARKER_GREY);

					long[] data = getTaskData(task);

					float xViewportCenter = 75;
					float yViewportCenter = 40;

					int rightLimit = 2; // we want to draw one more graph part past the right edge
					for (int i = 0; i < data.length; i++) {
						rightLimit--;
						if (rightLimit == 0) break;

						float x = i;
						float y = data[i];
						float prevX = x;
						float prevY = y;
						if (i > 0) {
							prevX = x - 1;
							prevY = data[i - 1];
						}

						x += xViewportOffset;
						x *= xViewportScale;
						x += xViewportCenter;
						prevX += xViewportOffset;
						prevX *= xViewportScale;
						prevX += xViewportCenter;

						y -= yViewportOffset;
						y *= yViewportScale;
						y += yViewportCenter;
						prevY -= yViewportOffset;
						prevY *= yViewportScale;
						prevY += yViewportCenter;

						if (x <= 150) rightLimit = 2;
						if (x < 0) continue;

						if (x <= 150) {
							int interval = max(1, (int) (40 / xViewportScale) + 1);
							if (i % interval == 0) {
								String s = formatTime(data.length - i - 1);
								int w = mc.fontRenderer.getStringWidth(s);
								drawLine((int) x, -1, (int) x, 4, Color.DARKER_GREY);
								mc.fontRenderer.drawString(s, (int) x - w / 2f, 6, Color.DARKER_GREY.getValue(), false);
							}
						}

						if (y > 0 && y < 80) {
							drawLine(-4, (int) -y, 0, (int) -y, Color.DARKER_GREY);
							GlStateManager.pushMatrix();
							GlStateManager.rotate(90, 0, 0, 1);
							String s = data[i] + "";
							int w = mc.fontRenderer.getStringWidth(s);
							mc.fontRenderer.drawString(s, (int) -y - w / 2f, 6, Color.DARKER_GREY.getValue(), false);
							GlStateManager.popMatrix();
						}

						drawGraphPart((int) prevX, (int) prevY, (int) x, (int) y);
					}
				}
				GlStateManager.popMatrix();
			}
		}

		@Nullable
		private TrackingTask getSelectedTask() {
			for (TrackingTask taskLoop : tile.tasks) {
				if (taskLoop.item == itemDisplay.getSelectedItem().getItem()) {
					return taskLoop;
				}
			}
			return null;
		}

		private long[] getTaskData(TrackingTask task) {
			long[] data = new long[task.amountRecorded.length];
			System.arraycopy(task.amountRecorded, task.arrayPos, data, 0, task.amountRecorded.length - task.arrayPos);
			System.arraycopy(task.amountRecorded, 0, data, task.amountRecorded.length - task.arrayPos, task.arrayPos);
			return data;
		}

		@Override
		public void drawForegroundLayer(int mouseX, int mouseY) {
			mc.fontRenderer.drawString(TextUtil.translate(PREFIX + "amount"), 10, 28, Color.getValue(Color.DARKER_GREY), false);
		}

		@Override
		public void actionPerformed(GuiButton button) {
			if (button.id == 0) {
				itemDisplay.prevPage();
			} else if (button.id == 1) {
				itemDisplay.prevPage();
			} else if (button.id == 2) {
				MainProxy.sendPacketToServer(PacketHandler.getPacket(RequestAmountTaskSubGui.class).setTilePos(tile));
			} else if (button.id == 3 && itemDisplay.getSelectedItem() != null) {
				MainProxy.sendPacketToServer(PacketHandler.getPacket(RemoveAmoundTask.class).setItem(itemDisplay.getSelectedItem().getItem()).setTilePos(tile));
				Iterator<TrackingTask> iter = tile.tasks.iterator();
				while (iter.hasNext()) {
					TrackingTask task = iter.next();
					if (task.item == itemDisplay.getSelectedItem().getItem()) {
						iter.remove();
						break;
					}
				}
				updateItemList();
			}
		}

		@Override
		public void keyTyped(char c, int i) {
			if (i == Keyboard.KEY_PRIOR) { //PgUp
				itemDisplay.prevPage();
			} else if (i == Keyboard.KEY_NEXT) { //PgDn
				itemDisplay.nextPage();
			}
		}

		@Override
		public void handleClick(int mouseX, int mouseY, int mouseButton) {
			if (itemDisplay.handleClick(mouseX, mouseY, mouseButton)) {
				xViewportOffset = max(-1439, min(xViewportOffset, 0));
				TrackingTask task = getSelectedTask();
				if (task != null) {
					long[] data = getTaskData(task);
					yViewportOffset = data[round(-xViewportOffset)];
				}
			}

			int xOrigo = xCenter - 72;
			int yOrigo = yCenter + 90;
			isDraggingGraph = mouseButton == 0 && mouseX > xOrigo && mouseX < xOrigo + 150 && mouseY < yOrigo && mouseY > yOrigo - 80;
			isDraggingXBar = mouseButton == 0 && mouseX > xOrigo && mouseX < xOrigo + 150 && mouseY < yOrigo + 16 && mouseY > yOrigo + 4;
			isDraggingYBar = mouseButton == 0 && mouseX > xOrigo - 16 && mouseX < xOrigo - 4 && mouseY < yOrigo && mouseY > yOrigo - 80;
		}

		@Override
		public void checkButtons() {
			for (GuiButton button : BUTTONS) {
				button.visible = getActiveTab() == this;
				if (button.displayString.equals("Remove")) {
					button.enabled = itemDisplay.getSelectedItem() != null;
				}
			}
		}

		@Override
		public void onMouseDrag(int x, int y, int dx, int dy) {
			if (isDraggingGraph) {
				xViewportOffset += dx / xViewportScale;
				yViewportOffset += dy / yViewportScale;
			} else if (isDraggingXBar) {
				float mul = (float) pow(1.25, dx / 2f);
				xViewportScale *= mul;
			} else if (isDraggingYBar) {
				float mul = (float) pow(1.25, -dy / 2f);
				yViewportScale *= mul;
			}
		}

		@Override
		public void onMouseScroll(int dw) {
			float mul = (float) pow(1.25, dw / 60f);
			xViewportScale *= mul;
			yViewportScale *= mul;
		}

		private void drawGraphPart(int prevX, int prevY, int x, int y) {
			Vec2 left = new Vec2(prevX, prevY);
			Vec2 right = new Vec2(x, y);

			// bounds check
			{
				Vec2 min = new Vec2(left.x, min(left.y, right.y));
				Vec2 max = new Vec2(right.x, max(left.y, right.y));

				if (!(min.x < 150 && max.x > 0 && min.y < 80 && max.y > 0)) return;
			}

			// clamp to the edges of the graph
			right = clampCorner(left, right, Vec2.ORIGIN, true);
			right = clampCorner(left, right, new Vec2(150, 80), false);
			left = clampCorner(right, left, Vec2.ORIGIN, true);
			left = clampCorner(right, left, new Vec2(150, 80), false);

			drawLine((int) left.x, (int) -left.y, (int) right.x, (int) -right.y, Color.RED);

			int radius = 2;
			if (xViewportScale < 4) radius = 1;

			if (prevX >= 0 && prevX <= 150 && prevY >= 0 && prevY <= 80)
				drawRect(prevX - radius + 1, -prevY - radius + 1, prevX + radius, -prevY + radius, Color.BLACK);

			if (x >= 0 && x <= 150 && y >= 0 && y <= 80)
				drawRect(x - radius + 1, -y - radius + 1, x + radius, -y + radius, Color.BLACK);
		}

		private Vec2 clampYPlane(Vec2 v, Vec2 toClamp, float x0, boolean greater) {
			if (toClamp.x == x0) return toClamp;
			if (toClamp.x == v.x) return toClamp;

			if ((!greater && toClamp.x < x0) || (greater && toClamp.x > x0)) {
				return toClamp;
			}

			Vec2 dir = toClamp.sub(v);
			dir = dir.div(dir.x); // let dir.x=1 but keep vector's direction
			float dist = (x0 - toClamp.x);
			return toClamp.add(dir.mul(dist));
		}

		@SuppressWarnings("SuspiciousNameCombination")
		private Vec2 clampXPlane(Vec2 from, Vec2 to, float y0, boolean greater) {
			final Vec2 vec2 = clampYPlane(new Vec2(from.y, from.x), new Vec2(to.y, to.x), y0, greater);
			return new Vec2(vec2.y, vec2.x);
		}

		private Vec2 clampCorner(Vec2 from, Vec2 to, Vec2 corner, boolean greater) {
			return clampXPlane(from, clampYPlane(from, to, corner.x, greater), corner.y, greater);
		}

		private String formatTime(int minutes) {
			if (minutes == 0) return "Now";

			int mins = minutes % 60;
			minutes /= 60;
			int hours = minutes;

			StringBuilder sb = new StringBuilder();

			if (hours > 0) sb.append(hours).append("h");
			if (mins > 0) sb.append(mins).append("min");

			return sb.toString();
		}

		public void updateItemList() {
			List<ItemIdentifierStack> allItems = tile.tasks.stream().map(task -> task.item.makeStack(1))
					.collect(Collectors.toList());
			itemDisplay.setItemList(allItems);
		}

		public void handlePacket(List<ItemIdentifierStack> identList) {
			if (hasSubGui() && getSubGui() instanceof GuiAddTracking) {
				((GuiAddTracking) getSubGui()).handlePacket(identList);
			} else if (!hasSubGui()) {
				GuiAddTracking sub = new GuiAddTracking(tile);
				setSubGui(sub);
				sub.handlePacket(identList);
			}
		}

	}

	private class TabCrafting implements StatisticsTab {

		private ItemDisplay itemDisplay;

		private final List<GuiButton> BUTTONS = new ArrayList<>();

		@Override
		public void init() {
			BUTTONS.add(addButton(new GuiButton(6, guiLeft + 10, guiTop + 40, 160, 20, TextUtil.translate(PREFIX + "gettasks"))));
			BUTTONS.add(addButton(new SmallGuiButton(7, guiLeft + 90, guiTop + 65, 10, 10, "<")));
			BUTTONS.add(addButton(new SmallGuiButton(8, guiLeft + 160, guiTop + 65, 10, 10, ">")));

			if (itemDisplay == null) {
				itemDisplay = new ItemDisplay(null, fontRenderer, GuiStatistics.this, null, guiLeft + 10, guiTop + 18, xSize - 20, ySize - 100, 0, 0, 0, new int[] { 1, 10, 64, 64 }, true);
				itemDisplay.setItemList(new ArrayList<>());
			}
			itemDisplay.reposition(guiLeft + 10, guiTop + 80, xSize - 20, 125, 0, 0);

		}

		@Override
		public void draw(int mouseX, int mouseY) {
			itemDisplay.renderItemArea(zLevel);
			itemDisplay.renderPageNumber(right - 50, guiTop + 66);
		}

		@Override
		public void drawForegroundLayer(int mouseX, int mouseY) {
			mc.fontRenderer.drawString(TextUtil.translate(PREFIX + "crafting"), 10, 28, Color.getValue(Color.DARKER_GREY), false);
			GuiGraphics.displayItemToolTip(itemDisplay.getToolTip(), GuiStatistics.this, zLevel, guiLeft, guiTop);
		}

		@Override
		public void actionPerformed(GuiButton button) {
			if (button.id == 6) {
				MainProxy.sendPacketToServer(PacketHandler.getPacket(RequestRunningCraftingTasks.class).setTilePos(tile));
			} else if (button.id == 7) {
				itemDisplay.prevPage();
			} else if (button.id == 8) {
				itemDisplay.prevPage();
			}
		}

		@Override
		public void keyTyped(char c, int i) {
			if (i == Keyboard.KEY_PRIOR) { //PgUp
				itemDisplay.prevPage();
			} else if (i == Keyboard.KEY_NEXT) { //PgDn
				itemDisplay.nextPage();
			}
		}

		@Override
		public void handleClick(int mouseX, int mouseY, int mouseButton) {
			itemDisplay.handleClick(mouseX, mouseY, mouseButton);
		}

		@Override
		public void checkButtons() {
			for (GuiButton button : BUTTONS) {
				button.visible = getActiveTab() == this;
			}
		}

		public void handlePacket(List<ItemIdentifierStack> identList) {
			itemDisplay.setItemList(identList);
		}

	}

}
