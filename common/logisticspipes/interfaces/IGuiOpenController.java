package logisticspipes.interfaces;

import net.minecraft.entity.player.PlayerEntity;

public interface IGuiOpenController {

	void guiOpenedByPlayer(PlayerEntity player);

	void guiClosedByPlayer(PlayerEntity player);

}
