package logisticspipes.gui.hud;

import logisticspipes.LogisticsPipes;
import logisticspipes.hud.HUDConfig;
import logisticspipes.interfaces.IHUDConfig;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.hud.HUDSettingsPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.DummyContainer;
import logisticspipes.utils.gui.GuiCheckBox;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;

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
		this.inventorySlots = dummy;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		if(player.inventory.getStackInSlot(slot) != null) {
			IHUDConfig config = new HUDConfig(player.inventory.getStackInSlot(slot));
			this.buttonList.add(new GuiCheckBox(0, guiLeft + 30, guiTop +  10, 12, 12, config.isHUDChassie()));
			this.buttonList.add(new GuiCheckBox(1, guiLeft + 30, guiTop +  30, 12, 12, config.isHUDCrafting()));
			this.buttonList.add(new GuiCheckBox(2, guiLeft + 30, guiTop +  50, 12, 12, config.isHUDInvSysCon()));
			this.buttonList.add(new GuiCheckBox(3, guiLeft + 30, guiTop +  70, 12, 12, config.isHUDPowerLevel()));
			this.buttonList.add(new GuiCheckBox(4, guiLeft + 30, guiTop +  90, 12, 12, config.isHUDProvider()));
			this.buttonList.add(new GuiCheckBox(5, guiLeft + 30, guiTop + 110, 12, 12, config.isHUDSatellite()));
		} else {
			this.closeGui();
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if(this.buttonList.get(button.id) instanceof GuiCheckBox) {
			((GuiCheckBox)this.buttonList.get(button.id)).change();
			MainProxy.sendPacketToServer(PacketHandler.getPacket(HUDSettingsPacket.class).setButtonId(button.id).setState(((GuiCheckBox)this.buttonList.get(button.id)).getState()).setSlot(slot));
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		if(player.inventory.getStackInSlot(slot) == null || player.inventory.getStackInSlot(slot).getItem() != LogisticsPipes.LogisticsHUDArmor) {
			this.mc.thePlayer.closeScreen();
		}
		BasicGuiHelper.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		mc.fontRenderer.drawString("HUD Chassie Pipe", guiLeft + 50, guiTop + 13, 0x4c4c4c);
		mc.fontRenderer.drawString("HUD Crafting Pipe", guiLeft + 50, guiTop + 33, 0x4c4c4c);
		mc.fontRenderer.drawString("HUD InvSysCon Pipe", guiLeft + 50, guiTop + 53, 0x4c4c4c);
		mc.fontRenderer.drawString("HUD Power Junction", guiLeft + 50, guiTop + 73, 0x4c4c4c);
		mc.fontRenderer.drawString("HUD Provider Pipe", guiLeft + 50, guiTop + 93, 0x4c4c4c);
		mc.fontRenderer.drawString("HUD Satellite Pipe", guiLeft + 50, guiTop + 113, 0x4c4c4c);
		BasicGuiHelper.drawPlayerHotbarBackground(mc, guiLeft + 10, guiTop + 134);
		BasicGuiHelper.drawPlayerArmorBackground(mc, guiLeft + 10, guiTop + 65);
	}
}
