package logisticspipes.gui.hud;

import java.util.ArrayList;
import java.util.List;

import logisticspipes.logic.BaseLogicCrafting;
import logisticspipes.pipes.PipeItemsCraftingLogistics;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.gui.BasicGuiHelper;
import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

public class HUDCrafting extends BasicHUDGui {
	
	private final PipeItemsCraftingLogistics pipe;
	
	public HUDCrafting(PipeItemsCraftingLogistics pipe) {
		this.pipe = pipe;
	}
	
	@Override
	public void renderHeadUpDisplay(double d, boolean day, Minecraft mc) {
		if(day) {
        	GL11.glColor4b((byte)64, (byte)64, (byte)64, (byte)64);
        } else {
        	GL11.glColor4b((byte)127, (byte)127, (byte)127, (byte)64);	
        }
		if(pipe.displayList.size() > 0) {
			BasicGuiHelper.drawGuiBackGround(mc, -50, -28, 50, 30, 0, false);
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

		if(pipe.displayList.size() > 0) {
			String message = "Result:";
			mc.fontRenderer.drawString(message , -28, -10, 0);
			message = "Todo:";
			mc.fontRenderer.drawString(message , -28, 5, 0);
		} else {
			String message = "Result:";
			mc.fontRenderer.drawString(message , -16, -10, 0);
		}
		GL11.glScalef(0.8F, 0.8F, -1F);
		List<ItemIdentifierStack> list = new ArrayList<ItemIdentifierStack>();
		if(((BaseLogicCrafting)pipe.logic).getCraftedItem() != null) {
			list.add(ItemIdentifierStack.GetFromStack(((BaseLogicCrafting)pipe.logic).getCraftedItem()));
		}
		if(pipe.displayList.size() > 0) {
			BasicGuiHelper.renderItemIdentifierStackListIntoGui(list, null, 0, 13, -17, 1, 1, 18, 18, mc, true, true);
			BasicGuiHelper.renderItemIdentifierStackListIntoGui(pipe.displayList, null, 0, 13, 3, 1, 1, 18, 18, mc, true, true);
		} else {
			BasicGuiHelper.renderItemIdentifierStackListIntoGui(list, null, 0, -9, 0, 1, 1, 18, 18, mc, true, true);
		}
	}

	@Override
	public boolean display() {
		return (pipe.canRegisterSign() && ((BaseLogicCrafting)pipe.logic).getCraftedItem() != null) || pipe.hasOrder();
	}


	@Override
	public boolean cursorOnWindow(int x, int y) {
		return -50 < x && x < 50 && -50 < y && y < 50;
	}

	@Override
	public void handleCursor(int x, int y) {
		super.handleCursor(x, y);
	}
}
