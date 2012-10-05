package logisticspipes.gui;

import logisticspipes.blocks.LogisticsSolderingTileEntity;
import logisticspipes.network.GuiIDs;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.KraphtBaseGuiScreen;
import net.minecraft.src.EntityPlayer;

import org.lwjgl.opengl.GL11;

public class GuiSolderingStation extends KraphtBaseGuiScreen {

	private final EntityPlayer player;
	private final LogisticsSolderingTileEntity tile;
	
	public GuiSolderingStation(EntityPlayer player, LogisticsSolderingTileEntity tile) {
		super(176, 166, 0, 0);
		this.inventorySlots = tile.createContainer(player);
		this.player = player;
		this.tile = tile;
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Soldering_Station;
	}

	@Override
	protected void drawGuiContainerForegroundLayer() {
		super.drawGuiContainerForegroundLayer();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		int i = mc.renderEngine.getTexture("/logisticspipes/gui/soldering_station.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(i);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
		BasicGuiHelper.renderItemIdentifierStackListIntoGui(tile.getRecipeForTagetAsItemIdentifierStackList(), null, 0, guiLeft + 30, guiTop + 17, 3, 9, 18, 18, mc, false, false);
		mc.renderEngine.bindTexture(i);
        for(int a=0;a<3;a++) {
			for(int b=0;b<3;b++) {
				drawRect(guiLeft + 30 + (a * 18), guiTop + 17 + (b * 18), guiLeft + 46 + (a * 18), guiTop + 33 + (b * 18), 0xcc555555);
			}
		}
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		int level = 100 - tile.heat;
		drawTexturedModalRect(j + 117, k + 19 + (level * 14 / 100), 176, level * 14 / 100, 14, 14 - (level * 14 / 100));
		int progress = tile.progress;
		if(progress >= 50) {
			drawTexturedModalRect(j + 93, k + 38, 176, 14, 10, 24);				
			drawTexturedModalRect(j + 103, k + 38, 186, 14, ((progress - 50) * 26 / 100), 24);				
		} else {
			if(progress >= 25) {
				drawTexturedModalRect(j + 93, k + 38, 176, 14, ((progress - 25) * 10 / 25), 24);	
			}
			drawTexturedModalRect(j + 100, k + 38, 183, 14, 3, (progress * 17 / 50));
		}
	}
}
