package logisticspipes.gui.hud;

import logisticspipes.hud.HUDConfig;
import logisticspipes.pipes.PipeItemsSatelliteLogistics;
import logisticspipes.pipes.PipeFluidSatellite;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.hud.BasicHUDButton;
import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

public class HUDSatellite extends BasicHUDGui {

	private final PipeItemsSatelliteLogistics pipe1;
	private final PipeFluidSatellite pipe2;
	private int page;

	public HUDSatellite(final PipeItemsSatelliteLogistics pipe) {
		this(pipe, null);
	}
	
	public HUDSatellite(final PipeFluidSatellite pipe) {
		this(null, pipe);
	}
	
	private HUDSatellite(PipeItemsSatelliteLogistics pPipe1, PipeFluidSatellite pPipe2) {
		this.pipe1 = pPipe1;
		this.pipe2 = pPipe2;
		this.addButton(new BasicHUDButton("<", -2, -40, 8, 8) {
			@Override
			public void clicked() {
				if(page > 0) {
					page--;
				}
			}

			@Override
			public boolean shouldRenderButton() {
				if(pipe1 != null) return pipe1.itemList.size() > 0;
				return pipe2.itemList.size() > 0;
			}

			@Override
			public boolean buttonEnabled() {
				return page > 0;
			}
		});
		this.addButton(new BasicHUDButton(">", 37, -40, 8, 8) {
			@Override
			public void clicked() {
				if(page + 1 < getMaxPage()) {
					page++;
				}
			}

			@Override
			public boolean shouldRenderButton() {
				if(pipe1 != null) return pipe1.itemList.size() > 0;
				return pipe2.itemList.size() > 0;
			}

			@Override
			public boolean buttonEnabled() {
				return page + 1 < getMaxPage();
			}
		});
	}
	
	@Override
	public void renderHeadUpDisplay(double distance, boolean day, Minecraft mc, HUDConfig config) {
		if((pipe1 != null && pipe1.itemList.size() > 0) || (pipe2 != null && pipe2.itemList.size() > 0)) {
			if(day) {
	        	GL11.glColor4b((byte)64, (byte)64, (byte)64, (byte)64);
	        } else {
	        	GL11.glColor4b((byte)127, (byte)127, (byte)127, (byte)64);	
	        }
			BasicGuiHelper.drawGuiBackGround(mc, -50, -50, 50, 50, 0, false);
			if(day) {
	        	GL11.glColor4b((byte)64, (byte)64, (byte)64, (byte)127);
	        } else {
	        	GL11.glColor4b((byte)127, (byte)127, (byte)127, (byte)127);	
	        }


			GL11.glTranslatef(0.0F, 0.0F, -0.01F);
			super.renderHeadUpDisplay(distance, day, mc, config);
			
			GL11.glScalef(1.5F, 1.5F, 0.0001F);
			String message;
			if(pipe1 != null) {
				message = "ID: " + Integer.toString(pipe1.satelliteId);
			} else {
				message = "ID: " + Integer.toString(pipe2.satelliteId);
			}
			mc.fontRenderer.drawString(message , -28, -28, 0);
			GL11.glScalef(0.8F, 0.8F, -1F);
			BasicGuiHelper.renderItemIdentifierStackListIntoGui((pipe1 != null ? pipe1.itemList : pipe2.itemList), null, page, -35, -20, 4, 12, 18, 18, mc, true, true, true, true);
			GL11.glScalef(0.8F, 0.8F, -1F);
			message = "(" + Integer.toString(page + 1) + "/" + Integer.toString(getMaxPage()) + ")";
			mc.fontRenderer.drawString(message , 9, -41, 0);
		} else {
			if(day) {
	        	GL11.glColor4b((byte)64, (byte)64, (byte)64, (byte)64);
	        } else {
	        	GL11.glColor4b((byte)127, (byte)127, (byte)127, (byte)64);	
	        }
			BasicGuiHelper.drawGuiBackGround(mc, -30, -30, 30, 30, 0, false);
			if(day) {
	        	GL11.glColor4b((byte)64, (byte)64, (byte)64, (byte)127);
	        } else {
	        	GL11.glColor4b((byte)127, (byte)127, (byte)127, (byte)127);	
	        }

			GL11.glTranslatef(0.0F, 0.0F, -0.01F);
			super.renderHeadUpDisplay(distance, day, mc, config);

			GL11.glScalef(3F, 3F, 0.0001F);
			String message;
			if(pipe1 != null) {
				message = Integer.toString(pipe1.satelliteId);
			} else {
				message = Integer.toString(pipe2.satelliteId);
			}
			mc.fontRenderer.drawString(message , -(mc.fontRenderer.getStringWidth(message) / 2), -4, 0);
		}
	}
	
	public int getMaxPage() {
		if(pipe1 != null) {
			int ret = pipe1.itemList.size() / 12;
			if(pipe1.itemList.size() % 12 != 0 || ret == 0) {
				ret++;
			}
			return ret;
		} else {
			int ret = pipe2.itemList.size() / 12;
			if(pipe2.itemList.size() % 12 != 0 || ret == 0) {
				ret++;
			}
			return ret;
		}
	}
	
	@Override
	public boolean display(HUDConfig config) {
		return config.isHUDSatellite();
	}

	@Override
	public boolean cursorOnWindow(int x, int y) {
		if((pipe1 != null && pipe1.itemList.size() > 0) || (pipe2 != null && pipe2.itemList.size() > 0)) {
			return -50 < x && x < 50 && -50 < y && y < 50;
		} else {
			return -30 < x && x < 30 && -30 < y && y < 30;
		}
	}
}
