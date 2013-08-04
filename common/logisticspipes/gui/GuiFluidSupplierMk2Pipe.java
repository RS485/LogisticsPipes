/** 
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public 
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui;

import logisticspipes.interfaces.IGuiIDHandlerProvider;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.pipe.FluidSupplierAmount;
import logisticspipes.network.packets.pipe.FluidSupplierMode;
import logisticspipes.pipes.PipeFluidSupplierMk2;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.SmallGuiButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.IInventory;

public class GuiFluidSupplierMk2Pipe extends GuiContainer implements IGuiIDHandlerProvider {
	
	private IInventory dummyInventory;
	private PipeFluidSupplierMk2 logic;
	
	public GuiFluidSupplierMk2Pipe(IInventory playerInventory, IInventory dummyInventory, PipeFluidSupplierMk2 logic) {
		super(null);
		
		
		DummyContainer dummy = new DummyContainer(playerInventory, dummyInventory);
		dummy.addNormalSlotsForPlayerInventory(13, 92);
		
		
		dummy.addFluidSlot(0, dummyInventory, 60, 18);					
		
		this.inventorySlots = dummy; 

		this.dummyInventory = dummyInventory;
		this.logic = logic;
		xSize = 184;
		ySize = 176;
//TODO 	MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.LIQUID_SUPPLIER_LIQUID_AMOUNT, this.logic.getX(), this.logic.getY(), this.logic.getZ(), 0).getPacket());
		MainProxy.sendPacketToServer(PacketHandler.getPacket(FluidSupplierAmount.class).setInteger(0).setPosX(this.logic.getX()).setPosY(this.logic.getY()).setPosZ(this.logic.getZ()));
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		fontRenderer.drawString(dummyInventory.getInvName(), xSize / 2 - fontRenderer.getStringWidth(dummyInventory.getInvName())/2, 6, 0x404040);
		fontRenderer.drawString("Inventory", 15, ySize - 95, 0x404040);
		fontRenderer.drawString("Fluid:", 25, 22, 0x404040);
		fontRenderer.drawString("Partial requests:", xSize - 140, ySize - 109, 0x404040);
		fontRenderer.drawString(Integer.toString(logic.getAmount()), xSize / 2, 22, 0x404040);
		fontRenderer.drawString("+", 32, 39, 0x404040);
		fontRenderer.drawString("-", 32, 50, 0x404040);
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {	
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, guiLeft + xSize, guiTop + ySize, zLevel, true);
		BasicGuiHelper.drawPlayerInventoryBackground(mc, guiLeft + 13, guiTop + 92);
		BasicGuiHelper.drawSlotBackground(mc, guiLeft + 59, guiTop + 17);
		//GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		//mc.renderEngine.func_110577_a("/logisticspipes/gui/supplier.png");
		//int j = guiLeft;
		//int k = guiTop;
		//drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
       buttonList.clear();
       buttonList.add(new GuiButton(0, width / 2 + 45, guiTop + ySize - 115, 30, 20, logic.isRequestingPartials() ? "Yes" : "No"));
       buttonList.add(new SmallGuiButton(10, guiLeft + 40, guiTop + 37, 10, 10, "1"));
       buttonList.add(new SmallGuiButton(11, guiLeft + 40, guiTop + 48, 10, 10, "1"));
       buttonList.add(new SmallGuiButton(20, guiLeft + 51, guiTop + 37, 20, 10, "10"));
       buttonList.add(new SmallGuiButton(21, guiLeft + 51, guiTop + 48, 20, 10, "10"));
       buttonList.add(new SmallGuiButton(30, guiLeft + 72, guiTop + 37, 30, 10, "100"));
       buttonList.add(new SmallGuiButton(31, guiLeft + 72, guiTop + 48, 30, 10, "100"));
       buttonList.add(new SmallGuiButton(40, guiLeft + 103, guiTop + 37, 40, 10, "1000"));
       buttonList.add(new SmallGuiButton(41, guiLeft + 103, guiTop + 48, 40, 10, "1000"));
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton.id == 0){
			logic.setRequestingPartials(!logic.isRequestingPartials());
			((GuiButton)buttonList.get(0)).displayString = logic.isRequestingPartials() ? "Yes" : "No";
//TODO 		MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.LIQUID_SUPPLIER_PARTIALS, logic.getX(), logic.getY(), logic.getZ(), (logic.isRequestingPartials() ? 1 : 0)).getPacket());
			MainProxy.sendPacketToServer(PacketHandler.getPacket(FluidSupplierMode.class).setInteger((logic.isRequestingPartials() ? 1 : 0)).setPosX(logic.getX()).setPosY(logic.getY()).setPosZ(logic.getZ()));
		}
		if((guibutton.id % 10 == 0 || guibutton.id % 10 == 1) && guibutton.id / 10 < 5 && guibutton.id / 10 > 0) {
			int change = 1;
			if(guibutton.id % 10 == 1) change = -1;
			change *= Math.pow(10, guibutton.id / 10 - 1);
//TODO 		MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.LIQUID_SUPPLIER_LIQUID_AMOUNT, this.logic.getX(), this.logic.getY(), this.logic.getZ(), change).getPacket());
			MainProxy.sendPacketToServer(PacketHandler.getPacket(FluidSupplierAmount.class).setInteger(change).setPosX(this.logic.getX()).setPosY(this.logic.getY()).setPosZ(this.logic.getZ()));
		}
		super.actionPerformed(guibutton);
		
	}
	
	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_FluidSupplier_ID;
	}
}
