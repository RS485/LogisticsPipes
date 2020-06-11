package logisticspipes.proxy.interfaces;

import java.util.List;
import javax.annotation.Nonnull;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface INEIProxy {

	@Nonnull
	ItemStack getItemForPosition(World world, EntityPlayer player, RayTraceResult objectMouseOver);

	List<String> getInfoForPosition(World world, EntityPlayer player, RayTraceResult objectMouseOver);

	@SideOnly(Side.CLIENT)
	boolean renderItemToolTip(int posX, int posY, List<String> msg, TextFormatting rarityColor, @Nonnull ItemStack stack);

	@SideOnly(Side.CLIENT)
	List<String> getItemToolTip(@Nonnull ItemStack var22, EntityPlayer thePlayer, ITooltipFlag advancedItemTooltips, GuiContainer screen);
}
