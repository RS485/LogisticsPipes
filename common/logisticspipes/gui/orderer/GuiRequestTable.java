package logisticspipes.gui.orderer;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import logisticspipes.Configs;
import logisticspipes.LogisticsPipes;
import logisticspipes.gui.popup.GuiDiskPopup;
import logisticspipes.gui.popup.GuiRequestPopup;
import logisticspipes.gui.popup.RequestMonitorPopup;
import logisticspipes.interfaces.IDiskProvider;
import logisticspipes.interfaces.ISlotClick;
import logisticspipes.interfaces.ISpecialItemRenderer;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.block.CraftingCycleRecipe;
import logisticspipes.network.packets.orderer.DiskRequestConectPacket;
import logisticspipes.network.packets.orderer.OrdererRefreshRequestPacket;
import logisticspipes.network.packets.orderer.RequestComponentPacket;
import logisticspipes.network.packets.orderer.RequestSubmitListPacket;
import logisticspipes.network.packets.orderer.RequestSubmitPacket;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.proxy.MainProxy;
import logisticspipes.routing.order.IOrderInfoProvider;
import logisticspipes.routing.order.LinkedLogisticsOrderList;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiCheckBox;
import logisticspipes.utils.gui.IItemSearch;
import logisticspipes.utils.gui.ISubGuiControler;
import logisticspipes.utils.gui.ItemDisplay;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import logisticspipes.utils.gui.SearchBar;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.gui.extention.GuiExtention;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.string.ChatColor;
import logisticspipes.utils.string.StringUtil;
import logisticspipes.utils.tuples.Pair;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class GuiRequestTable extends LogisticsBaseGuiScreen implements IItemSearch, ISpecialItemRenderer, IDiskProvider {

	private enum DisplayOptions {
		Both,
		SupplyOnly,
		CraftOnly,
	}

	protected DisplayOptions displayOptions = DisplayOptions.Both;
	public final PipeBlockRequestTable _table;
	private SmallGuiButton Macrobutton;

	public final EntityPlayer _entityPlayer;
	public ItemDisplay itemDisplay;
	private SearchBar search;
	
	protected final String _title = "Request items";

	public int dimension;
	private boolean showRequest = true;
	private int	startLeft;
	private int	startXSize;
	
	private BitSet handledExtention = new BitSet();
	private int orderIdForButton;
	
	private GuiButton[] sycleButtons = new GuiButton[2];
	
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
		dummy.addNormalSlot(0, _table.diskInv, guiLeft + 164, guiTop + 25);
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
		buttonList.add(new SmallGuiButton(20, right - 116, bottom - 41, 26, 10, "Sort")); // Sort
		
		buttonList.add(new SmallGuiButton(14, guiLeft + 96, guiTop + 53, 10, 10, "+")); // +1
		buttonList.add(new SmallGuiButton(15, guiLeft + 108, guiTop + 53, 15, 10, "++")); // +10
		buttonList.add(new SmallGuiButton(16, guiLeft + 96, guiTop + 64, 26, 10, "+++")); // +64
		
		buttonList.add(new SmallGuiButton(17, guiLeft + 173, guiTop + 5, 36, 10, "Hide")); // Hide
		buttonList.add(Macrobutton = new SmallGuiButton(18, right - 55, bottom - 60, 50, 10, "Disk"));
		Macrobutton.enabled = false;
		
		(sycleButtons[0] = addButton(new SmallGuiButton(21, guiLeft + 124, guiTop + 30, 15, 10, "/\\"))).visible = false;
		(sycleButtons[1] = addButton(new SmallGuiButton(22, guiLeft + 124, guiTop + 42, 15, 10, "\\/"))).visible = false;

		if(search == null) search = new SearchBar(mc.fontRenderer, this, guiLeft + 205, bottom - 78, 200, 15);
		search.reposition(guiLeft + 205, bottom - 78, 200, 15);
		
		if(itemDisplay == null) itemDisplay = new ItemDisplay(this, mc.fontRenderer, this, this, guiLeft + 205, guiTop + 18, 200, ySize - 100, new int[]{1,10,64,64}, true);
		itemDisplay.reposition(guiLeft + 205, guiTop + 18, 200, ySize - 100);
		
		startLeft = guiLeft;
		startXSize = xSize;
		if(reHide) {
			showRequest = false;
			xSize = startXSize - 210;
			guiLeft = startLeft + 105;
			for(int i=14;i<17;i++) {
				((GuiButton)buttonList.get(i)).xPosition += 105;
			}
			((GuiButton)buttonList.get(17)).xPosition += 90;
			((SmallGuiButton)buttonList.get(17)).displayString = "Show";
			for(int i=0; i< 14;i++) {
				((GuiButton)buttonList.get(i)).visible = false;
			}
		}
	}
	
	@Override
	public void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		for(int c=0;c<sycleButtons.length;c++) {
			sycleButtons[c].visible = this._table.targetType != null;
		}
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right - (showRequest ? 0 : 105), bottom, zLevel, true);

		drawRect(guiLeft + 162, guiTop + 23, guiLeft + 182, guiTop + 43, Colors.Black);
		drawRect(guiLeft + 164, guiTop + 25, guiLeft + 180, guiTop + 41, Colors.DarkGrey);
		
		if(showRequest) {
			mc.fontRenderer.drawString(_title, guiLeft + 180 + mc.fontRenderer.getStringWidth(_title) / 2, guiTop + 6, 0x404040);
			itemDisplay.renderPageNumber(right - 47, guiTop + 6);
			
			
			if(buttonList.get(9) instanceof GuiCheckBox && ((GuiCheckBox)buttonList.get(9)).getState()) {
				mc.fontRenderer.drawString("Popup", guiLeft + 225 , bottom - 56, 0x404040);
			} else {
				mc.fontRenderer.drawString("Popup", guiLeft + 225 , bottom - 56, 0xA0A0A0);
			}
			
			itemDisplay.renderAmount(right - 103, bottom - 24, getStackAmount());
			//SearchInput
			search.renderSearchBar();

			itemDisplay.renderSortMode(right - 103, bottom - 52);
			itemDisplay.renderItemArea(zLevel);
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
		mc.fontRenderer.drawString("Sort:", guiLeft + 136, guiTop + 55, 0xffffff);
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
		for(final Entry<Integer, Pair<ItemIdentifierStack, LinkedLogisticsOrderList>> entry:_table.watchedRequests.entrySet()) {
			if(!handledExtention.get(entry.getKey())) {
				handledExtention.set(entry.getKey());
				extentionControllerLeft.addExtention(new GuiExtention() {
					
					private Map<Pair<Integer, Integer>, IOrderInfoProvider> ordererPosition = new HashMap<Pair<Integer, Integer>, IOrderInfoProvider>();
					private int height;
					private int width = 4;
					private GuiButton localControlledButton;
					
					@SuppressWarnings("unchecked")
					@Override
					public void renderForground(int left, int top) {
						if(!_table.watchedRequests.containsKey(entry.getKey())) {
							extentionControllerLeft.removeExtention(this);
							if(isFullyExtended() && localControlledButton != null) {
								buttonList.remove(localControlledButton);
								localControlledButton = null;
								orderIdForButton = -1;
							}
							return;
						}
						ordererPosition.clear();
						GL11.glEnable(GL12.GL_RESCALE_NORMAL);
						OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240 / 1.0F, 240 / 1.0F);
						GL11.glEnable(GL11.GL_LIGHTING);
						GL11.glEnable(GL11.GL_DEPTH_TEST);
						RenderHelper.enableGUIStandardItemLighting();
						ItemStack stack = entry.getValue().getValue1().makeNormalStack();
						itemRender.renderItemAndEffectIntoGUI(mc.fontRenderer, getMC().renderEngine, stack, left + 5, top + 5);
						itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, getMC().renderEngine, stack, left + 5, top + 5, "");
						String s = StringUtil.getFormatedStackSize(stack.stackSize);
						GL11.glDisable(GL11.GL_LIGHTING);
						GL11.glDisable(GL11.GL_DEPTH_TEST);
						itemRender.zLevel = 0.0F;
		
						// Draw number
						mc.fontRenderer.drawStringWithShadow(s, left + 22 - mc.fontRenderer.getStringWidth(s), top + 14, 16777215);
						if(this.isFullyExtended()) {
							if(localControlledButton == null || orderIdForButton != entry.getKey()) {
								if(localControlledButton != null) {
									buttonList.remove(localControlledButton);
								}
								localControlledButton = new SmallGuiButton(100, guiLeft - 35, guiTop + 10, 30, 10,"more");
								buttonList.add(localControlledButton);
								orderIdForButton = entry.getKey();
							}
							List<IOrderInfoProvider> list = entry.getValue().getValue2().getList();
							calculateSize(left, top, list);
							String ident = "ID: " + Integer.toString(entry.getKey());
							mc.fontRenderer.drawStringWithShadow(ident, left + 25, top + 7, 16777215);
							int x = left + 6;
							int y = top + 25;
							for(IOrderInfoProvider order: list) {
								stack = order.getItem().makeNormalStack();
								if(stack.stackSize <= 0) continue;
								GL11.glEnable(GL11.GL_LIGHTING);
								GL11.glEnable(GL11.GL_DEPTH_TEST);
								RenderHelper.enableGUIStandardItemLighting();
								itemRender.renderItemAndEffectIntoGUI(mc.fontRenderer, getMC().renderEngine, stack, x, y);
								itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, getMC().renderEngine, stack, x, y, "");
								s = StringUtil.getFormatedStackSize(stack.stackSize);
								GL11.glDisable(GL11.GL_LIGHTING);
								GL11.glDisable(GL11.GL_DEPTH_TEST);
								itemRender.zLevel = 0.0F;
								
								// Draw number
								mc.fontRenderer.drawStringWithShadow(s, x + 17 - mc.fontRenderer.getStringWidth(s), y + 9, 16777215);
								ordererPosition.put(new Pair<Integer, Integer>(x, y), order);
								x += 18;
								if(x > left + getFinalWidth() - 18) {
									x = left + 6;
									y += 18;
								}
							}
						} else if(isExtending()) {
							List<IOrderInfoProvider> list = entry.getValue().getValue2().getList();
							calculateSize(left, top, list);
						}
						if(!isFullyExtended() && localControlledButton != null) {
							buttonList.remove(localControlledButton);
							localControlledButton = null;
							orderIdForButton = -1;
						}
						RenderHelper.disableStandardItemLighting();
					}
					
					private void calculateSize(int left, int top, List<IOrderInfoProvider> list) {
						int x = left + 6;
						int y = 50;
						int line = 1;
						width = 4;
						for(IOrderInfoProvider order: list) {
							ItemStack stack = order.getItem().makeNormalStack();
							if(stack.stackSize <= 0) continue;
							if(line++ % (4 * 4) == 0) {
								width++;
							}
						}
						for(IOrderInfoProvider order: list) {
							ItemStack stack = order.getItem().makeNormalStack();
							if(stack.stackSize <= 0) continue;
							x += 18;
							if(x > left + getFinalWidth() - 18) {
								x = left + 6;
								y += 18;
							}
						}
						height = y;
						if(x == left + 6) {
							height -= 18;
						}
					}
					
					@Override
					public int getFinalWidth() {
						return Math.max(85, width * 18 + 8);
					}
					
					@Override
					public int getFinalHeight() {
						return Math.max(50, height);
					}

					@Override
					public void handleMouseOverAt(int xPos, int yPos) {
						if(isFullyExtended()) {
							for(Pair<Integer, Integer> key:ordererPosition.keySet()) {
								if(xPos >= key.getValue1() && xPos < key.getValue1() + 18 && yPos >= key.getValue2() && yPos < key.getValue2() + 18) {
									IOrderInfoProvider order = ordererPosition.get(key);
									List<String> list = new ArrayList<String>();
									list.add(ChatColor.BLUE + "Request Type: " + ChatColor.YELLOW + order.getType().name());
									list.add(ChatColor.BLUE + "Send to Router ID: " + ChatColor.YELLOW + order.getRouterId());
									BasicGuiHelper.displayItemToolTip(new Object[]{xPos - 10, yPos, order.getItem().makeNormalStack(), true, list}, zLevel, guiLeft, guiTop, false, false);
								}
							}
						} else {
							List<String> list = new ArrayList<String>();
							list.add(ChatColor.BLUE + "Request ID: " + ChatColor.YELLOW + entry.getKey());
							BasicGuiHelper.displayItemToolTip(new Object[]{xPos - 10, yPos, entry.getValue().getValue1().makeNormalStack(), true, list}, zLevel, guiLeft, guiTop, false, false);
						}
					}
				});
			}
		}
		super.renderExtentions();
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
				for(int i=14;i<17;i++) {
					((GuiButton)buttonList.get(i)).xPosition -= 105;
				}
				((GuiButton)buttonList.get(17)).xPosition -= 90;
			} else {
				xSize = startXSize - 210;
				guiLeft = startLeft + 105;
				for(int i=14;i<17;i++) {
					((GuiButton)buttonList.get(i)).xPosition += 105;
				}
				((GuiButton)buttonList.get(17)).xPosition += 90;
			}
			((SmallGuiButton)buttonList.get(17)).displayString = showRequest ? "Hide" : "Show";
			for(int i=0; i< 14;i++) {
				((GuiButton)buttonList.get(i)).visible = showRequest;
			}
			Macrobutton.visible = showRequest;
			orderIdForButton = -1;
		} else if(guibutton.id == 100) {
			this.extentionControllerLeft.retract();
			this.setSubGui(new RequestMonitorPopup(_table, orderIdForButton));
		} else if (guibutton.id == 18) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(DiskRequestConectPacket.class).setPosX(_table.getX()).setPosY(_table.getY()).setPosZ(_table.getZ()));
			this.setSubGui(new GuiDiskPopup(this));
		} else if (guibutton.id == 20) {
			itemDisplay.cycle();
		} else if(guibutton.id == 21 || guibutton.id == 22) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(CraftingCycleRecipe.class).setDown(guibutton.id == 22).setTilePos(this._table.container));
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

	protected int getStackAmount() {
		return 64;
	}

	public void handlePacket(Collection<ItemIdentifierStack> allItems) {
		itemDisplay.setItemList(allItems);
	}

	@Override
	public void specialItemRendering(ItemIdentifier item, int x, int y) {
		//TODO Render Thaumcraft aspects 
	}
	
	@Override
	public void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		if(super.hasSubGui()) return;
		BasicGuiHelper.displayItemToolTip(itemDisplay.getToolTip(), this, this.zLevel, guiLeft, guiTop);
		Macrobutton.enabled = _table.diskInv.getStackInSlot(0) != null && _table.diskInv.getStackInSlot(0).getItem().equals(LogisticsPipes.LogisticsItemDisk);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean itemSearched(ItemIdentifier item) {
		if(search.isEmpty()) return true;
		if(isSearched(item.getFriendlyName().toLowerCase(), search.getContent().toLowerCase())) return true;
		//if(isSearched(String.valueOf(Item.getIdFromItem(item.item)), search.getContent())) return true;
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
	protected void keyTyped(char c, int i) {
		if (i == 30 && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) { //Ctrl-a
			itemDisplay.setMaxAmount();
		} else if (i == 32 && Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) { //Ctrl-d
			itemDisplay.resetAmount();
		} else if (i == 201) { //PgUp
			itemDisplay.prevPage();
		} else if (i == 209) { //PgDn
			itemDisplay.nextPage();
		} else {
			// Track everything except Escape when in search bar
			if(i == 1 || !search.handleKey(c, i)) {
				super.keyTyped(c, i);
			}
		}
	}

	@Override
	public void resetSubGui() {
		super.resetSubGui();
		refreshItems();
	}

	@Override
	public ItemStack getDisk() {
		return _table.diskInv.getStackInSlot(0);
	}

	@Override
	public int getX() {
		return _table.getX();
	}

	@Override
	public int getY() {
		return _table.getY();
	}

	@Override
	public int getZ() {
		return _table.getZ();
	}

	@Override
	public ItemDisplay getItemDisplay() {
		return itemDisplay;
	}
}
