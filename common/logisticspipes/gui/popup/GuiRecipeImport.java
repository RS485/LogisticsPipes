package logisticspipes.gui.popup;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import logisticspipes.gui.popup.SelectItemOutOfList.IHandleItemChoise;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.abstractpackets.ModernPacket;
import logisticspipes.network.packets.NEISetCraftingRecipe;
import logisticspipes.network.packets.pipe.FindMostLikelyRecipeComponents;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.Color;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.SimpleGraphics;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.gui.SubGuiScreen;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.string.StringUtils;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class GuiRecipeImport extends SubGuiScreen {

	public static class Canidates {

		public Canidates(Set<ItemIdentifierStack> set) {
			this.set = set;
		}

		Set<ItemIdentifierStack> set;
		public List<ItemIdentifierStack> order;
		int pos = 0;
	}

	private final RenderItem itemRenderer = new RenderItem();
	private final TileEntity tile;
	private final Canidates[] grid = new Canidates[9];
	private final List<Canidates> list;
	private Object[] tooltip = null;

	public GuiRecipeImport(TileEntity tile, ItemStack[][] stacks) {
		super(150, 200, 0, 0);
		this.tile = tile;
		list = new ArrayList<Canidates>();
		for (int i = 0; i < 9; i++) {
			if (stacks[i] == null) {
				continue;
			}
			Set<ItemIdentifierStack> part = new TreeSet<ItemIdentifierStack>();
			List<ItemIdentifierStack> order = new ArrayList<ItemIdentifierStack>();
			for (ItemStack stack : stacks[i]) {
				ItemIdentifierStack iStack = ItemIdentifierStack.getFromStack(stack);
				part.add(iStack);
				order.add(iStack);
			}
			Canidates canidate = new Canidates(part);
			boolean found = false;
			for (Canidates test : list) {
				if (test.set.equals(part)) {
					canidate = test;
					found = true;
					break;
				}
			}
			if (!found) {
				canidate.order = order;
				if (order.size() > 1) {
					list.add(canidate);
				}
			}
			grid[i] = canidate;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initGui() {
		super.initGui();
		buttonList.clear();
		buttonList.add(new SmallGuiButton(0, guiLeft + 100, guiTop + 180, 40, 10, "Done"));
		buttonList.add(new SmallGuiButton(1, guiLeft + 10, guiTop + 180, 60, 10, "Most likely"));
		int x = 0;
		int y = 0;
		for (Canidates canidate : list) {
			buttonList.add(new SmallGuiButton(10 + x + y * 3, guiLeft + 38 + x * 40, guiTop + 88 + y * 40, 15, 10, "/\\"));
			buttonList.add(new SmallGuiButton(20 + x + y * 3, guiLeft + 38 + x * 40, guiTop + 98 + y * 40, 15, 10, "\\/"));
			x++;
			if (x > 2) {
				x = 0;
				y++;
			}
		}
	}

	@Override
	protected void renderToolTips(int mouseX, int mouseY, float par3) {
		GuiGraphics.displayItemToolTip(tooltip, this, zLevel, guiLeft, guiTop);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		fontRendererObj.renderString(StringUtils.translate("misc.selectOreDict"), guiLeft + 10, guiTop + 6, 0x404040, false);
		tooltip = null;
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				if (grid[x + y * 3] == null) {
					continue;
				}
				ItemIdentifierStack stack = grid[x + y * 3].order.get(grid[x + y * 3].pos);
				ItemStack itemStack = stack.makeNormalStack();

				FontRenderer font = itemStack.getItem().getFontRenderer(itemStack);
				if (font == null) {
					font = fontRendererObj;
				}

				itemRenderer.renderItemAndEffectIntoGUI(font, mc.renderEngine, itemStack, guiLeft + 45 + x * 18, guiTop + 20 + y * 18);
				// With empty string, because damage value indicator struggles with the depth
				itemRenderer.renderItemOverlayIntoGUI(font, mc.renderEngine, itemStack, guiLeft + 45 + x * 18, guiTop + 20 + y * 18, null);

				if (guiLeft + 45 + x * 18 < mouseX && mouseX < guiLeft + 45 + x * 18 + 16 && guiTop + 20 + y * 18 < mouseY && mouseY < guiTop + 20 + y * 18 + 16 && !hasSubGui()) {
					SimpleGraphics.drawGradientRect(guiLeft + 45 + x * 18, guiTop + 20 + y * 18, guiLeft + 45 + x * 18 + 16, guiTop + 20 + y * 18 + 16, Color.WHITE_50, Color.WHITE_50, 0.0);
					tooltip = new Object[] { guiLeft + mouseX, guiTop + mouseY, itemStack };
				}
			}
		}
		int x = 0;
		int y = 0;
		for (Canidates canidate : list) {
			ItemIdentifierStack stack = canidate.order.get(canidate.pos);
			ItemStack itemStack = stack.makeNormalStack();
			FontRenderer font = itemStack.getItem().getFontRenderer(itemStack);
			if (font == null) {
				font = fontRendererObj;
			}

			itemRenderer.renderItemAndEffectIntoGUI(font, mc.renderEngine, itemStack, guiLeft + 20 + x * 40, guiTop + 90 + y * 40);
			// With empty string, because damage value indicator struggles with the depth
			itemRenderer.renderItemOverlayIntoGUI(font, mc.renderEngine, itemStack, guiLeft + 20 + x * 40, guiTop + 90 + y * 40, "");

			if (guiLeft + 20 + x * 40 < mouseX && mouseX < guiLeft + 20 + x * 40 + 16 && guiTop + 90 + y * 40 < mouseY && mouseY < guiTop + 90 + y * 40 + 16 && !hasSubGui()) {
				SimpleGraphics.drawGradientRect(guiLeft + 20 + x * 40, guiTop + 90 + y * 40, guiLeft + 20 + x * 40 + 16, guiTop + 90 + y * 40 + 16, Color.WHITE_50, Color.WHITE_50, 0.0);
				tooltip = new Object[] { guiLeft + mouseX, guiTop + mouseY, itemStack };
			}

			x++;
			if (x > 2) {
				x = 0;
				y++;
			}
		}
	}

	@Override
	protected void renderGuiBackground(int par1, int par2) {
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		fontRendererObj.renderString(StringUtils.translate("misc.selectOreDict"), guiLeft + 10, guiTop + 6, 0x404040, false);
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				GuiGraphics.drawSlotBackground(mc, guiLeft + 44 + x * 18, guiTop + 19 + y * 18);
			}
		}
		int x = 0;
		int y = 0;
		for (Canidates canidate : list) {
			GuiGraphics.drawSlotBackground(mc, guiLeft + 19 + x * 40, guiTop + 89 + y * 40);
			x++;
			if (x > 2) {
				x = 0;
				y++;
			}
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		int id = button.id;
		if (id == 0) {
			ItemStack[] stack = new ItemStack[9];
			int i = 0;
			for (Canidates canidate : grid) {
				if (canidate == null) {
					i++;
					continue;
				}
				stack[i++] = canidate.order.get(canidate.pos).makeNormalStack();
			}
			NEISetCraftingRecipe packet = PacketHandler.getPacket(NEISetCraftingRecipe.class);
			MainProxy.sendPacketToServer(packet.setContent(stack).setPosX(tile.xCoord).setPosY(tile.yCoord).setPosZ(tile.zCoord));
			exitGui();
		} else if(id == 1) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(FindMostLikelyRecipeComponents.class).setContent(list).setTilePos(tile));
		} else if (id >= 10 && id < 30) {
			int slot = id % 10;
			boolean up = id < 20;
			Canidates canidate = list.get(slot);
			if (up) {
				canidate.pos++;
				if (canidate.pos >= canidate.order.size()) {
					canidate.pos = 0;
				}
			} else {
				canidate.pos--;
				if (canidate.pos < 0) {
					canidate.pos = canidate.order.size() - 1;
				}
			}
		}
	}

	public void handleProposePacket(List<Integer> response) {
		if(list.size() != response.size()) return;
		for(int slot=0;slot<list.size();slot++) {
			Canidates canidate = list.get(slot);
			int newPos = response.get(slot);
			if(newPos != -1) {
				canidate.pos = newPos;
			}
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		int x = 0;
		int y = 0;
		for (final Canidates canidate : list) {

			if (guiLeft + 20 + x * 40 < mouseX && mouseX < guiLeft + 20 + x * 40 + 16 && guiTop + 90 + y * 40 < mouseY && mouseY < guiTop + 90 + y * 40 + 16) {
				setSubGui(new SelectItemOutOfList(canidate.order, new IHandleItemChoise() {

					@Override
					public void handleItemChoise(int slot) {
						canidate.pos = slot;
					}
				}));
			}

			x++;
			if (x > 2) {
				x = 0;
				y++;
			}
		}
		super.mouseClicked(mouseX, mouseY, button);
	}
}
