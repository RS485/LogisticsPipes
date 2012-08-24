package logisticspipes.buildcraft.logisticspipes.modules;

import logisticspipes.buildcraft.krapht.GuiIDs;
import logisticspipes.buildcraft.krapht.network.NetworkConstants;
import logisticspipes.buildcraft.krapht.network.PacketPipeInteger;
import logisticspipes.buildcraft.logisticspipes.ExtractionMode;
import logisticspipes.buildcraft.logisticspipes.modules.ModuleProvider;
import logisticspipes.krapht.gui.DummyContainer;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.IInventory;

import org.lwjgl.opengl.GL11;

import buildcraft.transport.Pipe;
import cpw.mods.fml.common.network.PacketDispatcher;

public class GuiProvider extends GuiWithPreviousGuiContainer {
	
	private final IInventory _playerInventory;
	private final ModuleProvider _provider;
	private final Pipe _pipe;
	private final int _slot;


	public GuiProvider(IInventory playerInventory, Pipe pipe, ModuleProvider provider, GuiScreen previousGui, int slot) {
		super(null,pipe,previousGui);
		_playerInventory = playerInventory;
		_provider = provider;
		_pipe = pipe;
		_slot = slot;
		
		DummyContainer dummy = new DummyContainer(_playerInventory, _provider.getFilterInventory());
		dummy.addNormalSlotsForPlayerInventory(18, 97);
		
		int xOffset = 72;
		int yOffset = 18;
		
		for (int row = 0; row < 3; row++){
			for (int column = 0; column < 3; column++){
				dummy.addDummySlot(column + row * 3, xOffset + column * 18, yOffset + row * 18);					
			}
		}
		
	    this.inventorySlots = dummy;
		xSize = 194;
		ySize = 186;

	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
       controlList.clear();
       controlList.add(new GuiButton(0, width / 2 + 40, height / 2 - 59, 45, 20, _provider.isExcludeFilter() ? "Exclude" : "Include"));
       controlList.add(new GuiButton(1, width / 2 - 90, height / 2 - 41, 38, 20, "Switch"));
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton.id == 0){
			_provider.setFilterExcluded(!_provider.isExcludeFilter());
			((GuiButton)controlList.get(0)).displayString = _provider.isExcludeFilter() ? "Exclude" : "Include";
			PacketDispatcher.sendPacketToServer(new PacketPipeInteger(NetworkConstants.PROVIDER_MODULE_CHANGE_INCLUDE, _pipe.xCoord, _pipe.yCoord, _pipe.zCoord, _slot).getPacket());
		} else if (guibutton.id  == 1){
			_provider.nextExtractionMode();
			PacketDispatcher.sendPacketToServer(new PacketPipeInteger(NetworkConstants.PROVIDER_MODULE_NEXT_MODE, _pipe.xCoord, _pipe.yCoord, _pipe.zCoord, _slot).getPacket());
		}
		super.actionPerformed(guibutton);
	}
	
	private String getExtractionModeString(){
		switch(_provider.getExtractionMode()){
			case Normal:
				return "Normal";
			case LeaveFirst:
				return "Leave 1st stack";
			case LeaveLast: 
				return "Leave last stack";
			case LeaveFirstAndLast:
				return "Leave first & last stack";
			case Leave1PerStack:
				return "Leave 1 item per stack";
			default:
				return "Unknown!";
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		int i = mc.renderEngine.getTexture("/logisticspipes/gui/supplier.png");

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer() {
		super.drawGuiContainerForegroundLayer();
		fontRenderer.drawString(_provider.getFilterInventory().getInvName(), xSize / 2 - fontRenderer.getStringWidth(_provider.getFilterInventory().getInvName())/2, 6, 0x404040);
		fontRenderer.drawString("Inventory", 18, ySize - 102, 0x404040);
		fontRenderer.drawString("Mode: " + getExtractionModeString(), 9, ySize - 112, 0x404040);
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Module_Provider_ID;
	}

	public void handleModuleModeRecive(PacketPipeInteger packet) {
		ExtractionMode mode = _provider.getExtractionMode();
		int modeint = mode.ordinal();
		while(modeint != packet.integer) {
			_provider.nextExtractionMode();
			modeint = _provider.getExtractionMode().ordinal();
			if(mode.ordinal() == modeint) {
				//loop break
				break;
			}
		}
	}

	public void refreshInclude() {
		((GuiButton)controlList.get(0)).displayString = _provider.isExcludeFilter() ? "Exclude" : "Include";
	}
	
	public void handleModuleIncludeRecive(PacketPipeInteger packet) {
		_provider.setFilterExcluded(packet.integer == 1);
		refreshInclude();
	}
}
