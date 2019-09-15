package logisticspipes.interfaces;

import net.minecraft.block.entity.BlockEntity;

public interface ISpecialTankUtil extends ITankUtil {

	BlockEntity getBlockEntity();

	ISpecialTankAccessHandler getSpecialHandler();
}
