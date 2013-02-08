package logisticspipes.gui.hud.modules;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;

import logisticspipes.interfaces.IHUDButton;
import logisticspipes.interfaces.IHUDModuleRenderer;
import logisticspipes.logic.BaseLogicCrafting;
import logisticspipes.modules.ModuleCrafting;
import logisticspipes.utils.ItemIdentifierStack;
import logisticspipes.utils.gui.BasicGuiHelper;

public class HUDCraftingModule implements IHUDModuleRenderer {

	private final ModuleCrafting module;
	
	public HUDCraftingModule(ModuleCrafting moduleCrafting) {
		
		module = moduleCrafting;
	}
	
	@Override
	public void renderContent() {
		GL11.glTranslatef(0.0F, 0.0F, -0.005F);
		GL11.glScalef(1.5F, 1.5F, 0.0001F);
		Minecraft mc = FMLClientHandler.instance().getClient();
		if(module.displayList.size() > 0) {
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
		if(module.getCraftedItem() != null) {
			list.add(module.getCraftedItemStack());
		}
		if(module.displayList.size() > 0) {
			BasicGuiHelper.renderItemIdentifierStackListIntoGui(list, null, 0, 13, -17, 1, 1, 18, 18, mc, true, true, true, true);
			BasicGuiHelper.renderItemIdentifierStackListIntoGui(module.displayList, null, 0, 13, 3, 1, 1, 18, 18, mc, true, true, true, true);
		} else {
			BasicGuiHelper.renderItemIdentifierStackListIntoGui(list, null, 0, -9, 0, 1, 1, 18, 18, mc, true, true, true, true);
		}

	}

	@Override
	public List<IHUDButton> getButtons() {
		// TODO Auto-generated method stub
		return null;
	}

}
