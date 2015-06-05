package logisticspipes.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import logisticspipes.utils.string.StringUtils;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class GuiStatistics extends LogisticsBaseGuiScreen {

	private final String PREFIX = "gui.networkstatistics.";

	private final int TAB_COUNT = 2;
	private int current_Tab;

	private final List<GuiButton> TAB_BUTTON_1 = new ArrayList<GuiButton>();
	private final List<GuiButton> TAB_BUTTON_1_2 = new ArrayList<GuiButton>();
	private final List<GuiButton> TAB_BUTTON_2 = new ArrayList<GuiButton>();
	private final LogisticsStatisticsTileEntity tile;

	private ItemDisplay itemDisplay_1;
	private ItemDisplay itemDisplay_2;

	private int move_left;

	public GuiStatistics(final LogisticsStatisticsTileEntity tile) {
		super(180, 220, 0, 0);
		this.tile = tile;
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		TAB_BUTTON_1.add(addButton(new GuiButton(0, guiLeft + 10, guiTop + 70, 20, 20, "<")));
		TAB_BUTTON_1.add(addButton(new GuiButton(1, guiLeft + 150, guiTop + 70, 20, 20, ">")));
		TAB_BUTTON_1.add(addButton(new GuiButton(2, guiLeft + 37, guiTop + 70, 40, 20, "Add")));
		TAB_BUTTON_1.add(addButton(new GuiButton(3, guiLeft + 83, guiTop + 70, 60, 20, "Remove")));
		TAB_BUTTON_1_2.add(addButton(new SmallGuiButton(4, guiLeft + 84, guiTop + 205, 10, 10, "<")));
		TAB_BUTTON_1_2.add(addButton(new SmallGuiButton(5, guiLeft + 96, guiTop + 205, 10, 10, ">")));
		TAB_BUTTON_2.add(addButton(new GuiButton(6, guiLeft + 10, guiTop + 40, 160, 20, StringUtils.translate(PREFIX + "gettasks"))));
		TAB_BUTTON_2.add(addButton(new SmallGuiButton(7, guiLeft + 90, guiTop + 65, 10, 10, "<")));
		TAB_BUTTON_2.add(addButton(new SmallGuiButton(8, guiLeft + 160, guiTop + 65, 10, 10, ">")));

		if (itemDisplay_1 == null) {
			itemDisplay_1 = new ItemDisplay(null, fontRendererObj, this, null, guiLeft + 10, guiTop + 18, xSize - 20, ySize - 100, new int[] { 1, 10, 64, 64 }, true);
		}
		itemDisplay_1.reposition(guiLeft + 10, guiTop + 40, xSize - 20, 20);

		if (itemDisplay_2 == null) {
			itemDisplay_2 = new ItemDisplay(null, fontRendererObj, this, null, guiLeft + 10, guiTop + 18, xSize - 20, ySize - 100, new int[] { 1, 10, 64, 64 }, true);
			itemDisplay_2.setItemList(new ArrayList<ItemIdentifierStack>());
		}
		itemDisplay_2.reposition(guiLeft + 10, guiTop + 80, xSize - 20, 125);

		updateItemList();
	}

	@Override
	public void resetSubGui() {
		super.resetSubGui();
		updateItemList();
	}

	public void updateItemList() {
		List<ItemIdentifierStack> allItems = new ArrayList<ItemIdentifierStack>();
		for (TrackingTask task : tile.tasks) {
			allItems.add(task.item.makeStack(1));
		}
		itemDisplay_1.setItemList(allItems);
	}

	@Override
	protected void actionPerformed(GuiButton p_146284_1_) {
		if (p_146284_1_.id == 0) {
			itemDisplay_1.prevPage();
		} else if (p_146284_1_.id == 1) {
			itemDisplay_1.prevPage();
		} else if (p_146284_1_.id == 2) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(RequestAmountTaskSubGui.class).setTilePos(tile));
		} else if (p_146284_1_.id == 3 && itemDisplay_1.getSelectedItem() != null) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(RemoveAmoundTask.class).setItem(itemDisplay_1.getSelectedItem().getItem()).setTilePos(tile));
			Iterator<TrackingTask> iter = tile.tasks.iterator();
			while (iter.hasNext()) {
				TrackingTask task = iter.next();
				if (task.item == itemDisplay_1.getSelectedItem().getItem()) {
					iter.remove();
					break;
				}
			}
			updateItemList();
		} else if (p_146284_1_.id == 4) {
			move_left++;
			if (move_left >= 24 * 4) {
				move_left--;
			}
		} else if (p_146284_1_.id == 5) {
			move_left--;
			if (move_left < 0) {
				move_left = 0;
			}
		} else if (p_146284_1_.id == 6) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(RequestRunningCraftingTasks.class).setTilePos(tile));
		} else if (p_146284_1_.id == 7) {
			itemDisplay_2.prevPage();
		} else if (p_146284_1_.id == 8) {
			itemDisplay_2.prevPage();
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mouse_x, int mouse_y) {
		GL11.glColor4d(1.0D, 1.0D, 1.0D, 1.0D);
		for (int i = 0; i < TAB_COUNT; i++) {
			GuiGraphics.drawGuiBackGround(mc, guiLeft + (25 * i) + 2, guiTop - 2, guiLeft + 27 + (25 * i), guiTop + 35, zLevel, false, true, true, false, true);
		}
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop + 20, right, bottom, zLevel, true);
		GuiGraphics.drawGuiBackGround(mc, guiLeft + (25 * current_Tab) + 2, guiTop - 2, guiLeft + 27 + (25 * current_Tab), guiTop + 38, zLevel, true, true, true, false, true);

		// First Tab
		GuiGraphics.drawStatsBackground(mc, guiLeft + 6, guiTop + 3);

		// Second Tab
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240 / 1.0F, 240 / 1.0F);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		RenderHelper.enableGUIStandardItemLighting();
		ItemStack stack = new ItemStack(Blocks.crafting_table, 0);
		GuiScreen.itemRender.renderItemAndEffectIntoGUI(fontRendererObj, getMC().renderEngine, stack, guiLeft + 31, guiTop + 3);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GuiScreen.itemRender.zLevel = 0.0F;

		if (current_Tab == 0) {
			itemDisplay_1.renderItemArea(zLevel);
			itemDisplay_1.renderPageNumber(right - 40, guiTop + 28);
			if (itemDisplay_1.getSelectedItem() != null) {
				TrackingTask task = null;
				for (TrackingTask taskLoop : tile.tasks) {
					if (taskLoop.item == itemDisplay_1.getSelectedItem().getItem()) {
						task = taskLoop;
						break;
					}
				}
				if (task != null) {
					GuiGraphics.drawSlotBackground(mc, guiLeft + 10, guiTop + 99);
					GL11.glEnable(GL12.GL_RESCALE_NORMAL);
					OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240 / 1.0F, 240 / 1.0F);
					GL11.glEnable(GL11.GL_LIGHTING);
					GL11.glEnable(GL11.GL_DEPTH_TEST);
					RenderHelper.enableGUIStandardItemLighting();
					GuiScreen.itemRender.renderItemAndEffectIntoGUI(fontRendererObj, getMC().renderEngine, task.item.makeNormalStack(1), guiLeft + 11, guiTop + 100);
					GL11.glDisable(GL11.GL_LIGHTING);
					GL11.glDisable(GL11.GL_DEPTH_TEST);
					GuiScreen.itemRender.zLevel = 0.0F;
					mc.fontRenderer.drawString(StringUtils.getWithMaxWidth(task.item.getFriendlyName(), 136, fontRendererObj), guiLeft + 32, guiTop + 104, Color.getValue(Color.DARKER_GREY), false);

					int xOrigo = xCenter - 68;
					int yOrigo = yCenter + 90;
					drawLine(xOrigo, yOrigo, xOrigo + 150, yOrigo, Color.DARKER_GREY);
					drawLine(xOrigo, yOrigo, xOrigo, yOrigo - 80, Color.DARKER_GREY);
					for (int k = -4; k < 5; k++) {
						int begin = -4;
						if (k == -4) {
							begin = -1;
						}
						if (k == 0) {
							begin = -1;
						}
						if (k == 4) {
							begin = -1;
						}
						drawLine(xOrigo + begin, yCenter + 50 + k * 10, xOrigo + 5, yCenter + 50 + k * 10, Color.DARKER_GREY);
					}
					for (int k = 0; k < 16; k++) {
						drawLine(xOrigo + k * 10, yOrigo - 4, xOrigo + k * 10, yOrigo + 4, Color.DARKER_GREY);
					}

					int time_left = 15 + move_left * 15;
					int time_right = move_left * 15;

					String right = "";
					String left = "";
					if (time_right == 0) {
						right = "Now";
					} else {
						if (time_right / 60 != 0) {
							right += (time_right / 60) + "h";
						}
						if (time_right % 60 != 0) {
							right += (time_right % 60) + "min";
						}
					}
					if (time_left / 60 != 0) {
						left += (time_left / 60) + "h";
					}
					if (time_left % 60 != 0) {
						left += (time_left % 60) + "min";
					}

					fontRendererObj.drawString(left, xOrigo - 12, yOrigo + 6, 0x404040);
					fontRendererObj.drawString(right, xOrigo + 153 - fontRendererObj.getStringWidth(right), yOrigo + 6, 0x404040);

					long[] data = new long[task.amountRecorded.length];
					int pos = 0;
					for (int i = task.arrayPos - 1; i >= 0; i--) {
						data[pos++] = task.amountRecorded[i];
					}
					for (int i = task.amountRecorded.length - 1; i >= task.arrayPos; i--) {
						data[pos++] = task.amountRecorded[i];
					}

					long lowest = Long.MAX_VALUE;
					long highest = Long.MIN_VALUE;
					int first = (15 * move_left);

					for (int i = first; i <= first + 15 && i < data.length; i++) {
						long point = data[i];
						if (point > highest) {
							highest = point;
						}
						if (point < lowest) {
							lowest = point;
						}
					}

					double averagey = ((double) highest + lowest) / 2;

					fontRendererObj.drawString(StringUtils.getFormatedStackSize(highest, false), xOrigo - 1 - fontRendererObj.getStringWidth(StringUtils.getFormatedStackSize(highest, false)), guiTop + 117, 0x404040);
					fontRendererObj.drawString(StringUtils.getFormatedStackSize((long) averagey, false), xOrigo - 1 - fontRendererObj.getStringWidth(StringUtils.getFormatedStackSize((long) averagey, false)), yCenter + 46, 0x404040);
					fontRendererObj.drawString(StringUtils.getFormatedStackSize(lowest, false), xOrigo - 1 - fontRendererObj.getStringWidth(StringUtils.getFormatedStackSize(lowest, false)), bottom - 23, 0x404040);

					float yScale = 80F / Math.max(highest - lowest, 0.5F);
					int x = xOrigo + 150;
					double yOff = data[first] - averagey;
					int y = (yOrigo - 80 / 2) - (int) (yOff * yScale);

					for (int i = first + 1; i < data.length; i++) {
						long point = data[i];
						int x1 = x - 10;
						if (x1 < guiLeft + 15) {
							break;
						}
						yOff = point - averagey;
						int y1 = (yOrigo - 80 / 2) - (int) (yOff * yScale);

						drawLine(x1, y1, x, y, Color.RED);
						drawRect(x - 1, y - 1, x + 2, y + 2, Color.BLACK);

						x = x1;
						y = y1;
					}
					drawRect(x - 1, y - 1, x + 2, y + 2, Color.BLACK);
				}
			}
		} else if (current_Tab == 1) {
			itemDisplay_2.renderItemArea(zLevel);
			itemDisplay_2.renderPageNumber(right - 50, guiTop + 66);
		}

		super.drawGuiContainerBackgroundLayer(f, mouse_x, mouse_y);
	}

	@Override
	protected void keyTyped(char c, int i) {
		if (current_Tab == 0) {
			if (i == 201) { //PgUp
				itemDisplay_1.prevPage();
			} else if (i == 209) { //PgDn
				itemDisplay_1.nextPage();
			} else {
				super.keyTyped(c, i);
			}
		} else if (current_Tab == 1) {
			if (i == 201) { //PgUp
				itemDisplay_2.prevPage();
			} else if (i == 209) { //PgDn
				itemDisplay_2.nextPage();
			} else {
				super.keyTyped(c, i);
			}
		} else {
			super.keyTyped(c, i);
		}
	}

	@Override
	protected void mouseClicked(int par1, int par2, int par3) {
		if (par3 == 0 && par1 > guiLeft && par1 < guiLeft + 220 && par2 > guiTop && par2 < guiTop + 20) {
			par1 -= guiLeft + 3;
			current_Tab = Math.max(0, Math.min(par1 / 25, TAB_COUNT - 1));
		} else {
			if (current_Tab == 0) {
				itemDisplay_1.handleClick(par1, par2, par3);
			} else if (current_Tab == 1) {
				itemDisplay_2.handleClick(par1, par2, par3);
			}
			super.mouseClicked(par1, par2, par3);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		if (current_Tab == 0) {
			mc.fontRenderer.drawString(StringUtils.translate(PREFIX + "amount"), 10, 28, Color.getValue(Color.DARKER_GREY), false);
		} else if (current_Tab == 1) {
			mc.fontRenderer.drawString(StringUtils.translate(PREFIX + "crafting"), 10, 28, Color.getValue(Color.DARKER_GREY), false);
			GuiGraphics.displayItemToolTip(itemDisplay_2.getToolTip(), this, zLevel, guiLeft, guiTop);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void checkButtons() {
		super.checkButtons();
		for (GuiButton button : (List<GuiButton>) buttonList) {
			if (TAB_BUTTON_1.contains(button)) {
				button.visible = current_Tab == 0;
				if (button.displayString.equals("Remove")) {
					button.enabled = itemDisplay_1.getSelectedItem() != null;
				}
			}
			if (TAB_BUTTON_1_2.contains(button)) {
				button.visible = current_Tab == 0 && itemDisplay_1.getSelectedItem() != null;
			}
			if (TAB_BUTTON_2.contains(button)) {
				button.visible = current_Tab == 1;
			}
		}
	}

	public void handlePacket_1(List<ItemIdentifierStack> identList) {
		if (hasSubGui() && getSubGui() instanceof GuiAddTracking) {
			((GuiAddTracking) getSubGui()).handlePacket(identList);
		} else if (!hasSubGui()) {
			GuiAddTracking sub = new GuiAddTracking(tile);
			setSubGui(sub);
			sub.handlePacket(identList);
		}
	}

	public void handlePacket_2(List<ItemIdentifierStack> identList) {
		itemDisplay_2.setItemList(identList);
	}
}
