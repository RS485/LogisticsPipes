package logisticspipes.gui.hud;

import logisticspipes.logic.BaseLogicSatellite;
import logisticspipes.pipes.PipeItemsSatelliteLogistics;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.hud.BasicHUDButton;
import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

public class HUDSatellite extends BasicHUDGui {
	
	private final PipeItemsSatelliteLogistics pipe;
	private int page;
	
	public HUDSatellite(final PipeItemsSatelliteLogistics pipe) {
		this.pipe = pipe;
		this.addButton(new BasicHUDButton("<", -2, -40, 8, 8) {
			@Override
			public void clicked() {
				if(page > 0) {
					page--;
				}
			}

			@Override
			public boolean shouldRenderButton() {
				return pipe.itemList.size() > 0;
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
				return pipe.itemList.size() > 0;
			}

			@Override
			public boolean buttonEnabled() {
				return page + 1 < getMaxPage();
			}
		});
	}
	
	@Override
	public void renderHeadUpDisplay(double distance, boolean day, Minecraft mc) {
		if(pipe.itemList.size() > 0) {
			if(day) {
	        	GL11.glColor4b((byte)64, (byte)64, (byte)64, (byte)64);
	        } else {
	        	GL11.glColor4b((byte)127, (byte)127, (byte)127, (byte)64);	
	        }
			BasicGuiHelper.drawGuiBackGround(mc, -50, -50, 50, 50, 0);
			if(day) {
	        	GL11.glColor4b((byte)64, (byte)64, (byte)64, (byte)127);
	        } else {
	        	GL11.glColor4b((byte)127, (byte)127, (byte)127, (byte)127);	
	        }


			GL11.glTranslatef(0.0F, 0.0F, -0.01F);
			super.renderHeadUpDisplay(distance, day, mc);
			
			GL11.glScalef(1.5F, 1.5F, 0.0001F);
			String message = "ID: " + Integer.toString(((BaseLogicSatellite)pipe.logic).satelliteId);
			mc.fontRenderer.drawString(message , -28, -28, 0);
			GL11.glScalef(0.8F, 0.8F, -1F);
			BasicGuiHelper.renderItemIdentifierStackListIntoGui(pipe.itemList, null, page, -35, -20, 4, 12, 18, 18, mc, true, true);
			GL11.glScalef(0.8F, 0.8F, -1F);
			message = "(" + Integer.toString(page + 1) + "/" + Integer.toString(getMaxPage()) + ")";
			mc.fontRenderer.drawString(message , 9, -41, 0);
		} else {
			if(day) {
	        	GL11.glColor4b((byte)64, (byte)64, (byte)64, (byte)64);
	        } else {
	        	GL11.glColor4b((byte)127, (byte)127, (byte)127, (byte)64);	
	        }
			BasicGuiHelper.drawGuiBackGround(mc, -30, -30, 30, 30, 0);
			if(day) {
	        	GL11.glColor4b((byte)64, (byte)64, (byte)64, (byte)127);
	        } else {
	        	GL11.glColor4b((byte)127, (byte)127, (byte)127, (byte)127);	
	        }

			GL11.glTranslatef(0.0F, 0.0F, -0.01F);
			super.renderHeadUpDisplay(distance, day, mc);

			GL11.glScalef(3F, 3F, 0.0001F);
			String message = Integer.toString(((BaseLogicSatellite)pipe.logic).satelliteId);
			mc.fontRenderer.drawString(message , -(mc.fontRenderer.getStringWidth(message) / 2), -4, 0);
		}
	}
	
	public int getMaxPage() {
		int ret = pipe.itemList.size() / 12;
		if(pipe.itemList.size() % 12 != 0 || ret == 0) {
			ret++;
		}
		return ret;
	}
	
	@Override
	public boolean display() {
		return true;
	}

	@Override
	public boolean cursorOnWindow(int x, int y) {
		if(pipe.itemList.size() > 0) {
			return -50 < x && x < 50 && -50 < y && y < 50;
		} else {
			return -30 < x && x < 30 && -30 < y && y < 30;
		}
	}

	@Override
	public void handleCursor(int x, int y) {
		super.handleCursor(x, y);
	}
}
