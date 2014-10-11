package logisticspipes.gui.popup;

import java.util.List;

import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.gui.SubGuiScreen;
import logisticspipes.utils.item.ItemIdentifierStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
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
	
	public SelectItemOutOfList(List<ItemIdentifierStack> canidate, IHandleItemChoise handler) {
		super(152, 200, 0, 0);
		this.canidate = canidate;
		this.handler = handler;
		maxPage = (int) Math.round((((double)canidate.size()) / 80.0D) + 0.5D) - (canidate.size() % 80 == 0 ? 1:0);
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
	public void drawScreen(int mouseX, int mouseY, float par3) {
		GL11.glEnable(GL11.GL_BLEND);
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		fontRendererObj.renderString("Select Item (TODO)", guiLeft + 10, guiTop + 6, 0x404040, false); //TODO
		String pageString = Integer.toString(page + 1) + "/" + Integer.toString(maxPage);
		fontRendererObj.renderString(pageString, guiLeft + 128 - (fontRendererObj.getStringWidth(pageString) / 2), guiTop + 6, 0x404040, false);
		GL11.glTranslated(0, 0, 100);
		int x = 0;
		int y = - page * 10;
		for(ItemIdentifierStack stack: canidate) {
			if(y >= 0) {
				ItemStack itemStack = stack.makeNormalStack();
				FontRenderer font = itemStack.getItem().getFontRenderer(itemStack);
				if (font == null) {
					font = fontRendererObj;
				}
				
				RenderHelper.enableGUIStandardItemLighting();
				BasicGuiHelper.drawSlotBackground(mc, guiLeft + 4 + x * 18, guiTop + 16 + y * 18);
				itemRenderer.renderItemAndEffectIntoGUI(font, this.mc.renderEngine, itemStack, guiLeft + 5 + x * 18, guiTop + 17 + y * 18);
				// With empty string, because damage value indicator struggles with the depth
				itemRenderer.renderItemOverlayIntoGUI(font, this.mc.renderEngine, itemStack, guiLeft + 5 + x * 18, guiTop + 17 + y * 18, "");
				
				if(guiLeft + 5 + x * 18 < mouseX && mouseX < guiLeft + 5 + x * 18 + 16
				&& guiTop + 17 + y * 18 < mouseY && mouseY < guiTop + 17 + y * 18 + 16) {
					GL11.glDisable(GL11.GL_LIGHTING);
					GL11.glDisable(GL11.GL_DEPTH_TEST);
					GL11.glColorMask(true, true, true, false);
					this.drawGradientRect(guiLeft + 5 + x * 18, guiTop + 17 + y * 18, guiLeft + 5 + x * 18 + 16, guiTop + 17 + y * 18 + 16, -2130706433, -2130706433);
					GL11.glColorMask(true, true, true, true);
					GL11.glEnable(GL11.GL_LIGHTING);
					GL11.glEnable(GL11.GL_DEPTH_TEST);
				}
			}
			
			x++;
			if(x>7) {
				x=0;
				y++;
			}
			if(y > 9) break;
		}
		super.drawScreen(mouseX, mouseY, par3);
		GL11.glTranslated(0, 0, -100);
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		int id = button.id;
		if(id == 1) {
			page++;
			if(page >= maxPage) {
				page = 0;
			}
		} else if(id == 0) {
			page--;
			if(page < 0) {
				page = maxPage - 1;
			}
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) {
		int x=0;
		int y=0;
		int count=0;
		for(ItemIdentifierStack stack: canidate) {
			if(y >= 0) {
				if(guiLeft + 5 + x * 18 < mouseX && mouseX < guiLeft + 5 + x * 18 + 16
				&& guiTop + 17 + y * 18 < mouseY && mouseY < guiTop + 17 + y * 18 + 16) {
					handler.handleItemChoise(count);
					this.exitGui();
				}
			}

			x++;
			if(x>7) {
				x=0;
				y++;
			}
			count++;
			if(y > 9) break;
		}
		super.mouseClicked(mouseX, mouseY, button);
	}
}
