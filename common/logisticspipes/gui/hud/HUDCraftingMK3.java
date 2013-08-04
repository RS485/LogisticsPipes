package logisticspipes.gui.hud;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.hud.HUDConfig;
import logisticspipes.pipes.PipeItemsCraftingLogisticsMk3;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.gui.BasicGuiHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

public class HUDCraftingMK3 extends BasicHUDGui {

	private final PipeItemsCraftingLogisticsMk3 pipe;
	
	public HUDCraftingMK3(PipeItemsCraftingLogisticsMk3 pipe) {
		this.pipe = pipe;
	}
	
	@Override
	public void renderHeadUpDisplay(double d, boolean day, Minecraft mc, HUDConfig config) {
		int bufferSize = (pipe.bufferList.size() / 4) + 1;
		if(pipe.bufferList.size() % 4 == 0) {
			bufferSize--;
		}
		if(day) {
        	GL11.glColor4b((byte)64, (byte)64, (byte)64, (byte)64);
        } else {
        	GL11.glColor4b((byte)127, (byte)127, (byte)127, (byte)64);	
        }
		if(pipe.displayList.size() > 0 && pipe.bufferList.size() == 0) {
			BasicGuiHelper.drawGuiBackGround(mc, -50, -28, 50, 30, 0, false);
		} else if(pipe.bufferList.size() > 0) {
			BasicGuiHelper.drawGuiBackGround(mc, -50, -50, 50, bufferSize * 20 + 10, 0, false);
		} else {
			BasicGuiHelper.drawGuiBackGround(mc, -30, -22, 30, 25, 0, false);
		}
		if(day) {
        	GL11.glColor4b((byte)64, (byte)64, (byte)64, (byte)127);
        } else {
        	GL11.glColor4b((byte)127, (byte)127, (byte)127, (byte)127);	
        }

		GL11.glTranslatef(0.0F, 0.0F, -0.005F);
		GL11.glScalef(1.5F, 1.5F, 0.0001F);

		if(pipe.displayList.size() > 0 && pipe.bufferList.size() == 0) {
			String message = "Result:";
			mc.fontRenderer.drawString(message , -28, -10, 0);
			message = "Todo:";
			mc.fontRenderer.drawString(message , -28, 5, 0);
		} else if(pipe.bufferList.size() > 0) {
			String message = "Result:";
			mc.fontRenderer.drawString(message , -28, -28, 0);
			message = "Todo:";
			mc.fontRenderer.drawString(message , -28, -15, 0);	
		} else {
			String message = "Result:";
			mc.fontRenderer.drawString(message , -16, -10, 0);
		}
		GL11.glScalef(0.8F, 0.8F, -1F);
		List<ItemIdentifierStack> list = new ArrayList<ItemIdentifierStack>();
		List<ItemIdentifierStack> craftables = pipe.getCraftedItems();
		if( craftables != null && craftables.size() > 0) {
			//TODO: handle multiple crafables.
			list.add(craftables.get(0));
		}
		if(pipe.displayList.size() > 0 && pipe.bufferList.size() == 0) {
			BasicGuiHelper.renderItemIdentifierStackListIntoGui(list, null, 0, 13, -17, 1, 1, 18, 18, mc, true, true, true, true);
			BasicGuiHelper.renderItemIdentifierStackListIntoGui(pipe.displayList, null, 0, 13, 3, 1, 1, 18, 18, mc, true, true, true, true);
		} else if(pipe.bufferList.size() > 0) {
			BasicGuiHelper.renderItemIdentifierStackListIntoGui(list, null, 0, 13, -37, 1, 1, 18, 18, mc, true, true, true, true);
			BasicGuiHelper.renderItemIdentifierStackListIntoGui(pipe.displayList, null, 0, 13, -17, 1, 1, 18, 18, mc, true, true, true, true);
			
			BasicGuiHelper.renderItemIdentifierStackListIntoGui(pipe.bufferList, null, 0, -35, 0, 4, 16, 18, 18, mc, true, true, true, true);
		} else {
			BasicGuiHelper.renderItemIdentifierStackListIntoGui(list, null, 0, -9, 0, 1, 1, 18, 18, mc, true, true, true, true);
		}
	}

	@Override
	public boolean display(HUDConfig config) {
		return config.isHUDCrafting() && ((pipe.getCraftingSigns().isEmpty() && pipe.getCraftedItems() != null) || pipe.bufferList.size() > 0 || pipe.displayList.size() > 0);
	}


	@Override
	public boolean cursorOnWindow(int x, int y) {
		int bufferSize = (pipe.bufferList.size() / 4) + 1;
		if(pipe.bufferList.size() % 4 == 0) {
			bufferSize--;
		}
		if(pipe.displayList.size() > 0 && pipe.bufferList.size() == 0) {
			return -50 < x && x < 50 && -28 < y && y < 30;
		} else if(pipe.bufferList.size() > 0) {
			return -50 < x && x < 50 && -50 < y && y < bufferSize * 20 + 10;
		} else {
			return -30 < x && x < 30 && -22 < y && y < 25;
		}
	}
}
