package logisticspipes.gui.modules;

import org.lwjgl.opengl.GL11;

import logisticspipes.modules.ModuleCrafting;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.NetworkConstants;
import logisticspipes.network.packets.PacketCoordinates;
import logisticspipes.network.packets.PacketInventoryChange;
import logisticspipes.network.packets.PacketPipeInteger;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.proxy.MainProxy;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import buildcraft.transport.Pipe;

public class GuiCrafting extends GuiWithPreviousGuiContainer {

	private final IInventory _playerInventory;
	private final ModuleCrafting _crafting;
	private final Pipe _pipe;
	private final int _slot;
	private final GuiButton[] buttonarray;
	
	public GuiCrafting(IInventory playerInventory, Pipe pipe, ModuleCrafting crafting, GuiScreen previousGui, int slot) {
		super(null,pipe,previousGui);
		_playerInventory = playerInventory;
		_crafting = crafting;
		_pipe = pipe;
		_slot = slot;
		MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.REQUEST_CRAFTING_MODULE_UPDATE, _crafting.xCoord, _crafting.yCoord, _crafting.zCoord, _slot).getPacket());
		
		DummyContainer dummy = new DummyContainer(playerInventory, _crafting.getDummyInventory());
		dummy.addNormalSlotsForPlayerInventory(18, 97);

		//Input slots
        for(int l = 0; l < 9; l++) {
        	dummy.addDummySlot(l, 18 + l * 18, 18);
        }
        
        //Output slot
        dummy.addDummySlot(9, 90, 64);
		
        this.inventorySlots = dummy;
		xSize = 195;
		ySize = 187;
		buttonarray = new GuiButton[6];
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		controlList.add(new SmallGuiButton(0, (width-xSize) / 2 + 164, (height - ySize) / 2 + 50, 10,10, ">"));
		controlList.add(new SmallGuiButton(1, (width-xSize) / 2 + 129, (height - ySize) / 2 + 50, 10,10, "<"));
		//controlList.add(new SmallGuiButton(2, (width-xSize) / 2 + 138, (height - ySize) / 2 + 75, 30,10, "Paint"));
		controlList.add(new SmallGuiButton(3, (width-xSize) / 2 + 47, (height - ySize) / 2 + 50, 37,10, "Import"));
		controlList.add(new SmallGuiButton(4, (width-xSize) / 2 + 15, (height - ySize) / 2 + 50, 28,10, "Open"));
		for(int i = 0; i < 6; i++) {
			controlList.add(buttonarray[i] = new SmallGuiButton(5 + i, (width-xSize) / 2 + 20 + 18 * i, (height - ySize) / 2 + 37, 10,10, ">"));
			buttonarray[i].drawButton = false;
		}
		controlList.add(new SmallGuiButton(20, (width-xSize) / 2 + 164, (height - ySize) / 2 + 85, 10,10, ">"));
		controlList.add(new SmallGuiButton(21, (width-xSize) / 2 + 129, (height - ySize) / 2 + 85, 10,10, "<"));
	}
	
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if(5 <= guibutton.id && guibutton.id < 11) {
			_crafting.handleStackMove(guibutton.id - 5);
		}
		switch(guibutton.id){
		case 0:
			if(_slot != 20) {
				final PacketCoordinates packet = new PacketPipeInteger(NetworkConstants.CRAFTING_MODULE_NEXT_SATELLITE, _pipe.xCoord, _pipe.yCoord, _pipe.zCoord, _slot);
				MainProxy.sendPacketToServer(packet.getPacket());
			}
			else
			{
				final PacketCoordinates packet = new PacketPipeInteger(NetworkConstants.CRAFTING_MODULE_NEXT_SATELLITE, _crafting.xCoord, _crafting.yCoord, _crafting.zCoord, _slot);
				MainProxy.sendPacketToServer(packet.getPacket());
			}
			return;
		case 1: 
			if(_slot != 20) {
				final PacketCoordinates packet = new PacketPipeInteger(NetworkConstants.CRAFTING_MODULE_PREV_SATELLITE, _pipe.xCoord, _pipe.yCoord, _pipe.zCoord, _slot);
				MainProxy.sendPacketToServer(packet.getPacket());
			}
			else
			{
				final PacketCoordinates packet = new PacketPipeInteger(NetworkConstants.CRAFTING_MODULE_PREV_SATELLITE, _crafting.xCoord, _crafting.yCoord, _crafting.zCoord, _slot);
				MainProxy.sendPacketToServer(packet.getPacket());
			}
			return;
		case 2:
			_crafting.paintPathToSatellite();
			return;
		case 3:
			if(_slot != 20) {
				final PacketCoordinates packet = new PacketPipeInteger(NetworkConstants.CRAFTING_MODULE_IMPORT, _pipe.xCoord, _pipe.yCoord, _pipe.zCoord, _slot);
				MainProxy.sendPacketToServer(packet.getPacket());
			}
			else
			{
				final PacketCoordinates packet = new PacketPipeInteger(NetworkConstants.CRAFTING_MODULE_IMPORT, _crafting.xCoord, _crafting.yCoord, _crafting.zCoord, _slot);
				MainProxy.sendPacketToServer(packet.getPacket());
			}
			return;
		case 4:
			if(_slot != 20) {
				final PacketCoordinates packet = new PacketPipeInteger(NetworkConstants.CRAFTING_MODULE_OPEN_CONNECTED_GUI, _pipe.xCoord, _pipe.yCoord, _pipe.zCoord, _slot);
				MainProxy.sendPacketToServer(packet.getPacket());
			}
			else
			{
				final PacketCoordinates packet = new PacketPipeInteger(NetworkConstants.CRAFTING_MODULE_OPEN_CONNECTED_GUI, _crafting.xCoord, _crafting.yCoord, _crafting.zCoord, _slot);
				MainProxy.sendPacketToServer(packet.getPacket());
			}
			return;
		case 20:
			//_crafting.priorityUp();
			if(_slot != 20) {
				final PacketCoordinates packet = new PacketPipeInteger(NetworkConstants.CRAFTING_MODULE_PRIORITY_UP, _pipe.xCoord, _pipe.yCoord, _pipe.zCoord, _slot);
				MainProxy.sendPacketToServer(packet.getPacket());
			}
			else
			{
				final PacketCoordinates packet = new PacketPipeInteger(NetworkConstants.CRAFTING_MODULE_PRIORITY_UP, _crafting.xCoord, _crafting.yCoord, _crafting.zCoord, _slot);
				MainProxy.sendPacketToServer(packet.getPacket());
			}
			return;
		case 21:
			//_crafting.priorityDown();
			if(_slot != 20) {
				final PacketCoordinates packet = new PacketPipeInteger(NetworkConstants.CRAFTING_MODULE_PRIORITY_DOWN, _pipe.xCoord, _pipe.yCoord, _pipe.zCoord, _slot);
				MainProxy.sendPacketToServer(packet.getPacket());
			}
			else
			{
				final PacketCoordinates packet = new PacketPipeInteger(NetworkConstants.CRAFTING_MODULE_PRIORITY_DOWN, _crafting.xCoord, _crafting.yCoord, _crafting.zCoord, _slot);
				MainProxy.sendPacketToServer(packet.getPacket());
			}
			return;
		default:
			super.actionPerformed(guibutton);
			return;
		}
	}
	
	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		//inventorySlots.onCraftGuiClosed(_player); // Fix approved
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		fontRenderer.drawString("Inputs", 18, 7, 0x404040);
		fontRenderer.drawString("Output", 48, 67, 0x404040);
		fontRenderer.drawString("Inventory", 18, 86, 0x404040);
		fontRenderer.drawString("Satellite", 132, 7, 0x404040);
		
		if (_crafting.satelliteId == 0){
			fontRenderer.drawString("Off", 144, 52, 0x404040);
		} else {
			fontRenderer.drawString(""+_crafting.satelliteId , 155 - fontRenderer.getStringWidth(""+_crafting.satelliteId) , 52, 0x404040);
			/*
			if (_crafting.isSatelliteConnected()){
				MinecraftForgeClient.bindTexture(mod_LogisticsPipes.LOGISTICSPIPE_ROUTED_TEXTURE_FILE);
			}else{
				MinecraftForgeClient.bindTexture(mod_LogisticsPipes.LOGISTICSPIPE_NOTROUTED_TEXTURE_FILE);
			}*/
			//TODO /\ /\ ???
	
			//GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			//drawRect(0,1000,0,10000, 0xFFFF0000);
			//drawTexturedModalRect(155, 50, 10 * (xSize / 16) , 0, 10, 10);
			//MinecraftForgeClient.unbindTexture();
		}
		fontRenderer.drawString("Priority:" , 132 , 75, 0x404040);
		fontRenderer.drawString(""+_crafting.priority , 152 - (fontRenderer.getStringWidth(""+_crafting.priority) / 2) , 87, 0x404040);
	}
	
	@Override
	public void drawScreen(int par1, int par2, float par3) {
		super.drawScreen(par1, par2, par3);
	}
    
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		int i = mc.renderEngine.getTexture("/logisticspipes/gui/crafting.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		int j = guiLeft;
		int k = guiTop;

		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);

		drawRect(400, 400, 0, 0, 0x404040);

		for(int count=36; count<42;count++) {
			Slot slot = inventorySlots.getSlot(count);
			if(slot != null && slot.getStack() != null && slot.getStack().getMaxStackSize() < 2) {
				drawRect(guiLeft + 18 + (18 * (count-36)), guiTop + 18, guiLeft + 18 + (18 * (count-36)) + 16, guiTop + 18 + 16, 0xFFFF0000);
				buttonarray[count - 36].drawButton = true;
			} else {
				buttonarray[count - 36].drawButton = false;
			}
		}
	}

	public void handleSatelliteRecieve(PacketPipeInteger packet)
	{
		_crafting.setSatelliteId(packet.integer);
	}
	public void handleInventoryRecieve(PacketInventoryChange packet)
	{
		for (int i = 0; i < packet.itemStacks.size(); i++) {
			_crafting.setDummyInventorySlot(i, packet.itemStacks.get(i));
		}
	}
	public void handlePriorityRecieve(PacketPipeInteger packet)
	{
		_crafting.priority = packet.integer;
	}
	
	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Module_Crafting_ID;
	}

}
