package logisticspipes.gui;

import logisticspipes.gui.modules.GuiWithPreviousGuiContainer;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketSplitSendingSettings;
import logisticspipes.pipes.PipeLogisticsChassi;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.SmallGuiButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import buildcraft.transport.Pipe;

public class GuiChassiSplitSending extends GuiWithPreviousGuiContainer {
	PipeLogisticsChassi _chassi;
	
	public GuiChassiSplitSending(EntityPlayer player, Pipe pipe, GuiScreen previousGui) {
		super(new DummyContainer(player.inventory,null), pipe, previousGui);
		_chassi = (PipeLogisticsChassi) pipe;
		xSize = 175;
		ySize = 125;
	}
	

	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		buttonList.add(new SmallGuiButton(1, guiLeft + 52, guiTop + 20, 10, 10, "+"));
		buttonList.add(new SmallGuiButton(2, guiLeft + 52, guiTop + 50, 10, 10, "-"));

		buttonList.add(new SmallGuiButton(3, guiLeft + 112, guiTop + 20, 10, 10, "+"));
		buttonList.add(new SmallGuiButton(4, guiLeft + 112, guiTop + 50, 10, 10, "-"));
		
		buttonList.add(new GuiButton(5, (width/2)-25, guiTop + 80, 50, 20, "Save"));
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		switch(guibutton.id)
		{
		case(1): 
			_chassi.setSplitGroup(_chassi.getSplitGroup()+1);
			break;
		case(2):
			if (_chassi.getSplitGroup()-1 < 0) break;
			_chassi.setSplitGroup(_chassi.getSplitGroup()-1);
			break;
		case(3):
			_chassi.setSplitAmount(_chassi.getSplitAmount()+1);
			break;
		case(4):
			if (_chassi.getSplitAmount()-1 < 1) break;
			_chassi.setSplitAmount(_chassi.getSplitAmount()-1);
			break;
		case(5):
			MainProxy.sendPacketToServer(new PacketSplitSendingSettings(NetworkConstants.CHASSIS_SPLITSENDING_CONFIG, _chassi.xCoord, _chassi.yCoord, _chassi.zCoord, _chassi.getSplitGroup(), _chassi.getSplitAmount(), _chassi.getSplitGroup()>0?false:true).getPacket());
			break;
		default: break;
		}	
	}
	
	
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		fontRenderer.drawString(""+_chassi.getSplitGroup(), 54, 36, 0x404040);
		fontRenderer.drawString(""+_chassi.getSplitAmount(), 114, 36, 0x404040);


		fontRenderer.drawString("Group", 43, 68, 0x404040);
		fontRenderer.drawString("Amount", 100, 68, 0x404040);

	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Chassi_SplitSending;
	}
	
}
