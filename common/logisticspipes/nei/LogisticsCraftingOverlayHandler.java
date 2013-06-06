package logisticspipes.nei;

import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.gui.GuiLogisticsCraftingTable;
import logisticspipes.network.PacketHandler;
import logisticspipes.network.packets.NEISetCraftingRecipe;
import logisticspipes.proxy.MainProxy;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import codechicken.nei.PositionedStack;
import codechicken.nei.api.IOverlayHandler;
import codechicken.nei.recipe.IRecipeHandler;
import cpw.mods.fml.client.FMLClientHandler;

public class LogisticsCraftingOverlayHandler implements IOverlayHandler {
	@Override
	public void overlayRecipe(GuiContainer firstGui, IRecipeHandler recipe, int recipeIndex, boolean shift) {
		LogisticsCraftingTableTileEntity c = ((GuiLogisticsCraftingTable)firstGui)._crafter;
		ItemStack[] stack = new ItemStack[9];
		NEISetCraftingRecipe packet = PacketHandler.getPacket(NEISetCraftingRecipe.class);
		for(PositionedStack ps : recipe.getIngredientStacks(recipeIndex)) {
			int x = (ps.relx - 25) / 18;
			int y = (ps.rely - 6) / 18;
			int slot = x + y * 3;
			if(x < 0 || x > 2 || y < 0 || y > 2 || slot < 0 || slot > 8) {
				FMLClientHandler.instance().getClient().thePlayer.sendChatMessage("Internal Error. This button is broken.");
				return;
			}
			if(slot < 9) {
				stack[slot] = ps.items[0];
			}
		}
		MainProxy.sendPacketToServer(packet.setContent(stack).setPosX(c.xCoord).setPosY(c.yCoord).setPosZ(c.zCoord).getPacket());
	}
}
