package logisticspipes.items;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import logisticspipes.interfaces.ItemAdvancedExistence;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.utils.string.StringUtils;

public class LogisticsItemCard extends LogisticsItem implements ItemAdvancedExistence {

	private final Type type;

	public LogisticsItemCard(Settings settings, Type type) {
		super(settings);
		this.type = type;
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext ctx) {
		super.appendTooltip(stack, world, tooltip, ctx);
		if (!stack.hasTag()) {
			tooltip.add(new LiteralText(StringUtils.translate("tooltip.logisticsItemCard")));
		} else {
			if (stack.getTag().hasUuid("UUID")) {
				if (type == Type.FREQ_CARD) {
					tooltip.add(new TranslatableText("item.logisticspipes.frequency_card.shortname"));
				} else if (type == Type.SEC_CARD) {
					tooltip.add(new TranslatableText("item.logisticspipes.security_card.shortname"));
				}
				if (Screen.hasShiftDown()) {
					tooltip.add(new LiteralText("Id: " + stack.getTag().getUuid("UUID")));
					if (type == Type.SEC_CARD) {
						UUID id = stack.getTag().getUuid("UUID");
						tooltip.add(new TranslatableText(SimpleServiceLocator.securityStationManager.isAuthorized(id) ? "tooltip.logisticspipes.item_card_authorized" : "tooltip.logisticspipes.item_card_deauthorized"));
					}
				}
			}
		}
	}

	// TODO 1.15: share tag
	// @Override
	// public boolean getShareTag() {
	// 	return true;
	// }

	@Override
	public boolean canExistInNormalInventory(@NotNull ItemStack stack) {
		return true;
	}

	@Override
	public boolean canExistInWorld(@NotNull ItemStack stack) {
		return type != Type.SEC_CARD;
	}

	public enum Type {
		FREQ_CARD,
		SEC_CARD
	}

}
