package logisticspipes.blocks;

import logisticspipes.LogisticsPipes;
import logisticspipes.blocks.crafting.LogisticsCraftingTableTileEntity;
import logisticspipes.blocks.powertile.LogisticsIC2PowerProviderTileEntity;
import logisticspipes.blocks.powertile.LogisticsPowerJunctionTileEntity;
import logisticspipes.blocks.powertile.LogisticsRFPowerProviderTileEntity;
import logisticspipes.blocks.stats.LogisticsStatisticsTileEntity;
import logisticspipes.interfaces.IGuiTileEntity;
import logisticspipes.interfaces.IRotationProvider;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;
import logisticspipes.proxy.MainProxy;
import logisticspipes.utils.StreamHelper;
import lombok.Getter;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LogisticsSolidBlock extends BlockContainer {

	public static final PropertyEnum<BlockType> metaProperty = PropertyEnum.create("block_sub_type", BlockType.class);
	public static final PropertyInteger rotationProperty = PropertyInteger.create("rotation", 0, 3);
	public static final PropertyBool active = PropertyBool.create("active");
	public static final Map<EnumFacing, PropertyBool> connectionPropertys = Arrays.stream(EnumFacing.values()).collect(Collectors.toMap(key -> key, key -> PropertyBool.create("connection_" + key.ordinal())));

	public enum BlockType implements IStringSerializable {
		SOLDERING_STATION("soldering_station", 0, true),
		LOGISTICS_POWER_JUNCTION("power_junction", 1),
		LOGISTICS_SECURITY_STATION("security_station", 2),
		LOGISTICS_AUTOCRAFTING_TABLE("crafting_table", 3),
		LOGISTICS_FUZZYCRAFTING_TABLE("fuzzy_crafting_table", 4),
		LOGISTICS_STATISTICS_TABLE("statistics_table", 5),

		//Power Provider
		LOGISTICS_RF_POWERPROVIDER("power_provider_rf", 10),
		LOGISTICS_IC2_POWERPROVIDER("power_provider_eu", 11),
		LOGISTICS_BC_POWERPROVIDER("power_provider_mj", 12),

		LOGISTICS_PROGRAM_COMPILER("program_compiler", 14),

		LOGISTICS_BLOCK_FRAME("frame", 15);

		@Getter
		String name;

		@Getter
		int meta;

		@Getter
		boolean hasActiveTexture;

		BlockType(String name, int meta) {
			this(name, meta, false);
		}

		BlockType(String name, int meta, boolean hasActiveTexture) {
			this.name = name;
			this.meta = meta;
			this.hasActiveTexture = hasActiveTexture;
		}

		public static BlockType getForMeta(int meta) {
			return Arrays.stream(values()).filter(value -> value.meta == meta).collect(StreamHelper.singletonCollector());
		}

		public static BlockType getForName(String name) {
			return Arrays.stream(values()).filter(value -> value.name.equals(name)).collect(StreamHelper.singletonCollector());
		}
	}

	@Override
	public boolean isSideSolid(IBlockState base_state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, EnumFacing side) {
		return true;
	}

	public LogisticsSolidBlock() {
		super(Material.IRON);
		setHardness(6.0F);
		IBlockState state = this.blockState.getBaseState()
			.withProperty(metaProperty, BlockType.SOLDERING_STATION)
			.withProperty(rotationProperty, 0)
			.withProperty(active, false);
		connectionPropertys.values().forEach(it -> state.withProperty(it, false));
		setDefaultState(state);
		setCreativeTab(LogisticsPipes.CREATIVE_TAB_LP);
	}

	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neigbour) {
		super.onNeighborChange(world, pos, neigbour);
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof LogisticsSolidTileEntity) {
			((LogisticsSolidTileEntity) tile).notifyOfBlockChange();
		}
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!playerIn.isSneaking()) {
			TileEntity tile = worldIn.getTileEntity(pos);
			if (tile instanceof IGuiTileEntity) {
				if (MainProxy.isServer(playerIn.world)) {
					((IGuiTileEntity) tile).getGuiProvider().setTilePos(tile).open(playerIn);
				}
				return true;
			}
		}
		return false;
	}

	@Override
	@Nonnull
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(world, pos, state, placer, stack);
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof LogisticsCraftingTableTileEntity) {
			((LogisticsCraftingTableTileEntity) tile).placedBy(placer);
		}
		if (tile instanceof IRotationProvider) {
			double x = tile.getPos().getX() + 0.5 - placer.posX;
			double z = tile.getPos().getZ() + 0.5 - placer.posZ;
			// x and z is correct, atan2's first parameter is named y though
			@SuppressWarnings("SuspiciousNameCombination")
			double w = Math.atan2(x, z);
			double halfPI = Math.PI / 2;
			double halfhalfPI = halfPI / 2;
			w -= halfhalfPI;
			if (w < 0) {
				w += 2 * Math.PI;
			}
			if (0 < w && w <= halfPI) {
				((IRotationProvider) tile).setRotation(1);
			} else if (halfPI < w && w <= 2 * halfPI) {
				((IRotationProvider) tile).setRotation(2);
			} else if (2 * halfPI < w && w <= 3 * halfPI) {
				((IRotationProvider) tile).setRotation(0);
			} else if (3 * halfPI < w && w <= 4 * halfPI) {
				((IRotationProvider) tile).setRotation(3);
			}
		}
	}

	@Override
	public void breakBlock(World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof LogisticsSolderingTileEntity) {
			((LogisticsSolderingTileEntity) tile).onBlockBreak();
		}
		if (tile instanceof LogisticsCraftingTableTileEntity) {
			((LogisticsCraftingTableTileEntity) tile).onBlockBreak();
		}
		if (tile instanceof LogisticsProgramCompilerTileEntity) {
			((LogisticsProgramCompilerTileEntity) tile).onBlockBreak();
		}
		super.breakBlock(worldIn, pos, state);
	}

	@Override
	@Nonnull
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		switch (BlockType.getForMeta(meta)) {
			case SOLDERING_STATION:
				return new LogisticsSolderingTileEntity();
			case LOGISTICS_POWER_JUNCTION:
				return new LogisticsPowerJunctionTileEntity();
			case LOGISTICS_SECURITY_STATION:
				return new LogisticsSecurityTileEntity();
			case LOGISTICS_AUTOCRAFTING_TABLE:
			case LOGISTICS_FUZZYCRAFTING_TABLE:
				return new LogisticsCraftingTableTileEntity();
			case LOGISTICS_STATISTICS_TABLE:
				return new LogisticsStatisticsTileEntity();
			case LOGISTICS_RF_POWERPROVIDER:
				return new LogisticsRFPowerProviderTileEntity();
			case LOGISTICS_IC2_POWERPROVIDER:
				return new LogisticsIC2PowerProviderTileEntity();
			case LOGISTICS_BC_POWERPROVIDER:
				return null;//new LogisticsBCPowerProvider();
			case LOGISTICS_PROGRAM_COMPILER:
				return new LogisticsProgramCompilerTileEntity();
			case LOGISTICS_BLOCK_FRAME:
				return null;
			default:
				throw new IllegalArgumentException("Undefined meta");
		}
	}

	@Override
	public int damageDropped(IBlockState state) {
		switch (state.getValue(metaProperty)) {
			case SOLDERING_STATION:
			case LOGISTICS_POWER_JUNCTION:
			case LOGISTICS_SECURITY_STATION:
			case LOGISTICS_AUTOCRAFTING_TABLE:
			case LOGISTICS_FUZZYCRAFTING_TABLE:
			case LOGISTICS_STATISTICS_TABLE:
			case LOGISTICS_RF_POWERPROVIDER:
			case LOGISTICS_IC2_POWERPROVIDER:
			case LOGISTICS_BC_POWERPROVIDER:
			case LOGISTICS_PROGRAM_COMPILER:
			case LOGISTICS_BLOCK_FRAME:
				return state.getValue(metaProperty).meta;
		}
		return super.damageDropped(state);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		List<IProperty<?>> list = new ArrayList<>();
		list.add(metaProperty);
		list.add(rotationProperty);
		list.add(active);
		list.addAll(connectionPropertys.values());
		IProperty<?>[] props = list.toArray(new IProperty<?>[list.size()]);
		return new BlockStateContainer(this, props);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(metaProperty, BlockType.getForMeta(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(metaProperty).meta;
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		state = super.getActualState(state, worldIn, pos);
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof LogisticsSolidTileEntity) {
			int rotation = ((LogisticsSolidTileEntity) tile).getRotation();
			state = state.withProperty(rotationProperty, rotation);
			state = state.withProperty(active, ((LogisticsSolidTileEntity) tile).isActive());
		}

		if (tile != null) {
			for (EnumFacing side : EnumFacing.VALUES) {
				boolean render = true;
				TileEntity sideTile = worldIn.getTileEntity(pos.offset(side));
				if (sideTile instanceof LogisticsTileGenericPipe) {
					LogisticsTileGenericPipe tilePipe = (LogisticsTileGenericPipe) sideTile;
					if (tilePipe.renderState.pipeConnectionMatrix.isConnected(side.getOpposite())) {
						render = false;
					}
				}
				state = state.withProperty(connectionPropertys.get(side), render);
			}
		}

		return state;
	}
}
