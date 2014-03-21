package logisticspipes.gui;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.IGuiOpenControler;
import logisticspipes.interfaces.ISlotCheck;
import logisticspipes.network.GuiIDs;
import logisticspipes.utils.CardManagmentInventory;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.KraphtBaseGuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class GuiCardManager extends KraphtBaseGuiScreen {

	public GuiCardManager(EntityPlayer player) {
		super(180, 180, 0, 0);
		final CardManagmentInventory Cinv = new CardManagmentInventory();
		DummyContainer dummy = new DummyContainer(player, Cinv, new IGuiOpenControler() {
			@Override public void guiOpenedByPlayer(EntityPlayer player) {}
			@Override
			public void guiClosedByPlayer(EntityPlayer player) {
				Cinv.close(player, (int)player.posX, (int)player.posY, (int)player.posZ);
			}
		});
		dummy.addRestrictedSlot(0, Cinv, 21, 21, LogisticsPipes.ModuleItem.itemID);
		dummy.addRestrictedSlot(1, Cinv, 61, 21, LogisticsPipes.ModuleItem.itemID);
		dummy.addRestrictedSlot(2, Cinv, 41, 58, new ISlotCheck() {
			@Override public boolean isStackAllowed(ItemStack itemStack) {return false;}
		});
		dummy.addRestrictedSlot(3, Cinv, 121, 39, LogisticsPipes.LogisticsItemCard.itemID);
		for(int i=4;i<7;i++) {
			dummy.addColorSlot(i, Cinv, 101, 21 + (i - 4) * 18);
		}
		for(int i=7;i<10;i++) {
			dummy.addColorSlot(i, Cinv, 141, 21 + (i - 7) * 18);
		}
		dummy.addNormalSlotsForPlayerInventory(10, 95);
		this.inventorySlots = dummy;
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Item_Manager;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int j, int k) {
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		BasicGuiHelper.drawPlayerInventoryBackground(mc, guiLeft + 10, bottom - 85);
		BasicGuiHelper.drawSlotBackground(mc, guiLeft + 20, guiTop + 20);
		BasicGuiHelper.drawSlotBackground(mc, guiLeft + 60, guiTop + 20);
		BasicGuiHelper.drawSlotBackground(mc, guiLeft + 40, guiTop + 57);
		BasicGuiHelper.drawSlotBackground(mc, guiLeft + 120, guiTop + 38);
		drawRect(guiLeft + 38, guiTop + 27, guiLeft + 60, guiTop + 31, Colors.DarkGrey);
		drawRect(guiLeft + 47, guiTop + 31, guiLeft + 51, guiTop + 57, Colors.DarkGrey);
		for(int i=0;i<3;i++) {
			BasicGuiHelper.drawSlotBackground(mc, guiLeft + 100, guiTop + 20 + i * 18);
		}
		for(int i=0;i<3;i++) {
			BasicGuiHelper.drawSlotBackground(mc, guiLeft + 140, guiTop + 20 + i* 18);
		}
	}
}
