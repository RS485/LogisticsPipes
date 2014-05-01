package logisticspipes.gui.popup;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.routing.LinkedLogisticsOrderList;
import logisticspipes.routing.LogisticsOrder;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.SubGuiScreen;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.string.ChatColor;
import logisticspipes.utils.string.StringUtil;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiSmallButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class RequestMonitorPopup extends SubGuiScreen {
	
	private enum ZOOM_LEVEL {
		NORMAL(1, 165, 224, 1, 0, 0, 0),
		LEVEL_1(0.5F, 330, 465, 1, 50, -200, 100),
		LEVEL_2(0.25F, 660, 950, 2, 100, -400, -100);
		private ZOOM_LEVEL(float zoom, int bottom, int right, int line, int moveY, int maxX, int maxY) {
			this.zoom = zoom;
			this.bottomRenderBorder = bottom;
			this.rightRenderBorder = right;
			this.line = line;
			this.moveY = moveY;
			this.maxX = maxX;
			this.maxY = maxY;
		}
		final float zoom;
		final int bottomRenderBorder;
		final int rightRenderBorder;
		final int line;
		final int moveY;
		final int maxX;
		final int maxY;
		ZOOM_LEVEL next() {
			int id = this.ordinal();
			if(id + 1 >= ZOOM_LEVEL.values().length) {
				return this;
			} else {
				return ZOOM_LEVEL.values()[id + 1];
			}
		}
		ZOOM_LEVEL prev() {
			int id = this.ordinal();
			if(id - 1 < 0) {
				return this;
			} else {
				return ZOOM_LEVEL.values()[id - 1];
			}
		}
	}
	
	private static final ResourceLocation	achievementTextures	= new ResourceLocation("textures/gui/achievement/achievement_background.png");
	
	private final PipeBlockRequestTable		_table;
	private final int						orderId;
	private final RenderItem				renderitem			= new RenderItem();
	
	private int								isMouseButtonDown;
	private int								mouseX;
	private int								mouseY;
	private double							guiMapX;
	private double							guiMapY;
	private int								minY				= -230;
	private int								maxY				= 0;
	private int								minX				= -800;
	private int								maxX				= 800;
	private ZOOM_LEVEL						zoom				= ZOOM_LEVEL.NORMAL;

	private Object[]	tooltip = null;
	
	public RequestMonitorPopup(PipeBlockRequestTable table, int orderId) {
		super(256, 202, 0, 0);
		this._table = table;
		this.orderId = orderId;
		this.guiMapY = -200;
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void initGui() {
		super.initGui();
		buttonList.clear();
		this.buttonList.add(new GuiSmallButton(0, this.width / 2 - 40, this.height / 2 + 74, 80, 20, "Close"));
	}
	
	@Override
	protected void actionPerformed(GuiButton button) {
		if(button.id == 0) {
			this.exitGui();
		}
	}
	
	@Override
	public void drawScreen(int par1, int par2, float par3) {
		if(!_table.watchedRequests.containsKey(orderId)) {
			this.exitGui();
			return;
		}
		if(Mouse.isButtonDown(0)) {
			int k = (this.width - this.xSize) / 2;
			int l = (this.height - this.ySize) / 2;
			int i1 = k + 8;
			int j1 = l + 17;
			
			if((this.isMouseButtonDown == 0 || this.isMouseButtonDown == 1) && par1 >= i1 && par1 < i1 + 224 && par2 >= j1 && par2 < j1 + 155) {
				if(this.isMouseButtonDown == 0) {
					this.isMouseButtonDown = 1;
				} else {
					this.guiMapX -= (double)(par1 - this.mouseX) * 1/zoom.zoom;
					this.guiMapY -= (double)(par2 - this.mouseY) * 1/zoom.zoom;
				}
				
				this.mouseX = par1;
				this.mouseY = par2;
			}
			
		} else {
			this.isMouseButtonDown = 0;
		}

		if(guiMapY < minY) {
			guiMapY = minY;
		}
		if(guiMapY > maxY) {
			guiMapY = maxY;
		}
		if(guiMapX > maxX) {
			guiMapX = maxX;
		}
		if(guiMapX < minX) {
			guiMapX = minX;
		}
		
		int dWheel = Mouse.getDWheel();
		if(dWheel < 0) {
			zoom = zoom.next();
		} else if(dWheel > 0) {
			zoom = zoom.prev();
		}
		this.createBoundary();
		this.drawTransparentBack();
		this.drawMap(par1, par2);
		super.drawScreen(par1, par2, par3);
		if(tooltip != null) {
			BasicGuiHelper.displayItemToolTip(tooltip, this.zLevel, guiLeft, guiTop, false, true);
		}
	}
	
	private void createBoundary() {
		int size = _table.watchedRequests.get(this.orderId).getValue2().getTreeRootSize();
		minX = -size * (40 / 2) + (int)(75 * (zoom.zoom));
		maxX = -minX + zoom.maxX;
		maxY = -100;
		findLowest(_table.watchedRequests.get(this.orderId).getValue2(), -200);
	}
	
	private void drawTransparentBack() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		this.drawGradientRect(0, 0, this.width, this.height, 0, 0);
	}
	
	private void findLowest(LinkedLogisticsOrderList list, int lowerLimit) {
		lowerLimit += 48;
		for(LinkedLogisticsOrderList sub: list.getSubOrders()) {
			findLowest(sub, lowerLimit);
		}
		if(maxY < (lowerLimit + 10) * zoom.zoom) {
			maxY = (int) ((lowerLimit + 10) * zoom.zoom) + zoom.maxY;
		}
	}
	
	private void drawMap(int par1, int par2) {
		tooltip = null;
		int mapX = MathHelper.floor_double(this.guiMapX);
		int mapY = MathHelper.floor_double(this.guiMapY - zoom.moveY);
		int leftSide = ((this.width - this.xSize) / 2);
		int topSide = ((this.height - this.ySize) / 2);
		
		guiTop *= 1/zoom.zoom;
		guiLeft *= 1/zoom.zoom;
		xSize *= 1/zoom.zoom;
		ySize *= 1/zoom.zoom;
		leftSide *= 1/zoom.zoom;
		topSide *= 1/zoom.zoom;
		par1 *= 1/zoom.zoom;
		par2 *= 1/zoom.zoom;
		
		int innerLeftSide = leftSide + 16;
		int innerTopSide = topSide + 17;
		this.zLevel = 0.0F;
		
		GL11.glDepthFunc(GL11.GL_GEQUAL);
		GL11.glPushMatrix();
		GL11.glScalef(zoom.zoom, zoom.zoom, 1);
		GL11.glTranslatef(0.0F, 0.0F, -100.0F);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		
		int moveBackgroundX = (mapX - minX) % 16;
		int moveBackgroundY = (mapY - minY) % 16;
		GL11.glColor4f(0.7F, 0.7F, 0.7F, 1.0F);
		for(int yVar = 0; yVar * 16 - moveBackgroundY < zoom.bottomRenderBorder; yVar++) {
			for(int xVar = 0; xVar * 16 - moveBackgroundX < zoom.rightRenderBorder; xVar++) {
				Icon icon = Block.stone.getIcon(0, 0);
				this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
				this.drawTexturedModelRectFromIcon(innerLeftSide + xVar * 16 - moveBackgroundX, innerTopSide + yVar * 16 - moveBackgroundY, icon, 16, 16);
			}
		}
		
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		renderLinkedOrderList(_table.watchedRequests.get(this.orderId).getValue2(), innerLeftSide - mapX + 102, innerTopSide - mapY - 180, false, par1, par2);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
		
		RenderHelper.enableGUIStandardItemLighting();
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glColor4f(0.7F, 0.7F, 0.7F, 1.0F);
		this.mc.getTextureManager().bindTexture(achievementTextures);
		this.drawTexturedModalRect(innerLeftSide - mapX + 97, innerTopSide - mapY - 200, 0, 202, 26, 26);
		String s = Integer.toString(orderId);
		fontRenderer.drawStringWithShadow(s, innerLeftSide - mapX + 111 - fontRenderer.getStringWidth(s) / 2, innerTopSide - mapY - 190, 16777215);
		renderLinkedOrderList(_table.watchedRequests.get(this.orderId).getValue2(), innerLeftSide - mapX + 102, innerTopSide - mapY - 180, true, par1, par2);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_BLEND);

		guiTop *= zoom.zoom;
		guiLeft *= zoom.zoom;
		xSize *= zoom.zoom;
		ySize *= zoom.zoom;
		leftSide *= zoom.zoom;
		topSide *= zoom.zoom;
		
		GL11.glScalef(1/zoom.zoom, 1/zoom.zoom, 1);
		
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(achievementTextures);
		this.drawTexturedModalRect(leftSide, topSide, 0, 0, this.xSize, this.ySize);
		GL11.glPopMatrix();
		this.zLevel = 0.0F;
		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_LIGHTING);
		RenderHelper.disableStandardItemLighting();
	}
	
	private void renderLinkedOrderList(LinkedLogisticsOrderList list, int xPos, int yPos, boolean renderItems, int par1, int par2) {
		int size = list.size();
		int startLeft = -(size - 1) * (30 / 2) + xPos;
		yPos += 13;
		if(renderItems) {
			for(int i = 0; i < list.size(); i++) {
				if(list.get(i).isInProgress()) {
					GL11.glColor4f(0.1F, 0.9F, 0.1F, 1.0F);
				} else {
					GL11.glColor4f(0.7F, 0.7F, 0.7F, 1.0F);
				}
				GL11.glEnable(GL11.GL_LIGHTING);
				this.mc.getTextureManager().bindTexture(achievementTextures);
				this.drawTexturedModalRect(startLeft - 5, yPos - 5, 0, 202, 26, 26);
				GL11.glColor4f(0.7F, 0.7F, 0.7F, 1.0F);
				renderItemAt(list.get(i).getItem(), startLeft, yPos);
				if(startLeft - 10 < par1 && par1 < startLeft + 20 && yPos - 6 < par2 && par2 < yPos + 20) {
					if(guiLeft < par1 && par1 < guiLeft + xSize - 16 && guiTop < par2 && par2 < guiTop + ySize - 16) {
						LogisticsOrder order = list.get(i);
						List<String> tooltipList = new ArrayList<String>();
						tooltipList.add(ChatColor.BLUE + "Request Type: " + ChatColor.YELLOW + order.getType().name());
						tooltipList.add(ChatColor.BLUE + "Send to Router ID: " + ChatColor.YELLOW + order.getRouterId());
						tooltip = new Object[]{(int) (par1 * zoom.zoom - 10), (int) (par2 * zoom.zoom), order.getItem().makeNormalStack(), true, tooltipList};
					}
				}
				startLeft += 30;
			}
			startLeft = xPos + 20 - list.getSubTreeRootSize() * (40 / 2);
			if(!list.getSubOrders().isEmpty()) {
				for(int i = 0; i < list.getSubOrders().size(); i++) {
					startLeft += list.getSubOrders().get(i).getTreeRootSize() * (40 / 2);
					renderLinkedOrderList(list.getSubOrders().get(i), startLeft - 20, yPos + 48, true, par1, par2);
					startLeft += list.getSubOrders().get(i).getTreeRootSize() * (40 / 2);
				}
			}
		} else {
			int left = startLeft;
			for(int i = 0; i < list.size(); i++) {
				this.drawVerticalLine(startLeft + 8, yPos - 13, yPos - 3, 0xff00ff00);
				if(!list.getSubOrders().isEmpty()) {
					this.drawVerticalLine(startLeft + 8, yPos + 18, yPos + 28, 0xff00ff00);
				}
				startLeft += 30;
			}
			this.drawHorizontalLine(left + 8, startLeft - 22, yPos - 13, 0xff00ff00);
			if(!list.getSubOrders().isEmpty()) {
				this.drawHorizontalLine(left + 8, startLeft - 22, yPos + 28, 0xff00ff00);
				startLeft -= 30;
				this.drawVerticalLine(left + ((startLeft - left) / 2) + 8, yPos + 28, yPos + 38, 0xff00ff00);
				startLeft = xPos + 20 - list.getSubTreeRootSize() * (40 / 2);
				left = startLeft;
				for(int i = 0; i < list.getSubOrders().size(); i++) {
					startLeft += list.getSubOrders().get(i).getTreeRootSize() * (40 / 2);
					this.drawVerticalLine(startLeft - 12, yPos + 38, yPos + 48, 0xff00ff00);
					renderLinkedOrderList(list.getSubOrders().get(i), startLeft - 20, yPos + 48, false, par1, par2);
					startLeft += list.getSubOrders().get(i).getTreeRootSize() * (40 / 2);
				}
				if(!list.getSubOrders().isEmpty()) {
					left += list.getSubOrders().get(0).getTreeRootSize() * (40 / 2);
					startLeft -= list.getSubOrders().get(list.getSubOrders().size() - 1).getTreeRootSize() * (40 / 2);
				}
				this.drawHorizontalLine(left - 12, startLeft - 12, yPos + 38, 0xff00ff00);
			}
		}
	}
	
	private void renderItemAt(ItemIdentifierStack item, int x, int y) {
		renderitem.renderItemAndEffectIntoGUI(this.mc.fontRenderer, this.mc.getTextureManager(), item.makeNormalStack(), x, y);
		if(guiLeft < x && x < guiLeft + xSize - 16 && guiTop < y && y < guiTop + ySize - 16) {
			renderitem.renderItemOverlayIntoGUI(fontRenderer, this.mc.renderEngine, item.makeNormalStack(), x, y, "");
			String s = StringUtil.getFormatedStackSize(item.getStackSize());
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			renderitem.zLevel = 0.0F;
			// Draw number
			fontRenderer.drawStringWithShadow(s, x + 17 - fontRenderer.getStringWidth(s), y + 9, 16777215);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
		}
	}
	
	protected void drawHorizontalLine(int par1, int par2, int par3, int par4) {
		if(par2 < par1) {
			int i1 = par1;
			par1 = par2;
			par2 = i1;
		}
		int line = zoom.line;
		drawRect(par1, par3, par2 + 1, par3 + line, par4);
	}
	
	protected void drawVerticalLine(int par1, int par2, int par3, int par4) {
		if(par3 < par2) {
			int i1 = par2;
			par2 = par3;
			par3 = i1;
		}
		int line = zoom.line;
		drawRect(par1, par2 + 1, par1 + line, par3, par4);
	}
}
