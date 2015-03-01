package logisticspipes.gui.modules;

import logisticspipes.modules.ModuleProvider;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.module.ProviderModuleIncludePacket;
import logisticspipes.network.packets.module.ProviderModuleNextModePacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiStringHandlerButton;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class GuiProvider extends ModuleBaseGui {
	
	private final IInventory _playerInventory;
	private final ModuleProvider _provider;

	public GuiProvider(IInventory playerInventory, ModuleProvider provider) {
		super(null, provider);
		_playerInventory = playerInventory;
		_provider = provider;
		
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
       buttonList.add(new GuiStringHandlerButton(0, width / 2 + 50, height / 2 - 38, 45, 20, new GuiStringHandlerButton.StringHandler() {
		@Override
		public String getContent() {
			return _provider.isActive() ? "Send" : "Hold";
		}
       }));
       buttonList.add(new GuiButton(1, width / 2 - 90, height / 2 - 41, 38, 20, "Switch"));
	}
	
	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton.id == 0){
			_provider.setFilterExcluded(!_provider.isExcludeFilter());
			MainProxy.sendPacketToServer(PacketHandler.getPacket(ProviderModuleIncludePacket.class).setModulePos(_provider));
		} else if (guibutton.id == 1){
			_provider.setIsActive(!_provider.isActive());
			MainProxy.sendPacketToServer(PacketHandler.getPacket(ProviderModuleNextModePacket.class).setModulePos(_provider));
		}else if (guibutton.id == 2){
			_provider.nextExtractionMode();
			MainProxy.sendPacketToServer(PacketHandler.getPacket(ProviderModuleNextModePacket.class).setModulePos(_provider));
		}
		super.actionPerformed(guibutton);
	}
	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/supplier.png");	
	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(TEXTURE);
		int j = guiLeft;
		int k = guiTop;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		mc.fontRenderer.drawString(_provider.getFilterInventory().getInventoryName(), xSize / 2 - mc.fontRenderer.getStringWidth(_provider.getFilterInventory().getInventoryName())/2, 6, 0x404040);
		mc.fontRenderer.drawString("Inventory", 18, ySize - 102, 0x404040);
		mc.fontRenderer.drawString("Mode: " + _provider.getExtractionMode().getExtractionModeString(), 9, ySize - 112, 0x404040);
		mc.fontRenderer.drawString("Excess Inventory: " + _provider.getExtractionMode().getExtractionModeString(), 9, ySize - 112, 0x404040);
	}
}
