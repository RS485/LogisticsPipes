package logisticspipes.gui.popup;

import java.util.List;

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

import org.lwjgl.opengl.GL11;

public class SelectItemOutOfList extends SubGuiScreen {

	public static interface IHandleItemChoise {

		public void handleItemChoise(int slot);
	}

	private final List<ItemIdentifierStack> canidate;
	private final IHandleItemChoise handler;
	private final RenderItem itemRenderer = new RenderItem();
	private int page = 0;
	private final int maxPage;
	private Object[] tooltip = null;

	public SelectItemOutOfList(List<ItemIdentifierStack> canidate, IHandleItemChoise handler) {
		super(152, 200, 0, 0);
		this.canidate = canidate;
		this.handler = handler;
		maxPage = (int) Math.round(((canidate.size()) / 80.0D) + 0.5D) - (canidate.size() % 80 == 0 ? 1 : 0);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initGui() {
		super.initGui();
		buttonList.clear();
		buttonList.add(new SmallGuiButton(0, guiLeft + 108, guiTop + 5, 10, 10, "<"));
		buttonList.add(new SmallGuiButton(1, guiLeft + 138, guiTop + 5, 10, 10, ">"));
	}

	@Override
	protected void renderToolTips(int mouseX, int mouseY, float par3) {
		if (!hasSubGui()) {
			GuiGraphics.displayItemToolTip(tooltip, this, zLevel, guiLeft, guiTop);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		tooltip = null;
		int x = 0;
		int y = -page * 10;
		for (ItemIdentifierStack stack : canidate) {
			if (y >= 0) {
				ItemStack itemStack = stack.makeNormalStack();
				FontRenderer font = itemStack.getItem().getFontRenderer(itemStack);
				if (font == null) {
					font = fontRendererObj;
				}

				itemRenderer.renderItemAndEffectIntoGUI(font, mc.renderEngine, itemStack, guiLeft + 5 + x * 18, guiTop + 17 + y * 18);
				// With empty string, because damage value indicator struggles with the depth
				itemRenderer.renderItemOverlayIntoGUI(font, mc.renderEngine, itemStack, guiLeft + 5 + x * 18, guiTop + 17 + y * 18, "");

				if (guiLeft + 5 + x * 18 < mouseX && mouseX < guiLeft + 5 + x * 18 + 16 && guiTop + 17 + y * 18 < mouseY && mouseY < guiTop + 17 + y * 18 + 16 && !hasSubGui()) {
					GL11.glDisable(GL11.GL_LIGHTING);
					GL11.glDisable(GL11.GL_DEPTH_TEST);
					GL11.glColorMask(true, true, true, false);
					SimpleGraphics.drawGradientRect(guiLeft + 5 + x * 18, guiTop + 17 + y * 18, guiLeft + 5 + x * 18 + 16, guiTop + 17 + y * 18 + 16, Color.WHITE_50, Color.WHITE_50, 0.0);
					GL11.glColorMask(true, true, true, true);
					GL11.glEnable(GL11.GL_LIGHTING);
					GL11.glEnable(GL11.GL_DEPTH_TEST);
					tooltip = new Object[] { guiLeft + mouseX, guiTop + mouseY, itemStack };
				}
			}

			x++;
			if (x > 7) {
				x = 0;
				y++;
			}
			if (y > 9) {
				break;
			}
		}
	}

	@Override
	protected void renderGuiBackground(int par1, int par2) {
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		fontRendererObj.renderString(StringUtils.translate("misc.selectType"), guiLeft + 10, guiTop + 6, 0x404040, false); //TODO
		String pageString = Integer.toString(page + 1) + "/" + Integer.toString(maxPage);
		fontRendererObj.renderString(pageString, guiLeft + 128 - (fontRendererObj.getStringWidth(pageString) / 2), guiTop + 6, 0x404040, false);
		int x = 0;
		int y = -page * 10;
		for (ItemIdentifierStack stack : canidate) {
			if (y >= 0) {
				GuiGraphics.drawSlotBackground(mc, guiLeft + 4 + x * 18, guiTop + 16 + y * 18);
			}
			x++;
			if (x > 7) {
				x = 0;
				y++;
			}
			if (y > 9) {
				break;
			}
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		int id = button.id;
		if (id == 1) {
			page++;
			if (page >= maxPage) {
				page = 0;
			}
		} else if (id == 0) {
			page--;
			if (page < 0) {
				page = maxPage - 1;
			}
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		int x = 0;
		int y = -page * 10;
		int count = 0;
		for (ItemIdentifierStack stack : canidate) {
			if (y >= 0) {
				if (guiLeft + 5 + x * 18 < mouseX && mouseX < guiLeft + 5 + x * 18 + 16 && guiTop + 17 + y * 18 < mouseY && mouseY < guiTop + 17 + y * 18 + 16) {
					handler.handleItemChoise(count);
					exitGui();
				}
			}

			x++;
			if (x > 7) {
				x = 0;
				y++;
			}
			count++;
			if (y > 9) {
				break;
			}
		}
		super.mouseClicked(mouseX, mouseY, button);
	}
}
