package logisticspipes.proxy.interfaces;

import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface INEIProxy {

	ItemStack getItemForPosition(World world, EntityPlayer player, MovingObjectPosition objectMouseOver);

	List<String> getInfoForPosition(World world, EntityPlayer player, MovingObjectPosition objectMouseOver);

	@SideOnly(Side.CLIENT) boolean renderItemToolTip(int posX, int posY, List<String> msg, EnumChatFormatting rarityColor, ItemStack stack);

	@SideOnly(Side.CLIENT) List<String> getItemToolTip(ItemStack var22, EntityPlayer thePlayer, boolean advancedItemTooltips, GuiContainer screen);
}
