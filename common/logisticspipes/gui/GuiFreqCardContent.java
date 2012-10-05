package logisticspipes.gui;

import logisticspipes.LogisticsPipes;
import logisticspipes.network.GuiIDs;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.KraphtBaseGuiScreen;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;

public class GuiFreqCardContent extends KraphtBaseGuiScreen {

	public GuiFreqCardContent(EntityPlayer player, IInventory card) {
		super(180, 130, 0, 0);
		DummyContainer dummy = new DummyContainer(player.inventory, card);
		dummy.addRestrictedSlot(0, card, 82, 15, LogisticsPipes.LogisticsItemCard.shiftedIndex);
		dummy.addNormalSlotsForPlayerInventory(10, 45);
		this.inventorySlots = dummy;
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Freq_Card_ID;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		BasicGuiHelper.drawPlayerInventoryBackground(mc, guiLeft + 10,  guiTop + 45);
		BasicGuiHelper.drawSlotBackground(mc, guiLeft + 81, guiTop + 14);
	}

}
