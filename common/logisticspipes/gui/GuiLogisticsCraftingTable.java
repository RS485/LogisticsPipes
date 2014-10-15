package logisticspipes.gui;

import java.util.Arrays;

import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.block.CraftingCycleRecipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.CraftingRequirement;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.string.StringUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.opengl.GL11;

public class GuiLogisticsCraftingTable extends LogisticsBaseGuiScreen {
	
	public LogisticsCraftingTableTileEntity _crafter;

	private int fuzzyPanelSelection	= -1;
	private int fuzzyPanelHover     = -1;
	private int fuzzyPanelHoverTime = 0;
	
	private GuiButton[] sycleButtons = new GuiButton[2];
	
	public GuiLogisticsCraftingTable(EntityPlayer player, LogisticsCraftingTableTileEntity crafter) {
		super(176, 218, 0, 0);
		DummyContainer dummy = new DummyContainer(player.inventory, crafter.matrix);

		for(int x=0;x<3;x++) {
			for(int y=0;y<3;y++) {
				dummy.addDummySlot(y*3 + x, 35 + x*18, 10 + y*18);
			}
		}
		dummy.addUnmodifiableSlot(0, crafter.resultInv, 125, 28);
		for(int y=0;y<2;y++) {
			for(int x=0;x<9;x++) {
				dummy.addNormalSlot(y*9 + x, crafter.inv, 8 + x*18, 80 + y*18);
			}
		}
		dummy.addNormalSlotsForPlayerInventory(8, 135);
		this.inventorySlots = dummy;
		_crafter = crafter;
	}
	
	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		(sycleButtons[0] = addButton(new SmallGuiButton(0, guiLeft + 144, guiTop + 25, 15, 10, "/\\"))).visible = false;
		(sycleButtons[1] = addButton(new SmallGuiButton(1, guiLeft + 144, guiTop + 37, 15, 10, "\\/"))).visible = false;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float fA, int iA, int jA) {
		for(int i=0;i<sycleButtons.length;i++) {
			sycleButtons[i].visible = this._crafter.targetType != null;
		}
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		for(int x=0;x<3;x++) {
			for(int y=0;y<3;y++) {
				BasicGuiHelper.drawSlotBackground(mc, guiLeft + 34 + x*18, guiTop + 9 + y*18);
			}
		}
		BasicGuiHelper.drawSlotBackground(mc, guiLeft + 124, guiTop + 27);
		for(int x=0;x<9;x++) {
			for(int y=0;y<2;y++) {
				BasicGuiHelper.drawSlotBackground(mc, guiLeft + 7 + x*18, guiTop + 79 + y*18);
			}
		}
		BasicGuiHelper.drawPlayerInventoryBackground(mc, guiLeft + 8, guiTop + 135);
		
		ItemIdentifierStack[] items = new ItemIdentifierStack[9];
		for(int i=0;i<9;i++) {
			if(_crafter.matrix.getIDStackInSlot(i) != null) {
				items[i] = _crafter.matrix.getIDStackInSlot(i);
			}
		}

		// Draw this part without depth
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		BasicGuiHelper.renderItemIdentifierStackListIntoGui(Arrays.asList(items), null, 0, guiLeft + 8, guiTop + 80, 9, 9, 18, 18, mc, false, false);

		for (int a = 0; a < 9; a++) {
			drawRect(guiLeft + 8 + (a * 18), guiTop + 80, guiLeft + 24 + (a * 18), guiTop + 96, 0xc08b8b8b);
		}
		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		if(_crafter.isFuzzy()) {
			int mx = par1 - guiLeft;
			int my = par2 - guiTop;
			if((mx - 35) % 18 <= 16 && (mx - 35) % 18 >= 0) if((my - 10) % 18 <= 16 && (my - 10) % 18 >= 0) {
				int x_slot = (mx - 35) / 18;
				int y_slot = (my - 10) / 18;
				if(x_slot >= 0 && x_slot < 3 && y_slot >= 0 && y_slot < 3) {
					int newFuzzyPanelHover = y_slot * 3 + x_slot;
					if(fuzzyPanelHover != newFuzzyPanelHover) {
						fuzzyPanelHover = newFuzzyPanelHover;
						fuzzyPanelHoverTime = 0;
					}
					else
						fuzzyPanelHoverTime++;
				}
				else
					fuzzyPanelHover = -1;
			}
			else
				fuzzyPanelHover = -1;
			if(fuzzyPanelHover != -1 && fuzzyPanelHoverTime >= 60)
				fuzzyPanelSelection = fuzzyPanelHover;
		}
		
		if(_crafter.isFuzzy()) {
			for(int i = 0; i < 9; i++) {
				int x1 = 35 + i % 3 * 18;
				int y1 = 10  + i / 3 * 18;
				if(_crafter.fuzzyFlags[i].use_od) {
					drawRect(x1 + 8, y1 - 1, x1 + 17, y1, 0xFFFF4040);
					drawRect(x1 + 16, y1, x1 + 17, y1 + 8, 0xFFFF4040);
				}
				if(_crafter.fuzzyFlags[i].ignore_dmg) {
					drawRect(x1 - 1, y1 - 1, x1 + 8, y1, 0xFF40FF40);
					drawRect(x1 - 1, y1, x1, y1 + 8, 0xFF40FF40);
				}
				if(_crafter.fuzzyFlags[i].ignore_nbt) {
					drawRect(x1 - 1, y1 + 16, x1 + 8, y1 + 17, 0xFF4040FF);
					drawRect(x1 - 1, y1 + 8, x1, y1 + 17, 0xFF4040FF);
				}
				if(_crafter.fuzzyFlags[i].use_category) {
					drawRect(x1 + 8, y1 + 16, x1 + 17, y1 + 17, 0xFF7F7F40);
					drawRect(x1 + 16, y1 + 8, x1 + 17, y1 + 17, 0xFF7F7F40);
				}
				if(fuzzyPanelSelection == i) {
					drawRect(x1, y1, x1 + 16, y1 + 16, 0x7F000000);
				}
			}
		}
		
		if(fuzzyPanelSelection != -1) {
			int posX = -60;
			int posY = 0;
			BasicGuiHelper.drawGuiBackGround(mc, posX, posY, posX + 60, posY + 52, zLevel, true, true, true, true, true);
			CraftingRequirement flag = _crafter.fuzzyFlags[fuzzyPanelSelection];
			final String PREFIX = "gui.crafting.";
			mc.fontRenderer.drawString(StringUtil.translate(PREFIX + "OreDict"), posX + 4, posY + 4, (!flag.use_od ? 0x404040 : 0xFF4040));
			mc.fontRenderer.drawString(StringUtil.translate(PREFIX + "IgnDamage"), posX + 4, posY + 14, (!flag.ignore_dmg ? 0x404040 : 0x40FF40));
			mc.fontRenderer.drawString(StringUtil.translate(PREFIX + "IgnNBT"), posX + 4, posY + 26, (!flag.ignore_nbt ? 0x404040 : 0x4040FF));
			mc.fontRenderer.drawString(StringUtil.translate(PREFIX + "OrePrefix"), posX + 4, posY + 38, (!flag.use_category ? 0x404040 : 0x7F7F40));
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if(button.id == 0 || button.id == 1) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(CraftingCycleRecipe.class).setDown(button.id == 1).setTilePos(_crafter));
		}
	}

	private boolean isMouseInFuzzyPanel(int mx, int my) {
		if(fuzzyPanelSelection == -1) return false;
		int posX = -60;
		int posY = 0;
		return mx >= posX && my >= posY && mx <= posX + 60 && my <= posY + 52;
	}

	
	@Override
	protected void mouseMovedOrUp(int mouseX, int mouseY, int which) {
		if(isMouseInFuzzyPanel(mouseX - guiLeft, mouseY - guiTop)) return;
		super.mouseMovedOrUp(mouseX, mouseY, which);
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int par3) {
		if(isMouseInFuzzyPanel(mouseX - guiLeft, mouseY - guiTop)) {
			int posX = -60;
			int posY = 0;
			int sel = -1;
			if(mouseX - guiLeft >= posX + 4 && mouseX - guiLeft <= posX + 60 - 4) if(mouseY - guiTop >= posY + 4 && mouseY - guiTop <= posY + 52 - 4) sel = (mouseY - guiTop - posY - 4) / 11;
			_crafter.handleFuzzyFlagsChange(fuzzyPanelSelection, sel, null);
			return;
		}
		super.mouseClicked(mouseX, mouseY, par3);
	}
}
