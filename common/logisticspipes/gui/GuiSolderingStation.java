package logisticspipes.gui;

import java.util.LinkedList;
import java.util.List;

import logisticspipes.blocks.LogisticsSolderingTileEntity;
import logisticspipes.utils.gui.LogisticsBaseGuiScreen;
import logisticspipes.utils.item.ItemIdentifier;
import logisticspipes.utils.item.ItemIdentifierStack;
import logisticspipes.utils.item.ItemStackRenderer;
import logisticspipes.utils.item.ItemStackRenderer.DisplayAmount;

import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

public class GuiSolderingStation extends LogisticsBaseGuiScreen {

	private final LogisticsSolderingTileEntity tile;

	public GuiSolderingStation(EntityPlayer player, LogisticsSolderingTileEntity tile) {
		super(176, 166, 0, 0);
		this.tile = tile;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
	}

	private static final ResourceLocation TEXTURE = new ResourceLocation("logisticspipes", "textures/gui/soldering_station.png");

	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(GuiSolderingStation.TEXTURE);
		int j = guiLeft;
		int k = guiTop;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
		ItemStackRenderer.renderItemIdentifierStackListIntoGui(tile.getRecipeForTagetAsItemIdentifierStackList(), null, 0, guiLeft + 44, guiTop + 17, 3, 9, 18, 18, 100.0F, DisplayAmount.NEVER);
		ItemStack resultstack = tile.getTargetForTaget();
		if (resultstack == null) {
			resultstack = tile.getTagetForRecipe(false);
		}
		if (resultstack != null) {
			ItemIdentifierStack iis = ItemIdentifier.get(resultstack).makeStack(0);
			List<ItemIdentifierStack> iisl = new LinkedList<ItemIdentifierStack>();
			iisl.add(iis);
			ItemStackRenderer.renderItemIdentifierStackListIntoGui(iisl, null, 0, guiLeft + 141, guiTop + 47, 1, 1, 18, 18, 100.0F, DisplayAmount.NEVER);
		}
		mc.renderEngine.bindTexture(GuiSolderingStation.TEXTURE);
		GL11.glDisable(2929 /*GL_DEPTH_TEST*/);
		for (int a = 0; a < 3; a++) {
			for (int b = 0; b < 3; b++) {
				Gui.drawRect(guiLeft + 44 + (a * 18), guiTop + 17 + (b * 18), guiLeft + 60 + (a * 18), guiTop + 33 + (b * 18), 0xc08b8b8b);
			}
		}
		Gui.drawRect(guiLeft + 141, guiTop + 47, guiLeft + 157, guiTop + 63, 0xc08b8b8b);
		GL11.glEnable(2929 /*GL_DEPTH_TEST*/);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		int level = 100 - tile.heat;
		drawTexturedModalRect(j + 131, k + 19 + (level * 14 / 100), 176, level * 14 / 100, 14, 14 - (level * 14 / 100));
		int progress = tile.progress;
		if (progress >= 50) {
			drawTexturedModalRect(j + 107, k + 38, 176, 14, 10, 24);
			drawTexturedModalRect(j + 117, k + 38, 186, 14, ((progress - 50) * 26 / 100), 24);
		} else {
			if (progress >= 25) {
				drawTexturedModalRect(j + 107, k + 38, 176, 14, ((progress - 25) * 10 / 25), 24);
			}
			drawTexturedModalRect(j + 114, k + 38, 183, 14, 3, (progress * 17 / 50));
		}
	}
}
