package logisticspipes.gui;

import java.util.Arrays;

import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.network.GuiIDs;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.KraphtBaseGuiScreen;
import net.minecraft.entity.player.EntityPlayer;

import org.lwjgl.opengl.GL11;

public class GuiLogisticsCraftingTable extends KraphtBaseGuiScreen {
	
	public LogisticsCraftingTableTileEntity _crafter;
	
	public GuiLogisticsCraftingTable(EntityPlayer player, LogisticsCraftingTableTileEntity crafter) {
		super(176, 218, 0, 0);
		DummyContainer dummy = new DummyContainer(player.inventory, crafter.matrix);

		for(int x=0;x<3;x++) {
			for(int y=0;y<3;y++) {
				dummy.addDummySlot(y*3 + x, 35 + x*18, 10 + y*18);
			}
		}
		dummy.addUnmodifiableSlot(0, crafter.resultInv, 125, 28);
		for(int x=0;x<9;x++) {
			for(int y=0;y<2;y++) {
				dummy.addNormalSlot(y*9 + x, crafter.inv, 8 + x*18, 80 + y*18);
			}
		}
		dummy.addNormalSlotsForPlayerInventory(8, 135);
		this.inventorySlots = dummy;
		_crafter = crafter;
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Auto_Crafting_ID;
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float fA, int iA, int jA) {
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
		BasicGuiHelper.renderItemIdentifierStackListIntoGui(Arrays.asList(items), null, 0, guiLeft + 8, guiTop + 80, 9, 9, 18, 18, mc, false, false);
		
		GL11.glDisable(2929 /*GL_DEPTH_TEST*/);
		for(int a=0;a<9;a++) {
			drawRect(guiLeft + 8 + (a * 18), guiTop + 80, guiLeft + 24 + (a * 18), guiTop + 96, 0xc08b8b8b);
		}
		GL11.glEnable(2929 /*GL_DEPTH_TEST*/);
	}
}
