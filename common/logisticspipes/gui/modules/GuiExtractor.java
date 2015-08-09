/**
 * Copyright (c) Krapht, 2012
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui.modules;

import java.util.Locale;

import logisticspipes.modules.abstractmodules.LogisticsSneakyDirectionModule;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.module.ExtractorModuleDirectionPacket;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyContainer;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

public class GuiExtractor extends ModuleBaseGui {

	private final LogisticsSneakyDirectionModule _directionReceiver;

	public GuiExtractor(IInventory playerInventory, LogisticsSneakyDirectionModule directionReceiver) {
		super(new DummyContainer(playerInventory, null), directionReceiver);
		_directionReceiver = directionReceiver;
		xSize = 160;
		ySize = 200;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui() {
		super.initGui();

		int left = width / 2 - xSize / 2;
		int top = height / 2 - ySize / 2;

		buttonList.add(new GuiButton(0, left + 110, top + 103, 40, 20, "")); //DOWN
		buttonList.add(new GuiButton(1, left + 110, top + 43, 40, 20, "")); //UP
		buttonList.add(new GuiButton(2, left + 50, top + 53, 50, 20, "")); //NORTH
		buttonList.add(new GuiButton(3, left + 50, top + 93, 50, 20, "")); //SOUTH
		buttonList.add(new GuiButton(4, left + 10, top + 73, 40, 20, "")); //WEST
		buttonList.add(new GuiButton(5, left + 100, top + 73, 40, 20, "")); //EAST
		buttonList.add(new GuiButton(6, left + 10, top + 23, 60, 20, "")); //DEFAULT

		refreshButtons();
	}

	private void refreshButtons() {
		for (Object p : buttonList) {
			GuiButton button = (GuiButton) p;
			button.displayString = isExtract(ForgeDirection.getOrientation(button.id));
		}
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		_directionReceiver.setSneakyDirection(ForgeDirection.getOrientation(guibutton.id));

		MainProxy.sendPacketToServer(PacketHandler.getPacket(ExtractorModuleDirectionPacket.class).setDirection(_directionReceiver.getSneakyDirection()).setModulePos(_directionReceiver));

		refreshButtons();
		super.actionPerformed(guibutton);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {

		refreshButtons();

		super.drawGuiContainerForegroundLayer(par1, par2);

		mc.fontRenderer.drawString("Extract orientation", xSize / 2 - mc.fontRenderer.getStringWidth("Extract orientation") / 2, 10, 0x404040);
	}

	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/extractor.png");

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(GuiExtractor.TEXTURE);
		int j = guiLeft;
		int k = guiTop;
		//drawRect(width/2 - xSize / 2, height / 2 - ySize /2, width/2 + xSize / 2, height / 2 + ySize /2, 0xFF404040);
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}

	private String isExtract(ForgeDirection o) {
		String s = (o == ForgeDirection.UNKNOWN ? "DEFAULT" : o.name());
		if (o == _directionReceiver.getSneakyDirection()) {
			return "\u00a7a>" + s + "<";
		}
		return s.toLowerCase(Locale.US);
	}

	public void setMode(ForgeDirection o) {
		_directionReceiver.setSneakyDirection(o);
		refreshButtons();
	}
}
