package logisticspipes.gui.popup;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.lwjgl.opengl.GL11;

import logisticspipes.gui.popup.SelectItemOutOfList.IHandleItemChoise;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.NEISetCraftingRecipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.gui.SubGuiScreen;
import logisticspipes.utils.item.ItemIdentifierStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class GuiRecipeImport extends SubGuiScreen {
	public static class Canidates {
		Canidates(Set<ItemIdentifierStack> set) {
			this.set = set;
		}
		Set<ItemIdentifierStack> set;
		List<ItemIdentifierStack> order;
		int pos = 0;
	}

	private final RenderItem itemRenderer = new RenderItem();
	private final TileEntity tile;
	private final Canidates[] grid = new Canidates[9];
	private final List<Canidates> list;
	
	public GuiRecipeImport(TileEntity tile, ItemStack[][] stacks) {
		super(150, 200, 0, 0);
		this.tile = tile;
		list = new ArrayList<Canidates>();
		for(int i=0; i < 9; i++) {
			if(stacks[i] == null) continue;
			Set<ItemIdentifierStack> part = new TreeSet<ItemIdentifierStack>();
			List<ItemIdentifierStack> order = new ArrayList<ItemIdentifierStack>();
			for(ItemStack stack:stacks[i]) {
				ItemIdentifierStack iStack = ItemIdentifierStack.getFromStack(stack);
				part.add(iStack);
				order.add(iStack);
			}
			Canidates canidate = new Canidates(part);
			boolean found = false;
			for(Canidates test:list) {
				if(test.set.equals(part)) {
					canidate = test;
					found = true;
					break;
				}
			}
			if(!found) {
				canidate.order = order;
				if(order.size() > 1) {
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
		int x=0;
		int y=0;
		for(Canidates canidate: list) {
			buttonList.add(new SmallGuiButton(10 + x + y*3, guiLeft + 38 + x * 40, guiTop + 88 + y * 40, 15, 10, "/\\"));
			buttonList.add(new SmallGuiButton(20 + x + y*3, guiLeft + 38 + x * 40, guiTop + 98 + y * 40, 15, 10, "\\/"));
			x++;
			if(x>2) {
				x=0;
				y++;
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float par3) {
		GL11.glEnable(GL11.GL_BLEND);
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		fontRendererObj.renderString("Test", guiLeft + 10, guiTop + 6, 0x404040, false);
		GL11.glTranslated(0, 0, 100);
		for(int x=0; x<3;x++) {
			for(int y=0;y<3;y++) {
				RenderHelper.enableGUIStandardItemLighting();
				BasicGuiHelper.drawSlotBackground(mc, guiLeft + 44 + x * 18, guiTop + 19 + y * 18);
				
				if(grid[x + y*3] == null) continue;
				ItemIdentifierStack stack = grid[x + y*3].order.get(grid[x + y*3].pos);
				ItemStack itemStack = stack.makeNormalStack();
				
				FontRenderer font = itemStack.getItem().getFontRenderer(itemStack);
				if (font == null) {
					font = fontRendererObj;
				}
				
				itemRenderer.renderItemAndEffectIntoGUI(font, this.mc.renderEngine, itemStack, guiLeft + 45 + x * 18, guiTop + 20 + y * 18);
				// With empty string, because damage value indicator struggles with the depth
				itemRenderer.renderItemOverlayIntoGUI(font, this.mc.renderEngine, itemStack, guiLeft + 45 + x * 18, guiTop + 20 + y * 18, null);
			}
		}
		int x = 0;
		int y = 0;
		for(Canidates canidate: list) {
			ItemIdentifierStack stack = canidate.order.get(canidate.pos);
			ItemStack itemStack = stack.makeNormalStack();
			FontRenderer font = itemStack.getItem().getFontRenderer(itemStack);
			if (font == null) {
				font = fontRendererObj;
			}
			
			RenderHelper.enableGUIStandardItemLighting();
			BasicGuiHelper.drawSlotBackground(mc, guiLeft + 19 + x * 40, guiTop + 89 + y * 40);
			itemRenderer.renderItemAndEffectIntoGUI(font, this.mc.renderEngine, itemStack, guiLeft + 20 + x * 40, guiTop + 90 + y * 40);
			// With empty string, because damage value indicator struggles with the depth
			itemRenderer.renderItemOverlayIntoGUI(font, this.mc.renderEngine, itemStack, guiLeft + 20 + x * 40, guiTop + 90 + y * 40, "");
			
			if(guiLeft + 20 + x * 40 < mouseX && mouseX < guiLeft + 20 + x * 40 + 16
			 && guiTop + 90 + y * 40 < mouseY && mouseY < guiTop + 90 + y * 40 + 16) {
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				GL11.glColorMask(true, true, true, false);
				this.drawGradientRect(guiLeft + 20 + x * 40, guiTop + 90 + y * 40, guiLeft + 20 + x * 40 + 16, guiTop + 90 + y * 40 + 16, -2130706433, -2130706433);
				GL11.glColorMask(true, true, true, true);
				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
			}
			
			x++;
			if(x>2) {
				x=0;
				y++;
			}
		}
		GL11.glTranslated(0, 0, -100);
		super.drawScreen(mouseX, mouseY, par3);
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		int id = button.id;
		if(id == 0) {
			ItemStack[] stack = new ItemStack[9];
			int i=0;
			for(Canidates canidate:grid) {
				if(canidate == null) {
					i++;
					continue;
				}
				stack[i++] = canidate.order.get(canidate.pos).makeNormalStack();
			}
			NEISetCraftingRecipe packet = PacketHandler.getPacket(NEISetCraftingRecipe.class);
			MainProxy.sendPacketToServer(packet.setContent(stack).setPosX(tile.xCoord).setPosY(tile.yCoord).setPosZ(tile.zCoord));
			this.exitGui();
		} else if(id >= 10 && id < 30) {
			int slot = id % 10;
			boolean up = id < 20;
			Canidates canidate = list.get(slot);
			if(up) {
				canidate.pos++;
				if(canidate.pos >= canidate.order.size()) {
					canidate.pos = 0;
				}
			} else {
				canidate.pos--;
				if(canidate.pos < 0) {
					canidate.pos = canidate.order.size() - 1;
				}
			}
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		int x=0;
		int y=0;
		for(final Canidates canidate: list) {
			
			if(guiLeft + 20 + x * 40 < mouseX && mouseX < guiLeft + 20 + x * 40 + 16
			 && guiTop + 90 + y * 40 < mouseY && mouseY < guiTop + 90 + y * 40 + 16) {
				this.setSubGui(new SelectItemOutOfList(canidate.order, new IHandleItemChoise() {
					@Override
					public void handleItemChoise(int slot) {
						canidate.pos = slot;
					}
				}));
			}
			
			x++;
			if(x>2) {
				x=0;
				y++;
			}
		}
		super.mouseClicked(mouseX, mouseY, button);
	}
}
