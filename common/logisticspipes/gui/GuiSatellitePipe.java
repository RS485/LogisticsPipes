/**
 * Copyright (c) Krapht, 2011
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package logisticspipes.gui;

import java.io.IOException;
import javax.annotation.Nonnull;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import org.lwjgl.input.Keyboard;

import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.satpipe.SatelliteSetNamePacket;
import logisticspipes.pipes.PipeFluidSatellite;
import logisticspipes.pipes.PipeItemsSatelliteLogistics;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.InputBar;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import logisticspipes.utils.gui.SmallGuiButton;
import logisticspipes.utils.string.StringUtils;

public class GuiSatellitePipe extends LogisticsBaseGuiScreen {

	private PipeItemsSatelliteLogistics _satellite;
	private PipeFluidSatellite _liquidSatellite;
	private InputBar input;

	private GuiSatellitePipe(EntityPlayer player) {
		super(new Container() {

			@Override
			public boolean canInteractWith(@Nonnull EntityPlayer entityplayer) {
				return true;
			}
		});
		xSize = 116;
		ySize = 77;
	}

	public GuiSatellitePipe(PipeItemsSatelliteLogistics satellite, EntityPlayer player) {
		this(player);
		_satellite = satellite;
	}

	public GuiSatellitePipe(PipeFluidSatellite satellite, EntityPlayer player) {
		this(player);
		_liquidSatellite = satellite;
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);

		super.initGui();
		buttonList.add(new SmallGuiButton(0, (width / 2) - (30 / 2) + 35, (height / 2) + 20, 30, 10, "Save"));
		input = new InputBar(fontRenderer, this, guiLeft + 8, guiTop + 40, 100, 16);
	}

	@Override
	public void closeGui() throws IOException {
		super.closeGui();
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) throws IOException {
		if (_satellite != null) {
			if (guibutton.id == 0) {
				MainProxy.sendPacketToServer(PacketHandler.getPacket(SatelliteSetNamePacket.class).setString(input.getText()).setTilePos(_satellite.getContainer()));
			} else {
				super.actionPerformed(guibutton);
			}
		} else if (_liquidSatellite != null) {
			if (guibutton.id == 0) {
				MainProxy.sendPacketToServer(PacketHandler.getPacket(SatelliteSetNamePacket.class).setString(input.getText()).setTilePos(_liquidSatellite.getContainer()));
			} else {
				super.actionPerformed(guibutton);
			}
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		mc.fontRenderer.drawString(StringUtils.translate("gui.satellite.SatelliteName"), 33, 10, 0x404040);
		String name = "";
		if (_satellite != null) {
			name = StringUtils.getCuttedString(_satellite.satellitePipeName, 100, mc.fontRenderer);
		}
		if (_liquidSatellite != null) {
			name = StringUtils.getCuttedString(_liquidSatellite.satellitePipeName, 100, mc.fontRenderer);
		}
		mc.fontRenderer.drawString(name, 59 - mc.fontRenderer.getStringWidth(name) / 2, 24, 0x404040);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		super.drawGuiContainerBackgroundLayer(f, x, y);
		GuiGraphics.drawGuiBackGround(mc, guiLeft, guiTop, right, bottom, zLevel, true);
		input.drawTextBox();
	}

	@Override
	protected void mouseClicked(int x, int y, int k) throws IOException {
		if (!input.handleClick(x, y, k)) {
			super.mouseClicked(x, y, k);
		}
	}

	@Override
	public void keyTyped(char c, int i) throws IOException {
		if (!input.handleKey(c, i)) {
			super.keyTyped(c, i);
		}
	}

}
