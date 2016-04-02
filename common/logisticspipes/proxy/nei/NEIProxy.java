package logisticspipes.proxy.nei;

import java.util.*;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.guihook.IContainerTooltipHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import logisticspipes.proxy.interfaces.INEIProxy;

import logisticspipes.utils.ReflectionHelper;
import lombok.SneakyThrows;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import codechicken.nei.api.ItemInfo;

public class NEIProxy implements INEIProxy {

	@Override
	public List<String> getInfoForPosition(World world, EntityPlayer player, MovingObjectPosition objectMouseOver) {
		List<ItemStack> items = ItemInfo.getIdentifierItems(world, player, objectMouseOver);
		if (items.isEmpty()) {
			return new ArrayList<String>(0);
		}
		Collections.sort(items, new Comparator<ItemStack>() {

			@Override
			public int compare(ItemStack stack0, ItemStack stack1) {
				return stack1.getItemDamage() - stack0.getItemDamage();
			}
		});
		return ItemInfo.getText(items.get(0), world, player, objectMouseOver);
	}

	@Override
	@SideOnly(Side.CLIENT)
	@SneakyThrows({NoSuchFieldException.class, IllegalAccessException.class})
	public boolean renderItemToolTip(int mousex, int mousey, List<String> msg, EnumChatFormatting rarityColor, ItemStack stack) {
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
			tooltip.set(0, (String) tooltip.get(0) + "§h");
		}
		GuiDraw.drawMultilineTip(font, mousex + 12, mousey - 12, tooltip);
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public List<String> getItemToolTip(ItemStack stack, EntityPlayer thePlayer, boolean advancedItemTooltips, GuiContainer screen) {
		return GuiContainerManager.itemDisplayNameMultiline(stack, screen, true);
	}

	@Override
	public ItemStack getItemForPosition(World world, EntityPlayer player, MovingObjectPosition objectMouseOver) {
		List<ItemStack> items = ItemInfo.getIdentifierItems(world, player, objectMouseOver);
		if (items.isEmpty()) {
			return null;
		}
		Collections.sort(items, new Comparator<ItemStack>() {

			@Override
			public int compare(ItemStack stack0, ItemStack stack1) {
				return stack1.getItemDamage() - stack0.getItemDamage();
			}
		});
		return items.get(0);
	}
}
