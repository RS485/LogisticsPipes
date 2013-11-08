package logisticspipes.gui.hud.modules;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.interfaces.IHUDButton;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.modules.ModuleAdvancedExtractor;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.hud.BasicHUDButton;
import logisticspipes.utils.item.ItemIdentifierStack;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;

public class HUDAdvancedExtractor implements IHUDModuleRenderer {
	
	private List<IHUDButton> buttons = new ArrayList<IHUDButton>();
	private int selected = 0;
	
	private ModuleAdvancedExtractor module;
	
	public HUDAdvancedExtractor(ModuleAdvancedExtractor moduleAdvancedExtractor) {
		this.module = moduleAdvancedExtractor;
		this.buttons.add(new TabButton("Side",0,-30,-50,25,10));
		this.buttons.add(new TabButton("Inv",1,-5,-50,25,10));
	}

	@Override
	public void renderContent() {
		if(selected == 0) {
			Minecraft mc = FMLClientHandler.instance().getClient();
			ForgeDirection d = module.getSneakyDirection();
			mc.fontRenderer.drawString("Extract" , -22, -22, 0);
			mc.fontRenderer.drawString("from:" , -22, -9, 0);
			mc.fontRenderer.drawString(((d == ForgeDirection.UNKNOWN) ? "DEFAULT" : d.name()) , -22, 18, 0);
		} else {
			Minecraft mc = FMLClientHandler.instance().getClient();
			GL11.glScalef(1.0F, 1.0F, -0.00001F);
			BasicGuiHelper.renderItemIdentifierStackListIntoGui(ItemIdentifierStack.getListFromInventory(module.getFilterInventory()), null, 0, -25, -32, 3, 9, 18, 18, mc, false, false, true, true);
			GL11.glScalef(1.0F, 1.0F, 1 / -0.00001F);
			if(module.areItemsIncluded()) {
				mc.fontRenderer.drawString("Included" , -22, 25, 0);
			} else {
				mc.fontRenderer.drawString("Excluded" , -22, 25, 0);
			}
		}
	}

	@Override
	public List<IHUDButton> getButtons() {
		return buttons;
	}
	
	private class TabButton extends BasicHUDButton {
		
		private final int mode;
		
		public TabButton(String name, int mode, int x, int y, int width, int heigth) {
			super(name, x, y, width, heigth);
			this.mode = mode;
		}

		@Override
		public void clicked() {
			selected = mode;
		}

		@Override
		public void renderButton(boolean hover, boolean clicked) {
			GL11.glTranslatef(0.0F, 0.0F, -0.000005F);
			Minecraft mc = FMLClientHandler.instance().getClient();
			if(hover) {
				GL11.glColor4b((byte)127, (byte)127, (byte)127, (byte)127);
				if(!clicked) {
					GL11.glTranslatef(0.0F, 0.0F, -0.01F);
				}
			} else {
				GL11.glColor4b((byte)127, (byte)127, (byte)127, (byte)64);
			}
			GL11.glScaled(0.5D, 0.5D, 1.0D);
			BasicGuiHelper.drawGuiBackGround(mc, posX * 2, posY * 2, (posX + sizeX) * 2, (posY + sizeY) * 2 + 15, 0, false, true, true, false, true);
			GL11.glScaled(2.0D, 2.0D, 1.0D);

			if(clicked) {
				GL11.glTranslatef(0.0F, 0.0F, -0.01F);
			}

			GL11.glTranslatef(0.0F, 0.0F, -0.000005F);
			int color = 0;
	        if(hover && !clicked) {
	        	color = 0xffffa0;
	        } else if(!clicked) {
	        	color = 0x000000;
	        } else  {
	        	color = 0x808080;
	        }
	        GL11.glScaled(0.8D, 0.8D, 1.0D);
	        mc.fontRenderer.drawString(label ,(int) ((-(mc.fontRenderer.getStringWidth(label) / (2* (1/0.8D))) + posX + sizeX / 2) * (1/0.8D)),(int) ((posY + (sizeY - 8) / 2) * (1/0.8D)) + 2, color);
	        GL11.glScaled(1/0.8D, 1/0.8D, 1.0D);
			if(hover) {
				GL11.glTranslatef(0.0F, 0.0F, 0.01F);
			}
			GL11.glTranslatef(0.0F, 0.0F, 0.00001F);
		}

		@Override
		public boolean shouldRenderButton() {
			return true;
		}

		@Override
		public boolean buttonEnabled() {
			return mode != selected;
		}
	}
}
