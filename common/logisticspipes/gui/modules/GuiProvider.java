package logisticspipes.gui.modules;

import java.io.IOException;
import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import logisticspipes.modules.ModuleProvider;
import logisticspipes.network.packets.module.PropertyModuleUpdate;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiStringHandlerButton;
import network.rs485.logisticspipes.inventory.ProviderMode;
import network.rs485.logisticspipes.property.BooleanProperty;
import network.rs485.logisticspipes.property.EnumProperty;
import network.rs485.logisticspipes.property.InventoryProperty;
import network.rs485.logisticspipes.property.Property;
import network.rs485.logisticspipes.property.PropertyLayer;

public class GuiProvider extends ModuleBaseGui {

	private final PropertyLayer propertyLayer;
	private final InventoryProperty filterInventory;
	private final EnumProperty<ProviderMode> providerMode;
	private final BooleanProperty isExclusionFilter;
	private final BooleanProperty isActive;

	public GuiProvider(IInventory playerInventory, ModuleProvider provider) {
		super(null, provider);
		propertyLayer = new PropertyLayer(provider.propertyList) {

			@Override
			protected void onChange(@Nonnull Property<?> property) {}
		};
		filterInventory = propertyLayer.getWritableProperty(provider.filterInventory);
		providerMode = propertyLayer.getWritableProperty(provider.providerMode);
		isExclusionFilter = propertyLayer.getWritableProperty(provider.isExclusionFilter);
		isActive = propertyLayer.getWritableProperty(provider.isActive);

		DummyContainer dummy = new DummyContainer(playerInventory, filterInventory);
		dummy.addNormalSlotsForPlayerInventory(18, 97);

		int xOffset = 72;
		int yOffset = 18;

		for (int row = 0; row < 3; row++) {
			for (int column = 0; column < 3; column++) {
				dummy.addDummySlot(column + row * 3, xOffset + column * 18, yOffset + row * 18);
			}
		}

		inventorySlots = dummy;
		xSize = 194;
		ySize = 186;
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		buttonList.add(new GuiStringHandlerButton(0, width / 2 + 40, height / 2 - 59, 45, 20,
				() -> (isExclusionFilter.getValue() ? "Exclude" : "Include")));
		buttonList.add(new GuiButton(1, width / 2 - 90, height / 2 - 41, 38, 20, "Switch"));
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		propertyLayer.unregister();
		if (this.mc.player != null && !propertyLayer.getProperties().isEmpty()) {
			// send update to server, when there are changed properties
			MainProxy.sendPacketToServer(PropertyModuleUpdate.fromPropertyHolder(propertyLayer).setModulePos(module));
		}
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) throws IOException {
		if (guibutton.id == 0) {
			isExclusionFilter.toggle();
		} else if (guibutton.id == 1) {
			providerMode.next();
		} else if (guibutton.id == 2) {
			isActive.toggle();
		}
		super.actionPerformed(guibutton);
	}

	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/supplier.png");

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(GuiProvider.TEXTURE);
		int j = guiLeft;
		int k = guiTop;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		mc.fontRenderer.drawString(
			filterInventory.getName(),
			xSize / 2 - mc.fontRenderer.getStringWidth(filterInventory.getName()) / 2,
			6,
			0x404040
		);
		mc.fontRenderer.drawString("Inventory", 18, ySize - 102, 0x404040);
		//mc.fontRenderer.drawString("Mode: " + _provider.getExtractionMode().getExtractionModeString(), 9, ySize - 112, 0x404040);
		mc.fontRenderer.drawString(
			"Excess Inventory: " + providerMode.getValue().getExtractionModeString(),
			9,
			ySize - 112,
			0x404040
		);
	}
}
