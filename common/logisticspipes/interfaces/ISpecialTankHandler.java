package logisticspipes.interfaces;

import java.util.List;

import net.minecraft.block.entity.BlockEntity;

public interface ISpecialTankHandler {

	boolean init();

	boolean isType(BlockEntity tile);

	List<BlockEntity> getBaseTilesFor(BlockEntity tile);
}
