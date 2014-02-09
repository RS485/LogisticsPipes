/** 
 * Copyright (c) Krapht, 2011
 * 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui.orderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import logisticspipes.Configs;
import logisticspipes.gui.popup.GuiRequestPopup;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.orderer.MissingItems.ProcessedItem;
import logisticspipes.network.packets.orderer.RequestSubmitPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiCheckBox;
import logisticspipes.utils.gui.IItemSearch;
import logisticspipes.utils.gui.ISubGuiControler;
import logisticspipes.utils.gui.KraphtBaseGuiScreen;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public abstract class GuiOrderer extends KraphtBaseGuiScreen implements IItemSearch {
	// constants
	public static final String TITLE = "Request items";

	public static final int
			ITEM_BOX_X = 5,
			ITEM_BOX_Y = 18,
			ITEM_BOX_WIDTH = 200,
			ITEM_BOX_HEIGHT = 140,
			
			ITEM_WIDTH = 20,
			ITEM_HEIGHT = 20,
			ITEM_AMOUNT_WIDTH = 10,
			ITEM_AMOUNT_HEIGHT = 7,
			
			
			SCROLL_BAR_MIN_HEIGHT = 10,
			
			//buttons
			BUTTON_REQUEST = 0,
			BUTTON_REFRESH = 1,
			BUTTON_SUB_1 = 2,
			BUTTON_SUB_2 = 3,
			BUTTON_SUB_3 = 4,
			BUTTON_ADD_1 = 5,
			BUTTON_ADD_2 = 6,
			BUTTON_ADD_3 = 7,
			
			CHECKBOX_POPUP = 8
			;
			
	
	// protected final IRequestItems _itemRequester;
	public final EntityPlayer _entityPlayer;
	protected LoadedItem[] loadedItems = /* prevent possible npe */ new LoadedItem[0];

	protected int requestCount = 1;
	private GuiTextField searchField, requestCountField;

	// bunch of varables for the scroll bar
	private float scrollPos = 0;
	private int scrollBarClicked = -1;
	private float lastScrollPos = -1;

	private ToolTip tooltip = null;
	private boolean listbyserver = false;

	public final int xCoord;
	public final int yCoord;
	public final int zCoord;
	public int dimension;

	public static int dimensioncache;
	public static long cachetime;

	protected final RenderItem renderItem = new RenderItem();

	public GuiOrderer(int x, int y, int z, int dim, EntityPlayer entityPlayer) {
		super(220, 240, 0, 0);
		xCoord = x;
		yCoord = y;
		zCoord = z;
		if (cachetime + 100 < System.currentTimeMillis()) {
			dimension = dim;
		} else {
			dimension = dimensioncache;
		}
		_entityPlayer = entityPlayer;
		this.inventorySlots = new DummyContainer(entityPlayer.inventory, null);
	}

	public abstract void refreshItems();

	public void handlePacket(Collection<ItemIdentifierStack> items) {
		listbyserver = true;

		// this is the only problem with this new design, this operation is
		// expensive.

		List<ItemIdentifierStack> allItems = new ArrayList<ItemIdentifierStack>(items.size());

		allItems.addAll(items);

		Collections.sort(allItems, new ItemIdentifierStack.orderedComparitor());

		LoadedItem[] newLoaded = new LoadedItem[allItems.size()];
		int index = 0;
		
		con: for (ItemIdentifierStack stack : allItems) {
			for (LoadedItem load : loadedItems) {
				if (load.getStack().getItem() == stack.getItem()) {
					if (load.getStack().getStackSize() == stack.getStackSize()) {
						newLoaded[index] = load;
					} else {
						newLoaded[index] = new LoadedItem(stack, load.isSelected(), load.isDisplayed());
					}
					
					index++;
					continue con;
				}
			}

			newLoaded[index] = new LoadedItem(stack);
			
			index++;
		}
		
		this.loadedItems = newLoaded;
		
		updateSearch(this.searchField.getText(), false);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();

		this.guiLeft = width / 2 - xSize / 2 + xCenterOffset;
		this.guiTop = height / 2 - ySize / 2 + yCenterOffset;

		this.right = width / 2 + xSize / 2 + xCenterOffset;
		this.bottom = height / 2 + ySize / 2 + yCenterOffset;

		this.xCenter = (right + guiLeft) / 2;
		this.yCenter = (bottom + guiTop) / 2;

		buttonList.clear();
		{
			GuiButton b = new GuiButton(BUTTON_REQUEST, guiLeft + 165 + this.getRenderOffsetX(), bottom - 25 + this.getRenderOffsetY(), 50, 20, "Request");
			
			b.enabled = false;
			
			buttonList.add(b); // Request
		}
		
		buttonList.add(new SmallGuiButton(BUTTON_SUB_3, guiLeft + 59 + this.getRenderOffsetX(), bottom - 15 + this.getRenderOffsetY(), 26, 10, "---"));
		buttonList.add(new SmallGuiButton(BUTTON_SUB_2, guiLeft + 59 + this.getRenderOffsetX(), bottom - 26 + this.getRenderOffsetY(), 15, 10,"--"));
		buttonList.add(new SmallGuiButton(BUTTON_SUB_1, guiLeft + 75 + this.getRenderOffsetX(), bottom - 26 + this.getRenderOffsetY(), 10, 10,"-"));
		buttonList.add(new SmallGuiButton(BUTTON_ADD_1, guiLeft + 59 + 76 + this.getRenderOffsetX(), bottom - 26 + this.getRenderOffsetY(), 10, 10,"+"));
		buttonList.add(new SmallGuiButton(BUTTON_ADD_2, guiLeft + 71 + 76 + this.getRenderOffsetX(), bottom - 26 + this.getRenderOffsetY(), 15, 10,"++"));
		buttonList.add(new SmallGuiButton(BUTTON_ADD_3, guiLeft + 59 + 76 + this.getRenderOffsetX(), bottom - 15 + this.getRenderOffsetY(), 26,10, "+++"));
		buttonList.add(new GuiCheckBox(CHECKBOX_POPUP, guiLeft + 9 + this.getRenderOffsetX(), bottom - 60 + this.getRenderOffsetY(), 14, 14, Configs.DISPLAY_POPUP));

		this.searchField = new GuiTextField(this.fontRenderer, 30 + this.getRenderOffsetX(), ITEM_BOX_HEIGHT + 23 + this.getRenderOffsetY(), 160, this.fontRenderer.FONT_HEIGHT + 5);
		this.searchField.setMaxStringLength(30);
		this.searchField.setEnableBackgroundDrawing(true);
		this.searchField.setVisible(true);
		this.searchField.setFocused(false);
		this.searchField.setTextColor(16777215);

		this.requestCountField = new GuiTextField(this.fontRenderer, 90 + this.getRenderOffsetX(), this.ySize - 22 + this.getRenderOffsetY(), 40, this.fontRenderer.FONT_HEIGHT + 5);
		this.requestCountField.setMaxStringLength(10);
		this.requestCountField.setEnableBackgroundDrawing(true);
		this.requestCountField.setVisible(true);
		this.requestCountField.setFocused(false);
		this.requestCountField.setTextColor(16777215);
		
		updateRequestCount();
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	public void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY) {
		// super.drawScreen(i, j, f);
		// drawDefaultBackground();
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom,
				zLevel, true);
		
		guiLeft += this.getRenderOffsetX();
		guiTop += this.getRenderOffsetY();
		
		fontRenderer.drawString("Popup", guiLeft + 25, bottom - 56, 0xA0A0A0);

		GL11.glPushMatrix();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		int numOfRows = (int) Math.ceil(this.getRenderedTiles() / (float) ITEM_AMOUNT_WIDTH);
		int rowsDisplayed = ITEM_AMOUNT_HEIGHT;

		{
			// item outline
			drawRect(ITEM_BOX_X + this.guiLeft, ITEM_BOX_Y + this.guiTop, ITEM_BOX_X + ITEM_BOX_WIDTH + this.guiLeft, ITEM_BOX_Y + ITEM_BOX_HEIGHT + this.guiTop, Colors.MiddleGrey);

			{
				GL11.glScissor(
						(int) Math.ceil(((float) ITEM_BOX_X + this.guiLeft) / this.width * this.mc.displayWidth),
						(int) Math.ceil((float) (this.height - (this.guiTop + ITEM_BOX_Y + ITEM_BOX_HEIGHT)) / (this.height + 1) * this.mc.displayHeight),
						(int) Math.ceil((float) ITEM_BOX_WIDTH / this.width * this.mc.displayWidth),
						(int) Math.ceil((float) ITEM_BOX_HEIGHT / this.height * this.mc.displayHeight));
			}

			// scroll bar outline
			drawRect(ITEM_BOX_X + 1 + ITEM_BOX_WIDTH + this.guiLeft, ITEM_BOX_Y + this.guiTop, ITEM_BOX_X + 1 + ITEM_BOX_WIDTH + 9 + this.guiLeft, this.guiTop + ITEM_BOX_Y + ITEM_BOX_HEIGHT, Colors.MiddleGrey);

			// scroll bar
			{

				int height = this.ySize - 19 - 82;
				int barHeight = Math.max(SCROLL_BAR_MIN_HEIGHT, (int) (height * ((float) rowsDisplayed / numOfRows)));

				if (barHeight > height) {
					barHeight = height;
				}

				float scrollPos = height * this.scrollPos;
				scrollPos -= barHeight * ((float) scrollPos / height);

				boolean in = mouseX - this.guiLeft > ITEM_BOX_X + ITEM_BOX_WIDTH
						&& mouseX - this.guiLeft < ITEM_BOX_X + ITEM_BOX_WIDTH + 10
						&& mouseY - this.guiTop >= ITEM_BOX_Y
						&& mouseY - this.guiTop < ITEM_BOX_Y + ITEM_BOX_HEIGHT;

				boolean over = scrollBarClicked != -1
						|| (in
								&& mouseX - this.guiLeft > ITEM_BOX_X + ITEM_BOX_WIDTH + 1
								&& mouseX - this.guiLeft < ITEM_BOX_X + ITEM_BOX_WIDTH + 1 + 8
								&& mouseY - this.guiTop > ITEM_BOX_Y + (int) scrollPos
								&& mouseY - this.guiTop < ITEM_BOX_Y + (int) scrollPos + barHeight);

				if (Mouse.isButtonDown(0)) {
					if (scrollBarClicked != -1) {
						this.scrollPos = lastScrollPos
								+ ((float) mouseY - this.scrollBarClicked)
								/ (height - barHeight);

						if (this.scrollPos > 1) {
							this.scrollPos = 1;
						} else if (this.scrollPos < 0) {
							this.scrollPos = 0;
						}
					} else if (over) {
						this.scrollBarClicked = mouseY;
						this.lastScrollPos = this.scrollPos;
					} else if (in) {
						this.scrollPos += ((float) mouseY - (this.guiTop + 17
								+ (int) scrollPos + barHeight / 2F))
								/ (height - barHeight);

						if (this.scrollPos > 1) {
							this.scrollPos = 1;
						} else if (this.scrollPos < 0) {
							this.scrollPos = 0;
						}

						this.lastScrollPos = this.scrollPos;
						this.scrollBarClicked = mouseY;
					} else {
						this.scrollBarClicked = -1;
					}
				} else {
					this.scrollBarClicked = -1;
				}

				drawRect(ITEM_BOX_X + ITEM_BOX_WIDTH + 2 + this.guiLeft, ITEM_BOX_Y + 1 + (int) scrollPos + this.guiTop,
						ITEM_BOX_X + ITEM_BOX_WIDTH + 1 + 8 + this.guiLeft, this.guiTop + ITEM_BOX_Y + (int) scrollPos + barHeight,
						this.scrollBarClicked != -1 ? Colors.Black : (over ? Colors.DarkGrey : Colors.LightGrey));
			}
		}

		tooltip = null;

		int startX = ITEM_BOX_X + this.guiLeft;
		int startY = ITEM_BOX_Y + this.guiTop;

		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		
		if (listbyserver) {
			RenderHelper.enableGUIStandardItemLighting();
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240 / 1.0F, 240 / 1.0F);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glDisable(GL11.GL_LIGHTING);

			int index = 0;
			int renderOffset = numOfRows > rowsDisplayed ? (int) (this.scrollPos * (numOfRows - rowsDisplayed) * ITEM_HEIGHT) : 0;

			for (LoadedItem litem : loadedItems){
				if (!litem.isDisplayed()) {
					continue;
				}

				ItemIdentifierStack itemIdentifierStack = litem.getStack();

				ItemIdentifier item = itemIdentifierStack.getItem();

				ItemStack itemstack = itemIdentifierStack.unsafeMakeNormalStack();

				int x = startX + (index % ITEM_AMOUNT_WIDTH) * ITEM_WIDTH;
				int y = startY + (index / ITEM_AMOUNT_WIDTH) * ITEM_HEIGHT - renderOffset;

				// make sure this tile is not out of frame
				if (y + ITEM_HEIGHT <= startY) {
					index++;

					continue;
				}

				if (y >= startY + ITEM_BOX_HEIGHT) {
					break;
				}

				if (this.scrollBarClicked == -1
						&& mouseX > startX - 3
						&& mouseX <= startX + ITEM_BOX_WIDTH
						&& mouseY > startY - 3
						&& mouseY <= startY + ITEM_BOX_HEIGHT
						&& mouseX >= x && mouseX < x + ITEM_WIDTH
						&& mouseY >= y && mouseY < y + ITEM_HEIGHT) {
					
					drawRect(x, y, x + ITEM_WIDTH, y + ITEM_HEIGHT, Colors.Black);
					drawRect(x + 1, y + 1, x + ITEM_WIDTH - 1, y + ITEM_HEIGHT - 1, Colors.DarkGrey);

					tooltip = new ToolTip(mouseX - 10, mouseY + 5, itemstack);
				}

				if (litem.isSelected()) {
					drawRect(x, y, x + ITEM_WIDTH, y + ITEM_HEIGHT, Colors.Black);
					drawRect(x + 1, y + 1, x + ITEM_WIDTH - 1, y + ITEM_HEIGHT - 1, Colors.LightGrey);
					drawRect(x + 2, y + 2, x + ITEM_WIDTH - 2, y + ITEM_HEIGHT - 2, Colors.DarkGrey);
					specialItemRendering(itemIdentifierStack.getItem(), x, y);
				}
				
				String overlay = null;
				if (itemstack.stackSize > 1) {
					if (itemstack.stackSize < 1000) {
						overlay = String.valueOf(itemstack.stackSize);
					} else if (itemstack.stackSize < 100000){
						overlay = String.valueOf((int) Math.ceil(itemstack.stackSize / 1000F)) + "K";
					} else if (itemstack.stackSize < 1000000){
						overlay = "." + String.valueOf((int) Math.ceil(itemstack.stackSize * 10F / 1000000F)) + "M";
					} else if (itemstack.stackSize < 100000000) {
						overlay = String.valueOf((int) Math.ceil(itemstack.stackSize / 1000000F)) + "M";
					} else if (itemstack.stackSize < 1000000000){ 
						overlay = "." + String.valueOf((int) Math.ceil(itemstack.stackSize * 10F / 1000000000F)) + "B";
					} else {
						overlay = String.valueOf((int) Math.ceil(itemstack.stackSize / 1000000000F)) + "B";
					}
				}

				FontRenderer font = itemstack.getItem().getFontRenderer(itemstack);
				if (font == null){
					font = fontRenderer;
				}
				
				itemRenderer.zLevel = 100;
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				GL11.glEnable(GL11.GL_LIGHTING);
				
				itemRenderer.renderItemAndEffectIntoGUI(font, this.mc.renderEngine, itemstack, x + 2, y + 2);
				itemRenderer.renderItemOverlayIntoGUI(font, this.mc.renderEngine, itemstack, x + 2, y + 2, overlay);
				
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				itemRenderer.zLevel = 0;

				index++;
			}

			GL11.glEnable(GL11.GL_DEPTH_TEST);
		}
		
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		
		GL11.glPopMatrix();
		
		guiLeft -= this.getRenderOffsetX();
		guiTop -= this.getRenderOffsetY();
	}

	public abstract void specialItemRendering(ItemIdentifier item, int x, int y);

	private int getRenderedTiles() {
		int num = 0;

		for (LoadedItem item : loadedItems){
			if (item.isDisplayed()) {
				num++;
			}
		}

		return num;
	}
	
	@Override
	public void drawGuiContainerForegroundLayer(int par1, int par2) {
		this.requestCountField.drawTextBox();
		this.searchField.drawTextBox();
		
		fontRenderer.drawString(TITLE, (xSize - fontRenderer.getStringWidth(TITLE) + this.getRenderOffsetX()) / 2,
				6 + this.getRenderOffsetY(), 0x404040);
		
		if (tooltip != null && !this.hasSubGui()) {
			BasicGuiHelper.displayItemToolTip(tooltip.toArray(), this, this.zLevel, guiLeft, guiTop);
		}
	}

	private boolean isSearched(String value, String search) {
		boolean flag = true;
		for (String s : search.split(" ")) {
			if (!value.contains(s)) {
				flag = false;
			}
		}
		return flag;
	}

	@Override
	protected void mouseClicked(int x, int y, int type) {
		super.mouseClicked(x, y, type);
		
		guiLeft += this.getRenderOffsetX();
		guiTop += this.getRenderOffsetY();
		
		this.searchField.mouseClicked(x - this.guiLeft + this.getRenderOffsetX(), y - this.guiTop + this.getRenderOffsetY(), type);
		this.requestCountField.mouseClicked(x - this.guiLeft + this.getRenderOffsetX(), y - this.guiTop + this.getRenderOffsetY(), type);
		
		if (!requestCountField.isFocused() && (requestCountField.getText() == null || requestCountField.getText().length() == 0)) {
			this.requestCount = 1;

			requestCountField.setText("1");
		}

		// select item

		int startX = ITEM_BOX_X - 1;
		int startY = ITEM_BOX_Y - 1;

		if (x - this.guiLeft > startX
				&& x - this.guiLeft <= startX + ITEM_BOX_WIDTH
				&& y - this.guiTop > startY
				&& y - this.guiTop <= startY + ITEM_BOX_HEIGHT) {

			int index = 0;

			int numOfRows = (int) Math.ceil(getRenderedTiles() / (float) ITEM_AMOUNT_WIDTH);
			int rowsDisplayed = ITEM_AMOUNT_HEIGHT;

			int renderOffset = numOfRows > rowsDisplayed ? (int) (this.scrollPos * (numOfRows - rowsDisplayed) * ITEM_HEIGHT) : 0;

			boolean ctrl = this.isCtrlKeyDown();

			startY -= renderOffset;
			
			int selected = 0;
			
			for (LoadedItem item : loadedItems){
				if (item.isDisplayed()) {
					int xx = this.guiLeft + startX + (index % ITEM_AMOUNT_WIDTH) * ITEM_WIDTH;
					int yy = this.guiTop + startY + (index / ITEM_AMOUNT_WIDTH) * ITEM_HEIGHT;

					if (x > xx && x <= xx + ITEM_WIDTH && y > yy && y <= yy + ITEM_HEIGHT) {
						item.setSelected(!item.isSelected());
					} else if (!ctrl) {
						item.setSelected(false);
					}
					
					if (item.isSelected()){
						selected++;
					}
					
					index++;
				}
			}
			
			this.getButton(BUTTON_REQUEST).enabled = selected > 0;
		}
		
		guiLeft -= this.getRenderOffsetX();
		guiTop -= this.getRenderOffsetY();
	}
	
	public int getRenderOffsetX(){
		return 0;
	}
	
	public int getRenderOffsetY(){
		return 0;
	}
	
	public void updateSearch(String searchQuery, boolean useOld) {
		for (LoadedItem item : loadedItems){
			if (!useOld || item.isDisplayed()) {
				if (searchQuery == null || searchQuery.length() == 0) {
					item.setDisplayed(true);
				} else {
					searchQuery = searchQuery.toLowerCase();

					ItemStack stack = item.getStack().makeNormalStack();

					boolean display = false;

					if (stack != null) {
						List<String> info = new ArrayList<String>();

						stack.getItem().addInformation(stack, _entityPlayer,
								info, true);

						for (String s : info) {
							if (s.toLowerCase().contains(searchQuery)) {
								display = true;
							}
						}

						if (stack.getDisplayName().toLowerCase()
								.contains(searchQuery)) {
							display = true;
						}
					}

					item.setDisplayed(display);
					
					if (!display){
						item.setSelected(false);
					}
				}
			}
		}
	}

	private void scroll(float amount) {
		int displayedRows = ITEM_AMOUNT_HEIGHT;
		int numOfRows = (int) Math.ceil(this.getRenderedTiles() / (float) ITEM_AMOUNT_WIDTH);

		if (Configs.LOGISTICS_ORDERER_PAGE_INVERTWHEEL) {
			amount = -amount;
		}

		float scrollAmount = (float) amount / (numOfRows - displayedRows);

		this.scrollPos -= scrollAmount;

		if (scrollPos < 0) {
			scrollPos = 0;
		}

		if (scrollPos > 1) {
			scrollPos = 1;
		}
	}

	@Override
	public void handleMouseInputSub() {
		boolean isShift = this.isShiftKeyDown();
		boolean isControl = this.isCtrlKeyDown();
		int wheel = Mouse.getEventDWheel() / 120;
		if (wheel == 0) {
			super.handleMouseInputSub();
			return;
		}

		if (!isShift && !isControl) {
			this.scroll(wheel);
		} else if (isShift && !isControl) {
			if (wheel > 0) {
				if (!Configs.LOGISTICS_ORDERER_COUNT_INVERTWHEEL) {
					requestCount = Math.max(1, requestCount
							- (wheel * getAmountChangeMode(1)));
				} else {
					requestCount += wheel * getAmountChangeMode(1);
				}
			} else {
				if (!Configs.LOGISTICS_ORDERER_COUNT_INVERTWHEEL) {
					requestCount += -(wheel * getAmountChangeMode(1));
				} else {
					requestCount = Math.max(1, requestCount + wheel
							* getAmountChangeMode(1));
				}
			}
		} else if (!isShift && isControl) {
			if (wheel > 0) {
				if (!Configs.LOGISTICS_ORDERER_COUNT_INVERTWHEEL) {
					requestCount = Math.max(1, requestCount
							- (wheel * getAmountChangeMode(2)));
				} else {
					if (requestCount == 1)
						requestCount -= 1;
					requestCount += wheel * getAmountChangeMode(2);
				}
			} else {
				if (!Configs.LOGISTICS_ORDERER_COUNT_INVERTWHEEL) {
					if (requestCount == 1)
						requestCount -= 1;
					requestCount += -(wheel * getAmountChangeMode(2));
				} else {
					requestCount = Math.max(1, requestCount + wheel
							* getAmountChangeMode(2));
				}
			}
		} else if (isControl && isShift) {
			if (wheel > 0) {
				if (!Configs.LOGISTICS_ORDERER_COUNT_INVERTWHEEL) {
					requestCount = Math.max(1, requestCount - wheel
							* getAmountChangeMode(3));
				} else {
					if (requestCount == 1)
						requestCount -= 1;
					requestCount += wheel * getAmountChangeMode(3);
				}
			} else {
				if (!Configs.LOGISTICS_ORDERER_COUNT_INVERTWHEEL) {
					if (requestCount == 1)
						requestCount -= 1;
					requestCount += -wheel * getAmountChangeMode(3);
				} else {
					requestCount = Math.max(1, requestCount + wheel
							* getAmountChangeMode(3));
				}
			}
		} else {
			if (wheel > 0) {
				if (!Configs.LOGISTICS_ORDERER_COUNT_INVERTWHEEL) {
					requestCount = Math.max(1, requestCount - wheel
							* getAmountChangeMode(4));
				} else {
					if (requestCount == 1)
						requestCount -= 1;
					requestCount += wheel * getAmountChangeMode(4);
				}
			} else {
				if (!Configs.LOGISTICS_ORDERER_COUNT_INVERTWHEEL) {
					if (requestCount == 1)
						requestCount -= 1;
					requestCount += -wheel * getAmountChangeMode(4);
				} else {
					requestCount = Math.max(1, requestCount + wheel
							* getAmountChangeMode(4));
				}
			}
		}

		updateRequestCount();

		super.handleMouseInputSub();
	}

	public void handleRequestAnswer(ProcessedItem[] items, ISubGuiControler control, EntityPlayer player) {
		while (control.hasSubGui()) {
			control = control.getSubGui();
		}
		
		List<String> sucessful = new ArrayList<String>(), unsucessful = new ArrayList<String>();
		
		for(ProcessedItem item : items){
			if (item.isSuccessful()){
				if (sucessful.size() == 0){
					sucessful.add("Sucessful:");
				}
				
				sucessful.add(item.getStack().getFriendlyName());
			}else{
				if (unsucessful.size() == 0){
					unsucessful.add("Missing:");
				}
				
				unsucessful.add(item.getStack().getFriendlyName());
			}
		}
		
		Object[] list;
		
		if (unsucessful.size() == 0){
			list = sucessful.toArray();
		}else{
			list = new Object[unsucessful.size() + 1 + sucessful.size()];
			int index = 0;
			
			for (String s : sucessful){
				list[index++] = s;
			}
			
			list[index++] = "";
			
			for (String s : unsucessful){
				list[index++] = s;
			}
		}
		
		
		control.setSubGui(new GuiRequestPopup(player, list));
	}

	public void handleSimulateAnswer(Collection<ItemIdentifierStack> used,
			Collection<ItemIdentifierStack> missing, ISubGuiControler control,
			EntityPlayer player) {
		while (control.hasSubGui()) {
			control = control.getSubGui();
		}
		control.setSubGui(new GuiRequestPopup(_entityPlayer, "Components: ",
				used, "Missing: ", missing));
	}

	public void requestItems() {
		if (requestCount > 0) {
			ArrayList<ItemIdentifierStack> stacks = new ArrayList<ItemIdentifierStack>();
			
			for (LoadedItem item : loadedItems){
				if (item.isSelected() && item.isDisplayed()) {
					stacks.add(item.getStack().getItem().makeStack(requestCount));
				}
			}
			
			if (stacks.size() > 0){
				MainProxy.sendPacketToServer(PacketHandler.getPacket(RequestSubmitPacket.class).setDimension(dimension)
						.setStacks(stacks.toArray(new ItemIdentifierStack[stacks.size()])).setPosX(xCoord).setPosY(yCoord).setPosZ(zCoord));
			}
		}
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (this.searchField.isFocused()) {
			this.searchField.setFocused(false);
		}

		if (guibutton.id == BUTTON_REQUEST) {
			requestItems();

			refreshItems();
		} else if (guibutton.id == BUTTON_REFRESH) {
			refreshItems();
		} else if (guibutton.id == BUTTON_SUB_3) {
			requestCount = Math.max(1, requestCount - getAmountChangeMode(3));
		} else if (guibutton.id == BUTTON_SUB_2) {
			requestCount = Math.max(1, requestCount - getAmountChangeMode(2));
		} else if (guibutton.id == BUTTON_SUB_1) {
			requestCount = Math.max(1, requestCount - getAmountChangeMode(1));
		} else if (guibutton.id == BUTTON_ADD_1) {
			requestCount += getAmountChangeMode(1);
		} else if (guibutton.id == BUTTON_ADD_2) {
			if (requestCount == 1) {
				requestCount -= 1;
			}
			requestCount += getAmountChangeMode(2);
		} else if (guibutton.id == BUTTON_ADD_3) {
			if (requestCount == 1) {
				requestCount -= 1;
			}
			requestCount += getAmountChangeMode(3);
		} else if (guibutton.id == CHECKBOX_POPUP) {
			GuiCheckBox button = (GuiCheckBox) guibutton;
			Configs.DISPLAY_POPUP = button.change();
			Configs.savePopupState();
		} else if (guibutton.id == 13) {
			requestItems();
		}
		
		updateRequestCount();

		super.actionPerformed(guibutton);
	}

	@Override
	public boolean itemSearched(ItemIdentifier item) {
		for (LoadedItem load : loadedItems){
			if (load.getStack().getItem() == item) {
				return true;
			}
		}

		return false;
	}

	protected int getAmountChangeMode(int step) {
		if (step == 1) {
			return 1;
		} else if (step == 2) {
			return 10;
		} else {
			return 64;
		}
	}
	
	@Override
	public void handleKeyboardInputSub() {
		if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
			if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
				int requestCount = Integer.MAX_VALUE;

				for (LoadedItem item : loadedItems){
					if (item.isDisplayed() && item.isSelected()) {
						requestCount = Math.min(requestCount, item.getStack().getStackSize());
					}
				}
				
				if (requestCount != Integer.MAX_VALUE){
					this.requestCount = requestCount;
					
					this.requestCountField.setText(String.valueOf(this.requestCount));
				}
			} else if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
				requestCount = 1;

				for (LoadedItem item : loadedItems){
					if (item.isSelected()) {
						item.setSelected(false);
					}
				}
			}
		}

		super.handleKeyboardInputSub();
	}

	@Override
	protected void keyTyped(char c, int i) {
		if (i == Keyboard.KEY_ESCAPE) {
			this._entityPlayer.closeScreen();
		} else if (i == Keyboard.KEY_RETURN) {
			this.requestItems();
		} else if (i == Keyboard.KEY_DOWN) {
			this.scroll(-1);
		} else if (i == Keyboard.KEY_UP) {
			this.scroll(1);
		} else if (this.searchField.isFocused()) {
			this.searchField.textboxKeyTyped(c, i);

			this.updateSearch(this.searchField.getText(), false);
		} else if (this.requestCountField.isFocused()) {
			if (i == Keyboard.KEY_0 || i == Keyboard.KEY_NUMPAD0
					|| i == Keyboard.KEY_1 || i == Keyboard.KEY_NUMPAD1
					|| i == Keyboard.KEY_2 || i == Keyboard.KEY_NUMPAD2
					|| i == Keyboard.KEY_3 || i == Keyboard.KEY_NUMPAD3
					|| i == Keyboard.KEY_4 || i == Keyboard.KEY_NUMPAD4
					|| i == Keyboard.KEY_5 || i == Keyboard.KEY_NUMPAD5
					|| i == Keyboard.KEY_6 || i == Keyboard.KEY_NUMPAD6
					|| i == Keyboard.KEY_7 || i == Keyboard.KEY_NUMPAD7
					|| i == Keyboard.KEY_8 || i == Keyboard.KEY_NUMPAD8
					|| i == Keyboard.KEY_9 || i == Keyboard.KEY_NUMPAD9
					|| i == Keyboard.KEY_DELETE || i == Keyboard.KEY_BACK
					|| i == Keyboard.KEY_RIGHT || i == Keyboard.KEY_LEFT) {
				this.requestCountField.textboxKeyTyped(c, i);

				if (this.requestCountField.getText() == null
						|| this.requestCountField.getText().length() == 0) {
					this.requestCount = 0;
				} else {
					try{
						this.requestCount = Integer.parseInt(this.requestCountField.getText());
					}catch (NumberFormatException e){
						//can only mean that they guy typed a number more then 4 billion
						this.requestCount = Integer.MAX_VALUE;
						this.requestCountField.setText(String.valueOf(this.requestCount));
					}
				}
			}
		} else {
			this.searchField.setFocused(true);
			
			this.searchField.textboxKeyTyped(c, i);
			this.updateSearch(this.searchField.getText(), false);
		}
	}
	
	public void updateRequestCount(){
		if (this.requestCount < 0){
			requestCount = Integer.MAX_VALUE;
		}
		
		this.requestCountField.setText(String.valueOf(this.requestCount));
	}
	
	@Override
	public void resetSubGui() {
		super.resetSubGui();
		refreshItems();
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Normal_Orderer_ID;
	}
	
	protected GuiButton getButton(int id){
		for (Object b : this.buttonList){
			if (((GuiButton) b).id == id){
				return ((GuiButton) b);
			}
		}
		
		return null;
	}
	
	public LoadedItem[] getLoadedItems() {
		return loadedItems;
	}
	
	public class LoadedItem {
		private final ItemIdentifierStack stack;
		private boolean selected;

		// displayed used for search feature. Only dispayed items that are
		// searched.
		private boolean displayed = true;

		public LoadedItem(ItemIdentifierStack stack) {
			this.stack = stack;
		}

		public LoadedItem(ItemIdentifierStack stack, boolean selected,
				boolean displayed) {
			this.stack = stack;
			this.selected = selected;
			this.displayed = displayed;
		}

		public ItemIdentifierStack getStack() {
			return stack;
		}

		public void setSelected(boolean selected) {
			this.selected = selected;
		}

		public void setDisplayed(boolean displayed) {
			this.displayed = displayed;
		}

		public boolean isSelected() {
			return selected;
		}

		public boolean isDisplayed() {
			return displayed;
		}
	}

	private class ToolTip {
		private final int x, y;
		private final ItemStack item;

		public ToolTip(int x, int y, ItemStack item) {
			this.x = x;
			this.y = y;
			this.item = item;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		public ItemStack getItem() {
			return item;
		}

		public Object[] toArray() {
			return new Object[] { x, y, item };
		}
	}
}
