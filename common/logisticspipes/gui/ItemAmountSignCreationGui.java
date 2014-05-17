package logisticspipes.gui;

import logisticspipes.network.GuiIDs;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.pipes.signs.ItemAmountPipeSign;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.KraphtBaseGuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.ForgeDirection;

public class ItemAmountSignCreationGui extends KraphtBaseGuiScreen {
	
	public ItemAmountSignCreationGui(EntityPlayer player, CoreRoutedPipe pipe, ForgeDirection dir) {
		super(180, 125, 0, 0);
		ItemAmountPipeSign sign = ((ItemAmountPipeSign)pipe.getPipeSign(dir));
		DummyContainer dummy = new DummyContainer(player.inventory, sign.itemTypeInv);
		dummy.addDummySlot(0, 10, 13);
		dummy.addNormalSlotsForPlayerInventory(10, 40);
		this.inventorySlots = dummy;
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Item_Amount_Sign;
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		BasicGuiHelper.drawPlayerInventoryBackground(mc, guiLeft + 10, guiTop + 40);
		BasicGuiHelper.drawSlotBackground(mc, guiLeft + 9, guiTop + 12);
	}
}
