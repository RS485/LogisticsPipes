package logisticspipes.proxy.nei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.api.ItemInfo;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.guihook.IContainerTooltipHandler;
import lombok.SneakyThrows;

import logisticspipes.proxy.interfaces.INEIProxy;
import logisticspipes.utils.ReflectionHelper;

public class NEIProxy implements INEIProxy {

	@Override
	public List<String> getInfoForPosition(World world, EntityPlayer player, RayTraceResult objectMouseOver) {
		List<ItemStack> items = ItemInfo.getIdentifierItems(world, player, objectMouseOver);
		if (items.isEmpty()) {
			return new ArrayList<>(0);
		}
		Collections.sort(items, (stack0, stack1) -> stack1.getItemDamage() - stack0.getItemDamage());
		return ItemInfo.getText(items.get(0), world, player, objectMouseOver);
	}

	@Override
	@SideOnly(Side.CLIENT)
	@SneakyThrows({NoSuchFieldException.class, IllegalAccessException.class})
	public boolean renderItemToolTip(int mousex, int mousey, List<String> msg, TextFormatting rarityColor, ItemStack stack) {
		if (!(Minecraft.getMinecraft().currentScreen instanceof GuiContainer)) {
			return false;
		}
		GuiContainer window = (GuiContainer) Minecraft.getMinecraft().currentScreen;
		List<String> tooltip = new LinkedList<String>();
		FontRenderer font = GuiDraw.fontRenderer;

		if (GuiContainerManager.shouldShowTooltip(window)) {
			font = GuiContainerManager.getFontRenderer(stack);
			if (stack != null) {
				tooltip = msg;
			}
			for (IContainerTooltipHandler handler : (List<IContainerTooltipHandler>) ReflectionHelper.getPrivateField(List.class, GuiContainerManager.class, "instanceTooltipHandlers", GuiContainerManager.getManager())) {
				tooltip = handler.handleItemTooltip(window, stack, mousex, mousey, tooltip);
			}
		}
		if (tooltip.size() > 0) {
			tooltip.set(0, tooltip.get(0) + "§h");
		}
		GuiDraw.drawMultilineTip(mousex + 12, mousey - 12, tooltip);
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public List<String> getItemToolTip(ItemStack stack, EntityPlayer thePlayer, ITooltipFlag advancedItemTooltips, GuiContainer screen) {
		return GuiContainerManager.itemDisplayNameMultiline(stack, screen, true);
	}

	@Override
	public ItemStack getItemForPosition(World world, EntityPlayer player, RayTraceResult objectMouseOver) {
		List<ItemStack> items = ItemInfo.getIdentifierItems(world, player, objectMouseOver);
		if (items.isEmpty()) {
			return null;
		}
		Collections.sort(items, (stack0, stack1) -> stack1.getItemDamage() - stack0.getItemDamage());
		return items.get(0);
	}
}
