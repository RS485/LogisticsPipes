package logisticspipes.gui.orderer;

import logisticspipes.gui.popup.GuiDiskPopup;
import logisticspipes.interfaces.IDiskProvider;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.orderer.DiskDropPacket;
import logisticspipes.network.packets.orderer.DiskRequestConectPacket;
import logisticspipes.pipes.PipeItemsRequestLogisticsMk2;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.Color;
import logisticspipes.utils.gui.ItemDisplay;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.item.ItemIdentifier;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

public class NormalMk2GuiOrderer extends NormalGuiOrderer implements IDiskProvider {

	public PipeItemsRequestLogisticsMk2 pipe;
	private SmallGuiButton Macrobutton;
	private RenderItem renderItem = new RenderItem();

	public NormalMk2GuiOrderer(PipeItemsRequestLogisticsMk2 RequestPipeMK2, EntityPlayer entityPlayer) {
		super(RequestPipeMK2.getX(), RequestPipeMK2.getY(), RequestPipeMK2.getZ(), MainProxy.getDimensionForWorld(RequestPipeMK2.getWorld()), entityPlayer);
		pipe = RequestPipeMK2;
		MainProxy.sendPacketToServer(PacketHandler.getPacket(DiskRequestConectPacket.class).setPosX(pipe.getX()).setPosY(pipe.getY()).setPosZ(pipe.getZ()));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();
		buttonList.add(Macrobutton = new SmallGuiButton(12, right - 55, bottom - 60, 50, 10, "Disk"));
		Macrobutton.enabled = false;
	}

	@Override
	public void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		super.drawGuiContainerBackgroundLayer(f, i, j);

		drawRect(right - 39, bottom - 47, right - 19, bottom - 27, Color.BLACK);
		drawRect(right - 37, bottom - 45, right - 21, bottom - 29, Color.DARKER_GREY);

		if (pipe.getDisk() != null) {
			renderItem.renderItemIntoGUI(mc.fontRenderer, mc.renderEngine, pipe.getDisk(), right - 37, bottom - 45);
			Macrobutton.enabled = true;
		} else {
			Macrobutton.enabled = false;
		}
		GL11.glDisable(2896 /*GL_LIGHTING*/);
	}

	@Override
	protected void mouseClicked(int x, int y, int k) {
		if (x >= right - 39 && x < right - 19 && y >= bottom - 47 && y < bottom - 27) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(DiskDropPacket.class).setPosX(pipe.getX()).setPosY(pipe.getY()).setPosZ(pipe.getZ()));
		} else {
			super.mouseClicked(x, y, k);
		}
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		super.actionPerformed(guibutton);
		if (guibutton.id == 12) {
			MainProxy.sendPacketToServer(PacketHandler.getPacket(DiskRequestConectPacket.class).setPosX(pipe.getX()).setPosY(pipe.getY()).setPosZ(pipe.getZ()));
			setSubGui(new GuiDiskPopup(this));
		}
	}

	@Override
	public ItemStack getDisk() {
		return pipe.getDisk();
	}

	@Override
	public void specialItemRendering(ItemIdentifier item, int x, int y) {
		GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
		if (SimpleServiceLocator.thaumCraftProxy.isScannedObject(item.unsafeMakeNormalStack(1), mc.thePlayer.getDisplayName())) {
			SimpleServiceLocator.thaumCraftProxy.renderAspectsDown(item.unsafeMakeNormalStack(1), -20, 10, this);
		}
		GL11.glPopAttrib();
	}

	@Override
	public int getX() {
		return pipe.getX();
	}

	@Override
	public int getY() {
		return pipe.getY();
	}

	@Override
	public int getZ() {
		return pipe.getZ();
	}

	@Override
	public ItemDisplay getItemDisplay() {
		return itemDisplay;
	}
}
