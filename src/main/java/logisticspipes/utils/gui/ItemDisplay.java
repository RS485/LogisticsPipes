package logisticspipes.utils.gui;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.client.FMLClientHandler;

import lombok.Getter;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import logisticspipes.config.Configs;
import logisticspipes.interfaces.ISpecialItemRenderer;
import logisticspipes.utils.Color;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.item.ItemStackRenderer;
import logisticspipes.utils.item.ItemStackRenderer.DisplayAmount;
import logisticspipes.utils.tuples.Pair;

public class ItemDisplay {

	public enum DisplayOption {
		ID,
		ID_DOWN,
		SIZE,
		SIZE_DOWN,
		NAME,
		NAME_DOWN,
	}

	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/icons.png");
	private static final int PANELSIZEX = 20;
	private static final int PANELSIZEY = 20;

	private ItemIdentifierStack selectedItem = null;
	public final LinkedList<ItemIdentifierStack> _allItems = new LinkedList<>();
	private final Map<Pair<Integer, Integer>, ItemIdentifierStack> map = new HashMap<>();

	@Getter
	private int page = 0;
	private int maxPage = 0;
	//private int requestCount = 1;
	private InputBar requestCountBar;
	private Object[] tooltip = null;
	private boolean listbyserver = false;

	private final IItemSearch search;
	private final FontRenderer fontRenderer;
	private final LogisticsBaseGuiScreen screen;
	private final ISpecialItemRenderer renderer;
	private int left, top, height, width, amountPosLeft, amountPosTop, amountWidth;
	private int itemsPerPage;
	private final int[] amountChangeMode;
	private final boolean shiftPageChange;
	private final Minecraft mc = FMLClientHandler.instance().getClient();
	private static DisplayOption option = DisplayOption.ID;
	private final ItemStackRenderer stackRenderer = new ItemStackRenderer(0, 0, 100.0F, false, false);

	public ItemDisplay(IItemSearch search, FontRenderer fontRenderer, LogisticsBaseGuiScreen screen, ISpecialItemRenderer renderer, int left, int top, int width, int height, int amountPosLeft, int amountPosTop, int amountWidth, int[] amountChangeMode, boolean shiftPageChange) {
		this.search = search;
		this.fontRenderer = fontRenderer;
		this.screen = screen;
		this.renderer = renderer;
		this.left = left;
		this.top = top;
		this.width = width;
		this.height = height;
		this.amountPosLeft = amountPosLeft;
		this.amountPosTop = amountPosTop;
		this.amountWidth = amountWidth;
		itemsPerPage = this.width * this.height / (20 * 20);
		if (amountChangeMode.length != 4) {
			throw new UnsupportedOperationException("amountChangeMode.length needs to be 4");
		}
		this.amountChangeMode = amountChangeMode;
		this.shiftPageChange = shiftPageChange;
		this.requestCountBar = new InputBar(this.fontRenderer, screen, amountPosLeft - (amountWidth / 2), amountPosTop - 5, amountWidth, 12, false, true, InputBar.Align.CENTER);
		this.requestCountBar.minNumber = 1;
		this.requestCountBar.setInteger(1);
	}

	public void reposition(int left, int top, int width, int height, int amountPosLeft, int amountPosTop) {
		this.left = left;
		this.top = top;
		this.width = width;
		this.height = height;
		this.amountPosLeft = amountPosLeft;
		this.amountPosTop = amountPosTop;
		itemsPerPage = this.width * this.height / (20 * 20);
		this.requestCountBar.reposition(amountPosLeft - (this.amountWidth / 2), amountPosTop - 2, this.amountWidth, 12);
	}

	public void setItemList(Collection<ItemIdentifierStack> allItems) {
		listbyserver = true;
		_allItems.clear();
		allItems.stream().sorted(new ItemidStackDisplayOptionComparator()).forEach(_allItems::add);
		boolean found = false;
		if (selectedItem == null) {
			return;
		}
		for (ItemIdentifierStack itemStack : _allItems) {
			if (itemStack.getItem().equals(selectedItem.getItem())) {
				selectedItem = itemStack;
				found = true;
				break;
			}
		}
		if (!found) {
			selectedItem = null;
		}
	}

	private static class ItemidStackDisplayOptionComparator implements Comparator<ItemIdentifierStack> {

		@Override
		public int compare(ItemIdentifierStack o1, ItemIdentifierStack o2) {
			if (ItemDisplay.option == DisplayOption.ID) {
				int c = basicCompare(o1, o2);
				if (c != 0) {
					return c;
				}
				return o2.getStackSize() - o1.getStackSize();
			} else if (ItemDisplay.option == DisplayOption.ID_DOWN) {
				int c = basicCompare(o2, o1);
				if (c != 0) {
					return c;
				}
				return o1.getStackSize() - o2.getStackSize();
			} else if (ItemDisplay.option == DisplayOption.SIZE) {
				int c = o2.getStackSize() - o1.getStackSize();
				if (c != 0) {
					return c;
				}
				return basicCompare(o1, o2);
			} else if (ItemDisplay.option == DisplayOption.SIZE_DOWN) {
				int c = o1.getStackSize() - o2.getStackSize();
				if (c != 0) {
					return c;
				}
				return basicCompare(o2, o1);
			} else if (ItemDisplay.option == DisplayOption.NAME) {
				int c = o1.getItem().getFriendlyName().compareToIgnoreCase(o2.getItem().getFriendlyName());
				if (c != 0) {
					return c;
				}
				c = basicCompare(o1, o2);
				if (c != 0) {
					return c;
				}
				return o2.getStackSize() - o1.getStackSize();
			} else if (ItemDisplay.option == DisplayOption.NAME_DOWN) {
				int c = o2.getItem().getFriendlyName().compareToIgnoreCase(o1.getItem().getFriendlyName());
				if (c != 0) {
					return c;
				}
				c = basicCompare(o2, o1);
				if (c != 0) {
					return c;
				}
				return o1.getStackSize() - o2.getStackSize();
			} else {
				int c = basicCompare(o1, o2);
				if (c != 0) {
					return c;
				}
				return o2.getStackSize() - o1.getStackSize();
			}
		}

		private int basicCompare(ItemIdentifierStack o1, ItemIdentifierStack o2) {
			return o1.compareTo(o2);
		}
	}

	public void cycle() {
		int i = ItemDisplay.option.ordinal();
		i++;
		if (i >= DisplayOption.values().length) {
			i = 0;
		}
		ItemDisplay.option = DisplayOption.values()[i];
		_allItems.sort(new ItemidStackDisplayOptionComparator());
	}

	public void renderSortMode(int x, int y) {
		String name = ItemDisplay.option.name();
		boolean up = true;
		if (name.endsWith("_DOWN")) {
			name = name.substring(0, name.length() - 5);
			up = false;
		}
		name += !up ? " /\\" : " \\/";
		fontRenderer.drawString(name, x - fontRenderer.getStringWidth(name) / 2, y, 0x404040);
	}

	public void renderPageNumber(int x, int y) {
		maxPage = (getSearchedItemNumber() - 1) / itemsPerPage;
		if (maxPage == -1) {
			maxPage = 0;
		}
		if (page > maxPage) {
			page = maxPage;
		}
		String pageString = "Page " + (page + 1) + " / " + (maxPage + 1);
		fontRenderer.drawString(pageString, x - fontRenderer.getStringWidth(pageString) / 2, y, 0x404040);
	}

	private int getSearchedItemNumber() {
		int count = 0;
		for (ItemIdentifierStack item : _allItems) {
			if (search == null || search.itemSearched(item.getItem())) {
				count++;
			}
		}
		return count;
	}

	public void renderAmount(int stackAmount) {
		int requestCount = requestCountBar.getInteger();
		String StackrequestCount = "" + (requestCount / stackAmount) + "+" + (requestCount % stackAmount);
		//fontRenderer.drawString(requestCount + "", x - fontRenderer.getStringWidth(requestCount + "") / 2, y, 0x404040);
		fontRenderer.drawString(StackrequestCount + "", this.amountPosLeft - fontRenderer.getStringWidth(StackrequestCount + "") / 2, this.amountPosTop + 11, 0x404040);

		requestCountBar.drawTextBox();
	}

	public void renderItemArea(double zLevel) {
		GlStateManager.pushMatrix();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		screen.drawRect(left, top, left + width, top + height, Color.GREY);

		tooltip = null;
		int ppi = 0;
		int panelxSize = 20;
		int panelySize = 20;
		int x = 2;
		int y = 2;
		ScaledResolution scaledresolution = new ScaledResolution(mc);
		int scaleX = scaledresolution.getScaledWidth();
		int scaleY = scaledresolution.getScaledHeight();
		int mouseX = Mouse.getX() * scaleX / mc.displayWidth - left;
		int mouseY = scaleY - Mouse.getY() * scaleY / mc.displayHeight - top;

		GlStateManager.translate(left, top, 0.0F);

		if (!listbyserver) {
			int graphic = ((int) (System.currentTimeMillis() / 250) % 5);
			screen.getMC().renderEngine.bindTexture(ItemDisplay.TEXTURE);
			Tessellator tess = Tessellator.getInstance();
			BufferBuilder buf = tess.getBuffer();
			buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			int xPosition = (width / 2) - 50;
			int yPosition = 40;
			buf.pos(xPosition, yPosition + 100, zLevel).tex(0.04, 0.72 + (graphic * 0.03125)).endVertex();
			buf.pos(xPosition + 100, yPosition + 100, zLevel).tex(0.08, 0.72 + (graphic * 0.03125)).endVertex();
			buf.pos(xPosition + 100, yPosition, zLevel).tex(0.08, 0.69 + (graphic * 0.03125)).endVertex();
			buf.pos(xPosition, yPosition, zLevel).tex(0.04, 0.69 + (graphic * 0.03125)).endVertex();
			tess.draw();
		} else {

			for (ItemIdentifierStack itemIdentifierStack : _allItems) {
				ItemIdentifier item = itemIdentifierStack.getItem();
				if (search != null && !search.itemSearched(item)) {
					continue;
				}
				ppi++;

				if (ppi <= itemsPerPage * page) {
					continue;
				}
				if (ppi > itemsPerPage * (page + 1)) {
					break;
				}

				//
				// -2 on both, because field starts there (see black rect below)
				int realX = x - 2;
				int realY = y - 2;

				Pair<Integer, Integer> pair = new Pair<>(realX, realY);
				if (map.get(pair) != itemIdentifierStack) {
					map.put(pair, itemIdentifierStack);
				}

				if (mouseX >= realX && mouseX < realX + panelxSize && mouseY >= realY && mouseY < realY + panelySize) {
					screen.drawRect(x - 2, y - 2, x + panelxSize - 2, y + panelySize - 2, Color.BLACK);
					screen.drawRect(x - 1, y - 1, x + panelxSize - 3, y + panelySize - 3, Color.DARKER_GREY);

					if (itemIdentifierStack.getStackSize() > 0) {
						tooltip = new Object[] { mouseX + left, mouseY + top, itemIdentifierStack.unsafeMakeNormalStack() };
					} else {
						tooltip = new Object[] { mouseX + left, mouseY + top, itemIdentifierStack.getItem().unsafeMakeNormalStack(1) };
					}
				}

				/*if (lastClickedx >= realX && lastClickedx < realX + panelxSize && lastClickedy >= realY && lastClickedy < realY + panelySize) {
					selectedItem = itemIdentifierStack;
					screen.drawRect(x - 2, y - 2, x + panelxSize - 2, y + panelySize - 2, Color.BLACK);
					screen.drawRect(x - 1, y - 1, x + panelxSize - 3, y + panelySize - 3, Color.LIGHTER_GREY);
					screen.drawRect(x, y, x + panelxSize - 4, y + panelySize - 4, Color.DARKER_GREY);
				}
				 */
				if (selectedItem == itemIdentifierStack) {
					screen.drawRect(x - 2, y - 2, x + panelxSize - 2, y + panelySize - 2, Color.BLACK);
					screen.drawRect(x - 1, y - 1, x + panelxSize - 3, y + panelySize - 3, Color.LIGHTER_GREY);
					screen.drawRect(x, y, x + panelxSize - 4, y + panelySize - 4, Color.DARKER_GREY);
					if (renderer != null) {
						renderer.specialItemRendering(itemIdentifierStack.getItem(), x, y);
					}
				}

				GlStateManager.enableLighting();
				// use GuiGraphics to render the ItemStacks
				stackRenderer.setPosX(x).setPosY(y).setItemIdentStack(itemIdentifierStack).setDisplayAmount(DisplayAmount.HIDE_ONE).renderInGui();
				GlStateManager.disableLighting();

				x += panelxSize;
				if (x > width) {
					x = 2;
					y += panelySize;
				}
			}

		}
		GlStateManager.popMatrix();

	}

	public void handleMouse() {
		boolean isShift = Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
		boolean isControl = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
		int wheel = Mouse.getEventDWheel() / 120;
		if (wheel == 0) {
			return;
		}

		if (isShift && !isControl && isShiftPageChange()) {
			if (wheel > 0) {
				if (!Configs.LOGISTICS_ORDERER_PAGE_INVERTWHEEL) {
					prevPage();
				} else {
					nextPage();
				}
			} else {
				if (!Configs.LOGISTICS_ORDERER_PAGE_INVERTWHEEL) {
					nextPage();
				} else {
					prevPage();
				}
			}
		} else if (!requestCountBar.isFocused()) {
			int requestCount = requestCountBar.getInteger();
			if (isShift && !isControl && !isShiftPageChange()) {
				if (wheel > 0) {
					if (!Configs.LOGISTICS_ORDERER_COUNT_INVERTWHEEL) {
						requestCount = Math.max(1, requestCount - (wheel * getAmountChangeMode(4)));
					} else {
						if (requestCount == 1) {
							requestCount -= 1;
						}
						requestCount += wheel * getAmountChangeMode(4);
					}
				} else {
					if (!Configs.LOGISTICS_ORDERER_COUNT_INVERTWHEEL) {
						if (requestCount == 1) {
							requestCount -= 1;
						}
						requestCount += -(wheel * getAmountChangeMode(4));
					} else {
						requestCount = Math.max(1, requestCount + wheel * getAmountChangeMode(4));
					}
				}
			} else if (!isControl) {
				if (wheel > 0) {
					if (!Configs.LOGISTICS_ORDERER_COUNT_INVERTWHEEL) {
						requestCount = Math.max(1, requestCount - (wheel * getAmountChangeMode(1)));
					} else {
						requestCount += wheel * getAmountChangeMode(1);
					}
				} else {
					if (!Configs.LOGISTICS_ORDERER_COUNT_INVERTWHEEL) {
						requestCount += -(wheel * getAmountChangeMode(1));
					} else {
						requestCount = Math.max(1, requestCount + wheel * getAmountChangeMode(1));
					}
				}
			} else if (isControl && !isShift) {
				if (wheel > 0) {
					if (!Configs.LOGISTICS_ORDERER_COUNT_INVERTWHEEL) {
						requestCount = Math.max(1, requestCount - wheel * getAmountChangeMode(2));
					} else {
						if (requestCount == 1) {
							requestCount -= 1;
						}
						requestCount += wheel * getAmountChangeMode(2);
					}
				} else {
					if (!Configs.LOGISTICS_ORDERER_COUNT_INVERTWHEEL) {
						if (requestCount == 1) {
							requestCount -= 1;
						}
						requestCount += -wheel * getAmountChangeMode(2);
					} else {
						requestCount = Math.max(1, requestCount + wheel * getAmountChangeMode(2));
					}
				}
			} else if (isControl && isShift) {
				if (wheel > 0) {
					if (!Configs.LOGISTICS_ORDERER_COUNT_INVERTWHEEL) {
						requestCount = Math.max(1, requestCount - wheel * getAmountChangeMode(3));
					} else {
						if (requestCount == 1) {
							requestCount -= 1;
						}
						requestCount += wheel * getAmountChangeMode(3);
					}
				} else {
					if (!Configs.LOGISTICS_ORDERER_COUNT_INVERTWHEEL) {
						if (requestCount == 1) {
							requestCount -= 1;
						}
						requestCount += -wheel * getAmountChangeMode(3);
					} else {
						requestCount = Math.max(1, requestCount + wheel * getAmountChangeMode(3));
					}
				}
			}
			requestCountBar.setInteger(requestCount);
		}
	}

	private int getAmountChangeMode(int step) {
		return amountChangeMode[step - 1];
	}

	private boolean isShiftPageChange() {
		return shiftPageChange;
	}

	public Object[] getToolTip() {
		return tooltip;
	}

	public void resetAmount() {
		requestCountBar.setInteger(1);
	}

	public void setMaxAmount() {
		if (selectedItem != null && selectedItem.getStackSize() != 0) {
			requestCountBar.setInteger(selectedItem.getStackSize());
		}
	}

	public void nextPage() {
		if (page < maxPage) {
			page++;
		} else {
			page = 0;
		}
	}

	public void prevPage() {
		if (page > 0) {
			page--;
		} else {
			page = maxPage;
		}
	}

	public void add(int i) {
		int requestCount = requestCountBar.getInteger();
		if (i != 1 && requestCount == 1) {
			requestCount -= 1;
		}
		requestCountBar.setInteger(requestCount + getAmountChangeMode(i));
	}

	public void sub(int i) {
		requestCountBar.setInteger(requestCountBar.getInteger() - getAmountChangeMode(i));
	}

	public ItemIdentifierStack getSelectedItem() {
		return selectedItem;
	}

	public int getRequestCount() {
		return requestCountBar.getInteger();
	}

	public boolean handleClick(int x, int y, int k) {
		if (requestCountBar.handleClick(x, y, k)) {
			return true;
		}
		x -= left;
		y -= top;
		if (x < 0 || y < 0 || x > width || y > height) {
			return false;
		}
		selectedItem = null;
		for (Entry<Pair<Integer, Integer>, ItemIdentifierStack> entry : map.entrySet()) {
			if (x >= entry.getKey().getValue1() && x < entry.getKey().getValue1() + ItemDisplay.PANELSIZEX && y >= entry.getKey().getValue2() && y < entry.getKey().getValue2() + ItemDisplay.PANELSIZEY) {
				selectedItem = entry.getValue();
				return true;
			}
		}
		return false;
	}

	public boolean keyTyped(char c, int i) {
		if (!requestCountBar.handleKey(c, i)) {
			if (i == 30 && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) { //Ctrl-a
				setMaxAmount();
				return true;
			} else if (i == 32 && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) { //Ctrl-d
				resetAmount();
				return true;
			} else if (i == 201) { //PgUp
				prevPage();
				return true;
			} else if (i == 209) { //PgDn
				nextPage();
				return true;
			}
			return false;
		}
		return true;
	}

	public void setFocused(boolean value) {
		requestCountBar.setFocused(value);
	}
}
