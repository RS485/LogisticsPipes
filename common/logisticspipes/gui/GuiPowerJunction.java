package logisticspipes.gui;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity;
import logisticspipes.network.GuiIDs;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.block.PowerJunctionCheatPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.KraphtBaseGuiScreen;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class GuiPowerJunction extends KraphtBaseGuiScreen {

	private final LogisticsPowerJunctionTileEntity junction;
	
	public GuiPowerJunction(EntityPlayer player, LogisticsPowerJunctionTileEntity junction) {
		super(176, 166, 0, 0);
		this.inventorySlots = junction.createContainer(player);
		this.junction = junction;
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Power_Junction_ID;
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		
	}
	
	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/power_junction.png");

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.func_110577_a(TEXTURE);
		int j = guiLeft;
		int k = guiTop;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
		int level = 100 - junction.getChargeState();
		drawTexturedModalRect(j + 10, k + 11 + (level * 59 / 100), 176, level * 59 / 100, 5, 59 - (level * 59 / 100));
		mc.fontRenderer.drawString("Logistics Power Junction", guiLeft + 30, guiTop + 8, 0x404040);
		mc.fontRenderer.drawString("Stored Energy:", guiLeft + 40, guiTop + 25, 0x404040);
		mc.fontRenderer.drawString(""+junction.getPowerLevel() + " LP", guiLeft + 40, guiTop + 35, 0x404040);
		mc.fontRenderer.drawString("/ "+junction.MAX_STORAGE + " LP", guiLeft + 40, guiTop + 45, 0x404040);
		mc.fontRenderer.drawString("1 MJ = 5 LP", guiLeft + 30, guiTop + 65, 0x404040);
		mc.fontRenderer.drawString("1 EU = 2 LP", guiLeft + 100, guiTop + 65, 0x404040);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		this.buttonList.clear();
		if (LogisticsPipes.DEBUG) {
			this.buttonList.add(new GuiButton(0, guiLeft + 140, guiTop + 20, 20, 20, "+"));
		}
	}
	
	@Override
	protected void actionPerformed(GuiButton par1GuiButton) {
		if(par1GuiButton.id == 0) {
			junction.addEnergy(100000);
//TODO 		MainProxy.sendPacketToServer(new PacketCoordinates(NetworkConstants.CHEATJUNCTIONPOWER, junction.getX(), junction.getY(), junction.getZ()).getPacket());
			MainProxy.sendPacketToServer(PacketHandler.getPacket(PowerJunctionCheatPacket.class).setPosX(junction.getX()).setPosY(junction.getY()).setPosZ(junction.getZ()));
		} else {
			super.actionPerformed(par1GuiButton);		
		}
	}


}
