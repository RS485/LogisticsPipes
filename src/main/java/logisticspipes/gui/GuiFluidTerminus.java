package logisticspipes.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

import logisticspipes.network.packets.pipe.PipePropertiesUpdate;
import logisticspipes.pipes.PipeFluidTerminus;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import logisticspipes.utils.item.ItemIdentifierInventory;
import network.rs485.logisticspipes.inventory.IItemIdentifierInventory;
import network.rs485.logisticspipes.property.InventoryProperty;
import network.rs485.logisticspipes.property.PropertyLayer;

public class GuiFluidTerminus extends LogisticsBaseGuiScreen {

	private final PropertyLayer propertyLayer;
	private final BlockPos pipePosition;
	private final PropertyLayer.PropertyOverlay<ItemIdentifierInventory, InventoryProperty> sinkInventoryOverlay;

	public GuiFluidTerminus(EntityPlayer player, PipeFluidTerminus pipe) {
		super(null);

		pipePosition = pipe.getPos();
		propertyLayer = new PropertyLayer(pipe.getProperties());
		sinkInventoryOverlay = propertyLayer.overlay(pipe.getSinkInv());

		DummyContainer dummy = new DummyContainer(player.inventory, propertyLayer.writeProp(pipe.getSinkInv()));
		// Pipe slots
		for (int pipeSlot = 0; pipeSlot < pipe.getSinkInv().getSizeInventory(); ++pipeSlot) {
			dummy.addFluidSlot(pipeSlot, 10 + pipeSlot * 18, 19);
		}
		dummy.addNormalSlotsForPlayerInventory(10, 45);

		inventorySlots = dummy;
		xSize = 180;
		ySize = 130;
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		propertyLayer.unregister();
		if (this.mc.player != null && !propertyLayer.getProperties().isEmpty()) {
			// send update to server, when there are changed properties
			MainProxy.sendPacketToServer(
					PipePropertiesUpdate.fromPropertyHolder(propertyLayer).setBlockPos(pipePosition));
		}
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
			GuiGraphics.drawSlotBackground(mc, guiLeft + 9 + i * 18, guiTop + 18);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		mc.fontRenderer.drawString(sinkInventoryOverlay.read(IItemIdentifierInventory::getName), 10, 8, 0x404040);
	}
}
