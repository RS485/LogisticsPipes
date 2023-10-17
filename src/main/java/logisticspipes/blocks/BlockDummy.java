package logisticspipes.blocks;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import logisticspipes.items.LogisticsSolidBlockItem;

/**
 * A dummy block used for backwards compat to before the {@link LogisticsSolidBlock} split.
 * TODO remove in 1.13
 */
public class BlockDummy extends Block {

	public static final PropertyInteger PROP_BLOCKTYPE = PropertyInteger.create("block_sub_type", 0, 11);

	static final Map<Integer, LogisticsSolidBlock> updateBlockMap = new HashMap<>();
	public static final Map<Integer, LogisticsSolidBlockItem> updateItemMap = new HashMap<>();

	public BlockDummy() {
		super(Material.IRON);
		setBlockUnbreakable();
	}

	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, PROP_BLOCKTYPE);
	}

	@Nonnull
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(PROP_BLOCKTYPE, meta);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(PROP_BLOCKTYPE);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {}

}
