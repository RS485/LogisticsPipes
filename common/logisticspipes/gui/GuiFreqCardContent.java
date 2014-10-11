package logisticspipes.gui;

import logisticspipes.LogisticsPipes;
import logisticspipes.interfaces.ISlotCheck;
import logisticspipes.items.LogisticsItemCard;
import logisticspipes.network.GuiIDs;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class GuiFreqCardContent extends LogisticsBaseGuiScreen {

	public GuiFreqCardContent(EntityPlayer player, IInventory card) {
		super(180, 130, 0, 0);
		DummyContainer dummy = new DummyContainer(player.inventory, card);
		dummy.addRestrictedSlot(0, card, 82, 15, new ISlotCheck() {
			@Override
			public boolean isStackAllowed(ItemStack itemStack) {
				if(itemStack == null) return false;
				if(itemStack.getItem() != LogisticsPipes.LogisticsItemCard) return false;
				if(itemStack.getItemDamage() != LogisticsItemCard.FREQ_CARD) return false;
				return true;
			}
		});
		dummy.addNormalSlotsForPlayerInventory(10, 45);
		this.inventorySlots = dummy;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		BasicGuiHelper.drawPlayerInventoryBackground(mc, guiLeft + 10,  guiTop + 45);
		BasicGuiHelper.drawSlotBackground(mc, guiLeft + 81, guiTop + 14);
	}

}
