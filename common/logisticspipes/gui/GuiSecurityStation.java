package logisticspipes.gui;

import logisticspipes.blocks.LogisticsSecurityTileEntity;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.KraphtBaseGuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;

public class GuiSecurityStation extends KraphtBaseGuiScreen {
	
	private final LogisticsSecurityTileEntity _tile;
	
	public GuiSecurityStation(LogisticsSecurityTileEntity tile, EntityPlayer player) {
		super(180, 300, 0, 0);
		DummyContainer dummy = new DummyContainer(player.inventory, tile.inv);
		dummy.addRestrictedSlot(0, tile.inv, 82, 181, -1);
		dummy.addNormalSlotsForPlayerInventory(10, 215);
		this.inventorySlots = dummy;
		_tile = tile;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		this.controlList.clear();
		this.controlList.add(new GuiButton(0, guiLeft + 10, guiTop + 179, 30, 20, "--"));
		this.controlList.add(new GuiButton(1, guiLeft + 45, guiTop + 179, 30, 20, "-"));
		this.controlList.add(new GuiButton(2, guiLeft + 105, guiTop + 179, 30, 20, "+"));
		this.controlList.add(new GuiButton(3, guiLeft + 140, guiTop + 179, 30, 20, "++"));
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if(button.id < 4) {
			MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.SECURITY_CARD, _tile.xCoord, _tile.yCoord, _tile.zCoord, button.id).getPacket());
		} else {
			super.actionPerformed(button);
		}
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Security_Station_ID;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		BasicGuiHelper.drawPlayerInventoryBackground(mc, guiLeft + 10, guiTop + 215);
		BasicGuiHelper.drawSlotBackground(mc, guiLeft + 81, guiTop + 180);
	}
}
