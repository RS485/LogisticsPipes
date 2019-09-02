package logisticspipes.items;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import logisticspipes.interfaces.IItemAdvancedExistance;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.string.StringUtils;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;

public class LogisticsItemCard extends LogisticsItem implements IItemAdvancedExistance {

	public static final int FREQ_CARD = 0;
	public static final int SEC_CARD = 1;

	public LogisticsItemCard() {
		hasSubtypes = true;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, tooltip, flagIn);
		if (!stack.hasTagCompound()) {
			tooltip.add(StringUtils.translate("tooltip.logisticsItemCard"));
		} else {
			if (stack.getTagCompound().hasKey("UUID")) {
				if (stack.getItemDamage() == LogisticsItemCard.FREQ_CARD) {
					tooltip.add("Freq. Card");
				} else if (stack.getItemDamage() == LogisticsItemCard.SEC_CARD) {
					tooltip.add("Sec. Card");
				}
				if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
					tooltip.add("Id: " + stack.getTagCompound().getString("UUID"));
					if (stack.getItemDamage() == LogisticsItemCard.SEC_CARD) {
						UUID id = UUID.fromString(stack.getTagCompound().getString("UUID"));
						tooltip.add("Authorization: " + (SimpleServiceLocator.securityStationManager.isAuthorized(id) ? "Authorized" : "Deauthorized"));
					}
				}
			}
		}
	}

	@Override
	public boolean getShareTag() {
		return true;
	}

	@Override
	public int getItemStackLimit() {
		return 64;
	}

	@Override
	public boolean canExistInNormalInventory(ItemStack stack) {
		return true;
	}

	@Override
	public boolean canExistInWorld(ItemStack stack) {
		return stack.getItemDamage() != LogisticsItemCard.SEC_CARD;
	}
}
