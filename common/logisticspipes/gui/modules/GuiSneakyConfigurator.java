/*
 * Copyright (c) Krapht, 2012
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui.modules;

import java.io.IOException;
import java.util.Locale;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import logisticspipes.modules.LogisticsModule;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.modules.SneakyModuleDirectionUpdate;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.DummyContainer;
import network.rs485.logisticspipes.module.SneakyDirection;

public class GuiSneakyConfigurator extends ModuleBaseGui {

	private final SneakyDirection directionReceiver;

	public GuiSneakyConfigurator(IInventory playerInventory, LogisticsModule module) {
		super(new DummyContainer(playerInventory, null), module);
		if (!(module instanceof SneakyDirection)) throw new IllegalArgumentException("Module is not sneaky");
		directionReceiver = (SneakyDirection) module;
		xSize = 160;
		ySize = 200;
	}

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
			button.displayString = getButtonOrientationString(button.id == 6 ? null : EnumFacing.getFront(button.id));
		}
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) throws IOException {
		directionReceiver.setSneakyDirection(guibutton.id == 6 ? null : EnumFacing.getFront(guibutton.id));

		MainProxy.sendPacketToServer(PacketHandler.getPacket(SneakyModuleDirectionUpdate.class).setDirection(directionReceiver.getSneakyDirection()).setModulePos(module));

		refreshButtons();
		super.actionPerformed(guibutton);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {

		refreshButtons();

		super.drawGuiContainerForegroundLayer(par1, par2);

		mc.fontRenderer.drawString("Sneaky orientation", xSize / 2 - mc.fontRenderer.getStringWidth("Sneaky orientation") / 2, 10, 0x404040);
	}

	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/sneaky.png");

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(GuiSneakyConfigurator.TEXTURE);
		int j = guiLeft;
		int k = guiTop;
		//drawRect(width/2 - xSize / 2, height / 2 - ySize /2, width/2 + xSize / 2, height / 2 + ySize /2, 0xFF404040);
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}

	private String getButtonOrientationString(EnumFacing orientation) {
		String s = (orientation == null ? "DEFAULT" : orientation.name());
		if (orientation == directionReceiver.getSneakyDirection()) {
			return "\u00a7a>" + s + "<";
		}
		return s.toLowerCase(Locale.US);
	}

}
