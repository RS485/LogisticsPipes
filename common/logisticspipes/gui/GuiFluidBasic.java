package logisticspipes.gui;

import logisticspipes.network.GuiIDs;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.KraphtBaseGuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

public class GuiFluidBasic extends KraphtBaseGuiScreen {

	public GuiFluidBasic(EntityPlayer player, IInventory inventory) {
		super(180, 130, 0, 0);
		DummyContainer dummy = new DummyContainer(player.inventory, inventory);
		dummy.addFluidSlot(0, inventory, 28, 13);
		dummy.addNormalSlotsForPlayerInventory(10, 45);
		this.inventorySlots = dummy;
	}
	
	@Override
	public void initGui() {
		super.initGui();
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Fluid_Basic_ID;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		BasicGuiHelper.drawPlayerInventoryBackground(mc, guiLeft + 10, guiTop + 45);
		BasicGuiHelper.drawSlotBackground(mc, guiLeft + 27, guiTop + 12);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		if(inventorySlots.getSlot(0).getStack() == null) {
			mc.fontRenderer.drawString("Empty", 50, 18, 0x404040);
		} else {
			mc.fontRenderer.drawString(ItemIdentifier.get(inventorySlots.getSlot(0).getStack()).getFriendlyName(), 50, 18, 0x404040);
		}
	}
}
