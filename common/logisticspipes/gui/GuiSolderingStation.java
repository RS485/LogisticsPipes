package logisticspipes.gui;

import java.util.LinkedList;
import java.util.List;

import logisticspipes.blocks.LogisticsSolderingTileEntity;
import logisticspipes.network.GuiIDs;
import logisticspipes.utils.ItemIdentifier;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.gui.BasicGuiHelper;
import logisticspipes.utils.gui.KraphtBaseGuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class GuiSolderingStation extends KraphtBaseGuiScreen {

	private final LogisticsSolderingTileEntity tile;
	
	public GuiSolderingStation(EntityPlayer player, LogisticsSolderingTileEntity tile) {
		super(176, 166, 0, 0);
		this.inventorySlots = tile.createContainer(player);
		this.tile = tile;
	}

	@Override
	public int getGuiID() {
		return GuiIDs.GUI_Soldering_Station_ID;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
	}
	
	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/soldering_station.png");
	

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.func_110577_a(TEXTURE);
		int j = guiLeft;
		int k = guiTop;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
		BasicGuiHelper.renderItemIdentifierStackListIntoGui(tile.getRecipeForTagetAsItemIdentifierStackList(), null, 0, guiLeft + 44, guiTop + 17, 3, 9, 18, 18, mc, false, false);
		ItemStack resultstack = tile.getTargetForTaget();
		if(resultstack == null) {
			resultstack = tile.getTagetForRecipe(false);
		}
		if(resultstack != null) {
			ItemIdentifierStack iis = ItemIdentifier.get(resultstack).makeStack(0);
			List<ItemIdentifierStack> iisl = new LinkedList<ItemIdentifierStack>();
			iisl.add(iis);
			BasicGuiHelper.renderItemIdentifierStackListIntoGui(iisl, null, 0, guiLeft + 141, guiTop + 47, 1, 1, 18, 18, mc, false, false);
		}
		mc.renderEngine.func_110577_a(TEXTURE);
		GL11.glDisable(2929 /*GL_DEPTH_TEST*/);
		for(int a=0;a<3;a++) {
			for(int b=0;b<3;b++) {
				drawRect(guiLeft + 44 + (a * 18), guiTop + 17 + (b * 18), guiLeft + 60 + (a * 18), guiTop + 33 + (b * 18), 0xc08b8b8b);
			}
		}
		drawRect(guiLeft + 141, guiTop + 47, guiLeft + 157, guiTop + 63, 0xc08b8b8b);
		GL11.glEnable(2929 /*GL_DEPTH_TEST*/);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		int level = 100 - tile.heat;
		drawTexturedModalRect(j + 131, k + 19 + (level * 14 / 100), 176, level * 14 / 100, 14, 14 - (level * 14 / 100));
		int progress = tile.progress;
		if(progress >= 50) {
			drawTexturedModalRect(j + 107, k + 38, 176, 14, 10, 24);
			drawTexturedModalRect(j + 117, k + 38, 186, 14, ((progress - 50) * 26 / 100), 24);
		} else {
			if(progress >= 25) {
				drawTexturedModalRect(j + 107, k + 38, 176, 14, ((progress - 25) * 10 / 25), 24);
			}
			drawTexturedModalRect(j + 114, k + 38, 183, 14, 3, (progress * 17 / 50));
		}
	}
}
