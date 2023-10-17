package logisticspipes.gui.hud;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

import logisticspipes.interfaces.IHUDConfig;
import logisticspipes.utils.gui.GuiGraphics;
import logisticspipes.utils.gui.hud.BasicHUDButton;
import logisticspipes.utils.item.ItemStackRenderer;
import logisticspipes.utils.item.ItemStackRenderer.DisplayAmount;
import network.rs485.logisticspipes.SatellitePipe;

public class HUDSatellite extends BasicHUDGui {

	@Nonnull
	private final SatellitePipe pipe;
	private int page;

	public HUDSatellite(@Nonnull SatellitePipe pipe) {
		this.pipe = pipe;
		addButton(new BasicHUDButton("<", -2, -40, 8, 8) {

			@Override
			public void clicked() {
				if (page > 0) {
					page--;
				}
			}

			@Override
			public boolean shouldRenderButton() {
				return HUDSatellite.this.pipe.getItemList().size() > 0;
			}

			@Override
			public boolean buttonEnabled() {
				return page > 0;
			}
		});
		addButton(new BasicHUDButton(">", 37, -40, 8, 8) {

			@Override
			public void clicked() {
				if (page + 1 < getMaxPage()) {
					page++;
				}
			}

			@Override
			public boolean shouldRenderButton() {
				return HUDSatellite.this.pipe.getItemList().size() > 0;
			}

			@Override
			public boolean buttonEnabled() {
				return page + 1 < getMaxPage();
			}
		});
	}

	@Override
	public void renderHeadUpDisplay(double distance, boolean day, boolean shifted, Minecraft mc, IHUDConfig config) {
		if (pipe.getItemList().size() > 0) {
			if (day) {
				GL11.glColor4b((byte) 64, (byte) 64, (byte) 64, (byte) 64);
			} else {
				GL11.glColor4b((byte) 127, (byte) 127, (byte) 127, (byte) 64);
			}
			GuiGraphics.drawGuiBackGround(mc, -50, -50, 50, 50, 0, false);
			if (day) {
				GL11.glColor4b((byte) 64, (byte) 64, (byte) 64, (byte) 127);
			} else {
				GL11.glColor4b((byte) 127, (byte) 127, (byte) 127, (byte) 127);
			}

			GL11.glTranslatef(0.0F, 0.0F, -0.01F);
			super.renderHeadUpDisplay(distance, day, shifted, mc, config);

			String message;
			message = pipe.getSatellitePipeName();
			if (mc.fontRenderer.getStringWidth(message) > 40) {
				GL11.glScalef(0.45F, 0.45F, 0.0001F);
				mc.fontRenderer.drawString(message, -100, -85, 0);
				GL11.glScalef(1 / 0.45F, 1 / 0.45F, 1);
			} else {
				GL11.glScalef(1.0F, 1.0F, 0.0001F);
				mc.fontRenderer.drawString(message, -42, -40, 0);
			}
			GL11.glScalef(1.5F, 1.5F, 1);
			GL11.glScalef(0.8F, 0.8F, -1F);
			ItemStackRenderer.renderItemIdentifierStackListIntoGui(pipe.getItemList(), null, page, -35, -20, 4, 12, 18, 18, 100.0F, DisplayAmount.ALWAYS, false, shifted);
			GL11.glScalef(0.8F, 0.8F, -1F);
			message = String.format("(%d/%d)", page + 1, getMaxPage());
			mc.fontRenderer.drawString(message, 9, -41, 0);
		} else {
			if (day) {
				GL11.glColor4b((byte) 64, (byte) 64, (byte) 64, (byte) 64);
			} else {
				GL11.glColor4b((byte) 127, (byte) 127, (byte) 127, (byte) 64);
			}
			GuiGraphics.drawGuiBackGround(mc, -50, -15, 50, 20, 0, false);
			if (day) {
				GL11.glColor4b((byte) 64, (byte) 64, (byte) 64, (byte) 127);
			} else {
				GL11.glColor4b((byte) 127, (byte) 127, (byte) 127, (byte) 127);
			}

			GL11.glTranslatef(0.0F, 0.0F, -0.01F);
			super.renderHeadUpDisplay(distance, day, shifted, mc, config);

			GL11.glScalef(1F, 1F, 0.0001F);
			String message;
			message = pipe.getSatellitePipeName();
			mc.fontRenderer.drawString(message, -(mc.fontRenderer.getStringWidth(message) / 2), -2, 0);
		}
	}

	public int getMaxPage() {
		int ret = pipe.getItemList().size() / 12;
		if (pipe.getItemList().size() % 12 != 0 || ret == 0) {
			ret++;
		}
		return ret;
	}

	@Override
	public boolean display(IHUDConfig config) {
		return config.isHUDSatellite();
	}

	@Override
	public boolean cursorOnWindow(int x, int y) {
		if (pipe.getItemList().size() > 0) {
			return -50 < x && x < 50 && -50 < y && y < 50;
		} else {
			return -50 < x && x < 50 && -15 < y && y < 20;
		}
	}
}
