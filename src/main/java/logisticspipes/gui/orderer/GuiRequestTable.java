package logisticspipes.gui.orderer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import logisticspipes.Configs;
import logisticspipes.gui.popup.GuiRequestPopup;
import logisticspipes.interfaces.ISlotClick;
import logisticspipes.interfaces.ISpecialItemRenderer;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.orderer.OrdererRefreshRequestPacket;
import logisticspipes.network.packets.orderer.RequestComponentPacket;
import logisticspipes.network.packets.orderer.RequestSubmitListPacket;
import logisticspipes.network.packets.orderer.RequestSubmitPacket;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiCheckBox;
import logisticspipes.utils.gui.IItemSearch;
import logisticspipes.utils.gui.ISubGuiControler;
import logisticspipes.utils.gui.ItemDisplay;
import logisticspipes.utils.gui.KraphtBaseGuiScreen;
import logisticspipes.utils.gui.SearchBar;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;

public class GuiRequestTable extends KraphtBaseGuiScreen implements IItemSearch, ISpecialItemRenderer {

	private enum DisplayOptions {
		Both,
		SupplyOnly,
		CraftOnly,
	}

	protected DisplayOptions displayOptions = DisplayOptions.Both;
	public final PipeBlockRequestTable _table;

	public final EntityPlayer _entityPlayer;
	public ItemDisplay itemDisplay;
	private SearchBar search;
	
	protected final String _title = "Request items";

	public int dimension;
	private boolean showRequest = true;
	private int	startLeft;
	private int	startXSize;
	
	public GuiRequestTable(EntityPlayer entityPlayer, PipeBlockRequestTable table) {
		super(410, 240, 0, 0);
		_table = table;
		_entityPlayer = entityPlayer;
		if(GuiOrderer.cachetime + 100 < System.currentTimeMillis()) {
			dimension = MainProxy.getDimensionForWorld(_table.getWorld());
		} else {
			dimension = GuiOrderer.dimensioncache;
		}
		DummyContainer dummy = new DummyContainer(entityPlayer.inventory, _table.matrix);
		int i = 0;
		for(int y = 0;y < 3;y++) {
			for(int x = 0;x < 9;x++) {
				dummy.addNormalSlot(i++, _table.inv, guiLeft + (x * 18) + 20, guiTop + (y * 18) + 80);
			}
		}
		i = 0;
		for(int y = 0;y < 3;y++) {
			for(int x = 0;x < 3;x++) {
				dummy.addDummySlot(i++, guiLeft + (x * 18) + 20, guiTop + (y * 18) + 15);
			}
		}
		dummy.addCallableSlotHandler(0, _table.resultInv, guiLeft + 101, guiTop + 33, new ISlotClick() {
			@Override
			public ItemStack getResultForClick() {
				return _table.getResultForClick();
			}
		});
		dummy.addNormalSlot(0, _table.toSortInv, guiLeft + 164, guiTop + 51);
		dummy.addNormalSlotsForPlayerInventory(20, 150);
		this.inventorySlots = dummy;
		refreshItems();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initGui() {
		boolean reHide = false;
		if(!showRequest) {
			guiLeft = startLeft;
			xSize = startXSize;
			showRequest = true;
			reHide = true;
		}
		super.initGui();

		buttonList.clear();
		buttonList.add(new GuiButton(0, right - 55, bottom - 25, 50,20,"Request")); // Request
		buttonList.add(new SmallGuiButton(1,  right - 15  , guiTop + 5, 10 ,10 ,">")); // Next page
		buttonList.add(new SmallGuiButton(2,  right - 90  , guiTop + 5, 10, 10, "<")); // Prev page
		buttonList.add(new SmallGuiButton(10, right - 148 , bottom - 15, 26, 10, "---")); // -64
		buttonList.add(new SmallGuiButton(4,  right - 148 , bottom - 26, 15, 10, "--")); // -10
		buttonList.add(new SmallGuiButton(5,  right - 132 , bottom - 26, 10, 10, "-")); // -1
		buttonList.add(new SmallGuiButton(6,  right - 86 , bottom - 26, 10, 10, "+")); // +1
		buttonList.add(new SmallGuiButton(7,  right - 74 , bottom - 26, 15, 10, "++")); // +10
		buttonList.add(new SmallGuiButton(11, right - 86 , bottom - 15, 26, 10, "+++")); // +64
		buttonList.add(new GuiCheckBox(8, guiLeft + 209, bottom - 60, 14, 14, Configs.DISPLAY_POPUP)); // Popup
		
		buttonList.add(new SmallGuiButton(3, guiLeft + 210, bottom - 15, 46, 10, "Refresh")); // Refresh
		buttonList.add(new SmallGuiButton(13,  guiLeft + 210, bottom - 28, 46, 10, "Content")); // Component
		buttonList.add(new SmallGuiButton(9, guiLeft + 210, bottom - 41, 46, 10, "Both"));
		
		buttonList.add(new SmallGuiButton(14, guiLeft + 96, guiTop + 53, 10, 10, "+")); // +1
		buttonList.add(new SmallGuiButton(15, guiLeft + 108, guiTop + 53, 15, 10, "++")); // +10
		buttonList.add(new SmallGuiButton(16, guiLeft + 96, guiTop + 64, 26, 10, "+++")); // +64
		
		buttonList.add(new SmallGuiButton(17, guiLeft + 173, guiTop + 5, 36, 10, "Hide")); // +64

		if(search == null) search = new SearchBar(fontRenderer, this, guiLeft + 205, bottom - 78, 200, 15);
		if(itemDisplay == null) itemDisplay = new ItemDisplay(this, fontRenderer, this, this, guiLeft + 205, guiTop + 18, 200, ySize - 100, new int[]{1,10,64,64}, true);
		
		startLeft = guiLeft;
		startXSize = xSize;
		if(reHide) {
			showRequest = false;
			xSize = startXSize - 210;
			guiLeft = startLeft + 105;
			for(int i=13;i<16;i++) {
				((GuiButton)buttonList.get(i)).xPosition += 105;
			}
			((GuiButton)buttonList.get(16)).xPosition += 90;
			((SmallGuiButton)buttonList.get(16)).displayString = "Show";
			for(int i=0; i< 13;i++) {
				((GuiButton)buttonList.get(i)).drawButton = false;
			}
		}
	}
	
	@Override
	public void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right - (showRequest ? 0 : 105), bottom, zLevel, true);
		
		if(showRequest) {
			fontRenderer.drawString(_title, guiLeft + 180 + fontRenderer.getStringWidth(_title) / 2, guiTop + 6, 0x404040);
			itemDisplay.renderPageNumber(right - 47, guiTop + 6);
			
			
			if(buttonList.get(9) instanceof GuiCheckBox && ((GuiCheckBox)buttonList.get(9)).getState()) {
				fontRenderer.drawString("Popup", guiLeft + 225 , bottom - 56, 0x404040);
			} else {
				fontRenderer.drawString("Popup", guiLeft + 225 , bottom - 56, 0xA0A0A0);
			}
			
			itemDisplay.renderAmount(right - 103, bottom - 24, getStackAmount());
			//SearchInput
			search.renderSearchBar();
			
			itemDisplay.renderItemArea(zLevel);
			
			/*
			drawRect(guiLeft + 180, guiTop + 11, guiLeft + 200, guiTop + 6, Colors.DarkGrey);
			for(int a = 0; a < 10;a++) {
				drawRect(guiLeft + 202 - a, guiTop + 8 - a, guiLeft + 203 - a, guiTop + 10 + a, Colors.DarkGrey);
			}
			*/
		}
		
		
		for(int x = 0;x < 9;x++) {
			for(int y = 0;y < 3;y++) {
				BasicGuiHelper.drawSlotBackground(mc, guiLeft + (x * 18) + 19, guiTop + (y * 18) + 79);
			}
		}
		for(int x = 0;x < 3;x++) {
			for(int y = 0;y < 3;y++) {
				BasicGuiHelper.drawSlotBackground(mc, guiLeft + (x * 18) + 19, guiTop + (y * 18) + 14);
			}
		}
		fontRenderer.drawString("Sort:", guiLeft + 136, guiTop + 55, 0xffffff);
		BasicGuiHelper.drawSlotBackground(mc, guiLeft + 100, guiTop + 32);
		BasicGuiHelper.drawSlotBackground(mc, guiLeft + 163, guiTop + 50);
		drawRect(guiLeft + 75, guiTop + 38, guiLeft + 95, guiTop + 43, Colors.DarkGrey);
		for(int a = 0; a < 10;a++) {
			drawRect(guiLeft + 97 - a, guiTop + 40 - a, guiLeft + 98 - a, guiTop + 41 + a, Colors.DarkGrey);
		}
		for(int a = 0; a < 15;a++) {
			drawRect(guiLeft + 164 + a, guiTop + 51 + a, guiLeft + 166 + a, guiTop + 53 + a, Colors.DarkGrey);
			drawRect(guiLeft + 164 + a, guiTop + 65 - a, guiLeft + 166 + a, guiTop + 67 - a, Colors.DarkGrey);
		}
		BasicGuiHelper.drawPlayerInventoryBackground(mc, guiLeft + 20, guiTop + 150);
	}

	public void refreshItems() {
			int integer;
			switch(displayOptions) {
			case Both:
				integer = 0;
				break;
			case SupplyOnly:
				integer = 1;
				break;
			case CraftOnly:
				integer = 2;
				break;
			default: 
				integer = 3;
			}
			integer += (dimension * 10);
			MainProxy.sendPacketToServer(PacketHandler.getPacket(OrdererRefreshRequestPacket.class).setInteger(integer).setTilePos(_table.container));
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton.id == 0 && itemDisplay.getSelectedItem() != null) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(RequestSubmitPacket.class).setDimension(dimension).setStack(itemDisplay.getSelectedItem().getItem().makeStack(itemDisplay.getRequestCount())).setTilePos(_table.container));
			refreshItems();
		} else if (guibutton.id == 1) {
			itemDisplay.nextPage();
		} else if (guibutton.id == 2) {
			itemDisplay.prevPage();
		} else if (guibutton.id == 3) {
			refreshItems();
		} else if (guibutton.id == 10) {
			itemDisplay.sub(3);
		} else if (guibutton.id == 4) {
			itemDisplay.sub(2);
		} else if (guibutton.id == 5) {
			itemDisplay.sub(1);
		} else if (guibutton.id == 6) {
			itemDisplay.add(1);
		} else if (guibutton.id == 7) {
			itemDisplay.add(2);
		} else if (guibutton.id == 11) {
			itemDisplay.add(3);
		} else if (guibutton.id == 8) {
			GuiCheckBox button = (GuiCheckBox)guibutton;
			Configs.DISPLAY_POPUP = button.change();
			Configs.savePopupState();
		} else if (guibutton.id == 13 && itemDisplay.getSelectedItem() != null) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(RequestComponentPacket.class).setDimension(dimension).setStack(itemDisplay.getSelectedItem().getItem().makeStack(itemDisplay.getRequestCount())).setTilePos(_table.container));
		} else if (guibutton.id == 9) {
			String displayString = "";
			switch (displayOptions){
			case Both:
				displayOptions = DisplayOptions.CraftOnly;
				displayString = "Craft";
				break;
			case CraftOnly:
				displayOptions = DisplayOptions.SupplyOnly;
				displayString = "Supply";
				break;
			case SupplyOnly:
				displayOptions = DisplayOptions.Both;
				displayString = "Both";
				break;
			}
			guibutton.displayString = displayString;
			refreshItems();
		} else if(guibutton.id == 14) {
			requestMatrix(1);
		} else if(guibutton.id == 15) {
			requestMatrix(10);
		} else if(guibutton.id == 16) {
			requestMatrix(64);
		} else if(guibutton.id == 17) {
			showRequest = !showRequest;
			if(showRequest) {
				xSize = startXSize;
				guiLeft = startLeft;
				for(int i=13;i<16;i++) {
					((GuiButton)buttonList.get(i)).xPosition -= 105;
				}
				((GuiButton)buttonList.get(16)).xPosition -= 90;
			} else {
				xSize = startXSize - 210;
				guiLeft = startLeft + 105;
				for(int i=13;i<16;i++) {
					((GuiButton)buttonList.get(i)).xPosition += 105;
				}
				((GuiButton)buttonList.get(16)).xPosition += 90;
			}
			((SmallGuiButton)buttonList.get(16)).displayString = showRequest ? "Hide" : "Show";
			for(int i=0; i< 13;i++) {
				((GuiButton)buttonList.get(i)).drawButton = showRequest;
			}
		}
	}

	private void requestMatrix(int multiplier) {
		ArrayList<ItemIdentifierStack> list = new ArrayList<ItemIdentifierStack>(9);
		for(Entry<ItemIdentifier,Integer> e : _table.matrix.getItemsAndCount().entrySet()) {
			list.add(e.getKey().makeStack(e.getValue() * multiplier));
		}
		MainProxy.sendPacketToServer(PacketHandler.getPacket(RequestSubmitListPacket.class).setIdentList(list).setTilePos(_table.container));
		refreshItems();
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Request_Table_ID;
	}

	protected int getStackAmount() {
		return 64;
	}

	public void handlePacket(Collection<ItemIdentifierStack> allItems) {
		itemDisplay.setItemList(allItems);
	}

	@Override
	public void specialItemRendering(ItemIdentifier item, int x, int y) {}
	
	@Override
	public void drawGuiContainerForegroundLayer(int par1, int par2) {
		if(super.hasSubGui()) return;
		BasicGuiHelper.displayItemToolTip(itemDisplay.getToolTip(), this, this.zLevel, guiLeft, guiTop);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean itemSearched(ItemIdentifier item) {
		if(search.isEmpty()) return true;
		if(isSearched(item.getFriendlyName().toLowerCase(), search.getContent().toLowerCase())) return true;
		if(isSearched(String.valueOf(item.itemID), search.getContent())) return true;
		//Enchantment? Enchantment!
		Map<Integer,Integer> enchantIdLvlMap = EnchantmentHelper.getEnchantments(item.unsafeMakeNormalStack(1));
		for(Entry<Integer,Integer> e:enchantIdLvlMap.entrySet()) {
			if (e.getKey().intValue() < Enchantment.enchantmentsList.length && Enchantment.enchantmentsList[e.getKey()] != null) {
				String enchantname = Enchantment.enchantmentsList[e.getKey()].getTranslatedName(e.getValue());
				if(enchantname != null) {
					if(isSearched(enchantname.toLowerCase(), search.getContent().toLowerCase())) return true;
				}
			}
		}
		return false;
	}
	
	private boolean isSearched(String value, String search) {
		boolean flag = true;
		for(String s:search.split(" ")) {
			if(!value.contains(s)) {
				flag = false;
			}
		}
		return flag;
	}
	
	@Override
	protected void mouseClicked(int i, int j, int k) {
		if(showRequest) {
			itemDisplay.handleClick(i, j, k);
			search.handleClick(i, j, k);
		}
		super.mouseClicked(i, j, k);
	}
	
	@Override
	public void handleMouseInputSub() {
		if(showRequest) itemDisplay.handleMouse();
		super.handleMouseInputSub();
	}

	public void handleRequestAnswer(Collection<ItemIdentifierStack> items, boolean error, ISubGuiControler control, EntityPlayer player) {
		while(control.hasSubGui()) {
			control = control.getSubGui();
		}
		if (error) {
			control.setSubGui(new GuiRequestPopup(_entityPlayer, "You are missing:", items));
		} else {
			control.setSubGui(new GuiRequestPopup(_entityPlayer, "Request successful!", items));
		}
	}

	public void handleSimulateAnswer(Collection<ItemIdentifierStack> used, Collection<ItemIdentifierStack> missing, ISubGuiControler control, EntityPlayer player) {
		while(control.hasSubGui()) {
			control = control.getSubGui();
		}
		control.setSubGui(new GuiRequestPopup(_entityPlayer, "Components: ", used, "Missing: ", missing));
	}

	@Override
	public void handleKeyboardInputSub() {
		if(Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
			if(Keyboard.isKeyDown(Keyboard.KEY_A)) {
				itemDisplay.setMaxAmount();
			} else if(Keyboard.isKeyDown(Keyboard.KEY_D)) {
				itemDisplay.reset();
			}
		}
		super.handleKeyboardInputSub();
	}

	@Override
	protected void keyTyped(char c, int i) {
		// Track everything except Escape when in search bar
		if(i == 1 || !search.handleKey(c, i)) {
			super.keyTyped(c, i);
		}
	}

	@Override
	public void resetSubGui() {
		super.resetSubGui();
		refreshItems();
	}
}
