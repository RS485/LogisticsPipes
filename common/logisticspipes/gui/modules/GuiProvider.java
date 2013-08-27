package logisticspipes.gui.modules;

import logisticspipes.modules.ModuleProvider;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.module.ProviderModuleIncludePacket;
import logisticspipes.network.packets.module.ProviderModuleNextModePacket;
import logisticspipes.pipes.basic.CoreRoutedPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiStringHandlerButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import buildcraft.transport.Pipe;

public class GuiProvider extends GuiWithPreviousGuiContainer {
	
	private final IInventory _playerInventory;
	private final ModuleProvider _provider;
	//private final Pipe pipe;
	private final int _slot;


	public GuiProvider(IInventory playerInventory, CoreRoutedPipe pipe, ModuleProvider provider, GuiScreen previousGui, int slot) {
		super(null,pipe,previousGui);
		_playerInventory = playerInventory;
		_provider = provider;
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
       buttonList.clear();
       buttonList.add(new GuiStringHandlerButton(0, width / 2 + 40, height / 2 - 59, 45, 20, new GuiStringHandlerButton.StringHandler() {
		@Override
		public String getContent() {
			return _provider.isExcludeFilter() ? "Exclude" : "Include";
		}
       }));
       buttonList.add(new GuiButton(1, width / 2 - 90, height / 2 - 41, 38, 20, "Switch"));
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton.id == 0){
			_provider.setFilterExcluded(!_provider.isExcludeFilter());
			if(_slot >= 0) {
//TODO 			MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.PROVIDER_MODULE_CHANGE_INCLUDE, pipe.getX(), pipe.getY(), pipe.getZ(), _slot).getPacket());
				MainProxy.sendPacketToServer(PacketHandler.getPacket(ProviderModuleIncludePacket.class).setInteger(_slot).setPosX(pipe.getX()).setPosY(pipe.getY()).setPosZ(pipe.getZ()));
			} else {
//TODO 			MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.PROVIDER_MODULE_CHANGE_INCLUDE, _provider.getX(), _provider.getY(), _provider.getZ(), _slot).getPacket());	
				MainProxy.sendPacketToServer(PacketHandler.getPacket(ProviderModuleIncludePacket.class).setInteger(_slot).setPosX(_provider.getX()).setPosY(_provider.getY()).setPosZ(_provider.getZ()));
			}
		} else if (guibutton.id  == 1){
			_provider.nextExtractionMode();
			if(_slot >= 0) {
//TODO 			MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.PROVIDER_MODULE_NEXT_MODE, pipe.getX(), pipe.getY(), pipe.getZ(), _slot).getPacket());
				MainProxy.sendPacketToServer(PacketHandler.getPacket(ProviderModuleNextModePacket.class).setInteger(_slot).setPosX(pipe.getX()).setPosY(pipe.getY()).setPosZ(pipe.getZ()));
			} else {
//TODO 			MainProxy.sendPacketToServer(new PacketPipeInteger(NetworkConstants.PROVIDER_MODULE_NEXT_MODE, _provider.getX(), _provider.getY(), _provider.getZ(), _slot).getPacket());
				MainProxy.sendPacketToServer(PacketHandler.getPacket(ProviderModuleNextModePacket.class).setInteger(_slot).setPosX(_provider.getX()).setPosY(_provider.getY()).setPosZ(_provider.getZ()));
				}
		}
		super.actionPerformed(guibutton);
	}
	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/supplier.png");	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.func_110577_a(TEXTURE);
		int j = guiLeft;
		int k = guiTop;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		fontRenderer.drawString(_provider.getFilterInventory().getInvName(), xSize / 2 - fontRenderer.getStringWidth(_provider.getFilterInventory().getInvName())/2, 6, 0x404040);
		fontRenderer.drawString("Inventory", 18, ySize - 102, 0x404040);
		fontRenderer.drawString("Mode: " + _provider.getExtractionMode().getExtractionModeString(), 9, ySize - 112, 0x404040);
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Module_Provider_ID;
	}

	public void handleModuleModeRecive(int integer) {
		_provider.setExtractionMode(integer);
	}

	public void refreshInclude() {
		((GuiButton)buttonList.get(0)).displayString = _provider.isExcludeFilter() ? "Exclude" : "Include";
	}
	
	public void handleModuleIncludeRecive(int integer) {
		_provider.setFilterExcluded(integer == 1);
		refreshInclude();
	}
}
