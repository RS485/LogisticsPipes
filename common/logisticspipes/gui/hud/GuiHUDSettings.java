package logisticspipes.gui.hud;

import java.io.IOException;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;

import lombok.SneakyThrows;

import logisticspipes.LPItems;
import logisticspipes.hud.HUDConfig;
import logisticspipes.interfaces.IHUDConfig;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.hud.HUDSettingsPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiCheckBox;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;

public class GuiHUDSettings extends LogisticsBaseGuiScreen {

	private int slot;
	private EntityPlayer player;

	public GuiHUDSettings(EntityPlayer player, int slot) {
		super(180, 160, 0, 0);
		this.slot = slot;
		this.player = player;
		DummyContainer dummy = new DummyContainer(player.inventory, null);
		dummy.addRestrictedHotbarForPlayerInventory(10, 134);
		dummy.addRestrictedArmorForPlayerInventory(10, 65);
		inventorySlots = dummy;
	}

	@Override
	@SneakyThrows(IOException.class)
	public void initGui() {
		super.initGui();
		if (!player.inventory.getStackInSlot(slot).isEmpty()) {
			IHUDConfig config = new HUDConfig(player.inventory.getStackInSlot(slot));
			buttonList.add(new GuiCheckBox(0, guiLeft + 30, guiTop + 10, 12, 12, config.isHUDChassie()));
			buttonList.add(new GuiCheckBox(1, guiLeft + 30, guiTop + 30, 12, 12, config.isHUDCrafting()));
			buttonList.add(new GuiCheckBox(2, guiLeft + 30, guiTop + 50, 12, 12, config.isHUDInvSysCon()));
			buttonList.add(new GuiCheckBox(3, guiLeft + 30, guiTop + 70, 12, 12, config.isHUDPowerLevel()));
			buttonList.add(new GuiCheckBox(4, guiLeft + 30, guiTop + 90, 12, 12, config.isHUDProvider()));
			buttonList.add(new GuiCheckBox(5, guiLeft + 30, guiTop + 110, 12, 12, config.isHUDSatellite()));
		} else {
			closeGui();
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (buttonList.get(button.id) instanceof GuiCheckBox) {
			((GuiCheckBox) buttonList.get(button.id)).change();
			MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDSettingsPacket.class).setButtonId(button.id).setState(((GuiCheckBox) buttonList.get(button.id)).getState()).setSlot(slot));
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		if (player.inventory.getStackInSlot(slot).isEmpty() || player.inventory.getStackInSlot(slot).getItem() != LPItems.hudGlasses) {
			mc.player.closeScreen();
		}
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		mc.fontRenderer.drawString("HUD Chassie Pipe", guiLeft + 50, guiTop + 13, 0x4c4c4c);
		mc.fontRenderer.drawString("HUD Crafting Pipe", guiLeft + 50, guiTop + 33, 0x4c4c4c);
		mc.fontRenderer.drawString("HUD InvSysCon Pipe", guiLeft + 50, guiTop + 53, 0x4c4c4c);
		mc.fontRenderer.drawString("HUD Power Junction", guiLeft + 50, guiTop + 73, 0x4c4c4c);
		mc.fontRenderer.drawString("HUD Provider Pipe", guiLeft + 50, guiTop + 93, 0x4c4c4c);
		mc.fontRenderer.drawString("HUD Satellite Pipe", guiLeft + 50, guiTop + 113, 0x4c4c4c);
		GuiGraphics.drawPlayerHotbarBackground(mc, guiLeft + 10, guiTop + 134);
		GuiGraphics.drawPlayerArmorBackground(mc, guiLeft + 10, guiTop + 65);
	}
}
