package logisticspipes.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;

public class GuiFluidTerminus extends LogisticsBaseGuiScreen {

    public GuiFluidTerminus(EntityPlayer player, IInventory inventory) {
        super(176, 166, 0, 0);
        DummyContainer dummy = new DummyContainer(player.inventory, inventory);
        for (int i = 0; i < 9; i++) {
            dummy.addFluidSlot(i, inventory, 8 + i * 18, 13);
        }
        dummy.addNormalSlotsForPlayerInventory(10, 45);
        inventorySlots = dummy;
    }

    @Override
    public void initGui() {
        super.initGui();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
        GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
        GuiGraphics.drawPlayerInventoryBackground(mc, guiLeft + 10, guiTop + 45);
        for (int i = 0; i < 9; i++) {
            GuiGraphics.drawSlotBackground(mc, guiLeft + 7 + i * 18, guiTop + 12);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int par1, int par2) {
        super.drawGuiContainerForegroundLayer(par1, par2);
    }
}
