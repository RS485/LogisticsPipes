package logisticspipes.pipes.basic;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static logisticspipes.LPBlocks.pipe;
import static net.minecraft.util.EnumBlockRenderType.ENTITYBLOCK_ANIMATED;

import logisticspipes.LogisticsPipes;
import logisticspipes.config.Configs;
import logisticspipes.proxy.MainProxy;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public class LogisticsBlockGenericSubMultiBlock extends BlockContainer {

	protected final Random rand = new Random();
	public static boolean redirectedToMainPipe = false;

	public LogisticsBlockGenericSubMultiBlock() {
		super(Material.GLASS);
		setCreativeTab(LogisticsPipes.CREATIVE_TAB_LP);
	}

	@Override
	@Nonnull
	public List<ItemStack> getDrops(IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof LogisticsTileGenericSubMultiBlock) {
			List<LogisticsTileGenericPipe> mainPipeList = ((LogisticsTileGenericSubMultiBlock) tile).getMainPipe();
			return mainPipeList.stream()
					.filter(Objects::nonNull)
					.filter(LogisticsTileGenericPipe::isMultiBlock)
					.map(mainPipe -> pipe.getDrops(world, mainPipe.getPos(), world.getBlockState(mainPipe.getPos()), fortune))
					.flatMap(Collection::stream)
					.collect(Collectors.toCollection(NonNullList::create));
		}
		return Collections.emptyList();
	}

	/*
	@Override
	public TextureAtlasSprite getIcon(int p_149691_1_, int p_149691_2_) {
		return LogisticsPipes.LogisticsPipeBlock.getIcon(p_149691_1_, p_149691_2_);
	}

	@Override
	@SideOnly(Side.CLIENT)
	@SuppressWarnings({ "all" })
	public TextureAtlasSprite getIcon(IBlockAccess iblockaccess, int i, int j, int k, int l) {
		DoubleCoordinates pos = new DoubleCoordinates(i, j, k);
		TileEntity tile = pos.getTileEntity(iblockaccess);
		if (tile instanceof LogisticsTileGenericSubMultiBlock) {
			List<LogisticsTileGenericPipe> mainPipe = ((LogisticsTileGenericSubMultiBlock) tile).getMainPipe();
			if (!mainPipe.isEmpty() && mainPipe.get(0).pipe != null && mainPipe.get(0).pipe.isMultiBlock()) {
				return LogisticsPipes.LogisticsPipeBlock.getIcon(iblockaccess, mainPipe.get(0).xCoord, mainPipe.get(0).yCoord, mainPipe.get(0).zCoord, l);
			}
		}
		return null;
	}
	*/

	public static DoubleCoordinates currentCreatedMultiBlock;

	@Override
	@Nonnull
	public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
		if (LogisticsBlockGenericSubMultiBlock.currentCreatedMultiBlock == null && MainProxy.isServer(worldIn)) {
			new RuntimeException("Unknown MultiBlock controller").printStackTrace();
		}
		return new LogisticsTileGenericSubMultiBlock(LogisticsBlockGenericSubMultiBlock.currentCreatedMultiBlock);
	}

	@Override
	public void breakBlock(World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		if (redirectedToMainPipe) return;
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof LogisticsTileGenericSubMultiBlock) {
			List<LogisticsTileGenericPipe> mainPipeList = ((LogisticsTileGenericSubMultiBlock) tile).getMainPipe();
			mainPipeList.stream()
					.filter(Objects::nonNull)
					.filter(LogisticsTileGenericPipe::isMultiBlock)
					.forEach(mainPipe -> {
						redirectedToMainPipe = true;
						pipe.breakBlock(worldIn, mainPipe.getBlockPos(), worldIn.getBlockState(mainPipe.getBlockPos()));
						redirectedToMainPipe = false;
						worldIn.setBlockToAir(mainPipe.getPos());
					});
		}
	}

	@Override
	public void dropBlockAsItemWithChance(World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, float chance, int fortune) {
		if (world.isRemote) {
			return;
		}
		BlockPos mainPipePos = LogisticsBlockGenericPipe.pipeSubMultiRemoved.get(new DoubleCoordinates(pos));
		if (mainPipePos != null) {
			pipe.dropBlockAsItemWithChance(world, mainPipePos, state, chance, fortune);
		}
	}

	@Override
	public float getBlockHardness(IBlockState state, World par1World, BlockPos pos) {
		return Configs.pipeDurability;
	}

	@Override
	public boolean isNormalCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
		return false;
	}

	@Override
	public boolean canBeReplacedByLeaves(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
		return false;
	}

	@Override
	@Nonnull
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return ENTITYBLOCK_ANIMATED;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World worldIn, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof LogisticsTileGenericSubMultiBlock) {
			List<LogisticsTileGenericPipe> mainPipeList = ((LogisticsTileGenericSubMultiBlock) tile).getMainPipe();
			mainPipeList.stream()
					.filter(Objects::nonNull)
					.filter(LogisticsTileGenericPipe::isMultiBlock)
					.forEach(mainPipe -> pipe.addCollisionBoxToList(mainPipe, entityBox, collidingBoxes, entityIn, isActualState));
		}
	}

	@Override
	@Nullable
	public RayTraceResult collisionRayTrace(IBlockState blockState, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Vec3d start, @Nonnull Vec3d end) {
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof LogisticsTileGenericSubMultiBlock) {
			List<LogisticsTileGenericPipe> mainPipeList = ((LogisticsTileGenericSubMultiBlock) tile).getMainPipe();
			return mainPipeList.stream()
					.filter(Objects::nonNull)
					.filter(LogisticsTileGenericPipe::isMultiBlock)
					.map(mainPipe -> pipe.collisionRayTrace(blockState, worldIn, mainPipe.getPos(), start, end))
					.filter(Objects::nonNull)
					.findFirst()
					.map(it -> {
						RayTraceResult result = new RayTraceResult(it.typeOfHit, it.hitVec, it.sideHit, pos);
						result.subHit = it.subHit;
						result.hitVec = it.hitVec;
						result.hitInfo = it.hitInfo;
						result.entityHit = it.entityHit;
						return result;
					})
					.orElse(null);
		}
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	@Nonnull
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos) {
		TileEntity tile = worldIn.getTileEntity(pos);
		Optional<AxisAlignedBB> result = Optional.empty();
		if (tile instanceof LogisticsTileGenericSubMultiBlock) {
			List<LogisticsTileGenericPipe> mainPipeList = ((LogisticsTileGenericSubMultiBlock) tile).getMainPipe();
			result = mainPipeList.stream()
					.filter(Objects::nonNull)
					.filter(LogisticsTileGenericPipe::isMultiBlock)
					.filter(mainPipe -> Objects.nonNull(pipe.doRayTrace(worldIn, mainPipe.getPos(), Minecraft.getMinecraft().player)))
					.map(mainPipe -> pipe.getSelectedBoundingBox(state, worldIn, mainPipe.getPos()))
					.findFirst();
		}
		return result.orElse(super.getSelectedBoundingBox(state, worldIn, pos).expand(-0.85F, -0.85F, -0.85F));
	}

	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
		super.onNeighborChange(world, pos, neighbor);
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof LogisticsTileGenericSubMultiBlock) {
			((LogisticsTileGenericSubMultiBlock) tile).scheduleNeighborChange();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
		TileEntity tile = world.getTileEntity(pos);
		Optional<Boolean> result = Optional.empty();
		if (tile instanceof LogisticsTileGenericSubMultiBlock) {
			IBlockState state = tile.getBlockType().getExtendedState(tile.getBlockType().getDefaultState(), world, pos);
			List<LogisticsTileGenericPipe> mainPipeList = ((LogisticsTileGenericSubMultiBlock) tile).getMainPipe();
			result = mainPipeList.stream()
					.filter(Objects::nonNull)
					.filter(LogisticsTileGenericPipe::isMultiBlock)
					.filter(mainPipe -> Objects.nonNull(pipe.doRayTrace(world, mainPipe.getPos(), Minecraft.getMinecraft().player)))
					.map(mainPipe -> pipe.addDestroyEffects(world, mainPipe.getPos(), manager))
					.findFirst();
		}
		return result.orElse(super.addDestroyEffects(world, pos, manager));
	}

	@Override
	@Nonnull
	public ItemStack getPickBlock(@Nonnull IBlockState state, RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, EntityPlayer player) {
		TileEntity tile = world.getTileEntity(pos);
		Optional<ItemStack> result = Optional.empty();
		if (tile instanceof LogisticsTileGenericSubMultiBlock) {
			List<LogisticsTileGenericPipe> mainPipeList = ((LogisticsTileGenericSubMultiBlock) tile).getMainPipe();
			result = mainPipeList.stream()
					.filter(Objects::nonNull)
					.filter(LogisticsTileGenericPipe::isMultiBlock)
					.filter(mainPipe -> Objects.nonNull(pipe.doRayTrace(world, mainPipe.getPos(), player)))
					.map(mainPipe -> pipe.getPickBlock(state, target, world, mainPipe.getPos(), player))
					.findFirst();

			if (!result.isPresent()) {
				result = mainPipeList.stream()
						.findFirst()
						.map(mainPipe -> pipe.getPickBlock(state, target, world, mainPipe.getPos(), player));
			}
		}
		return result.orElse(super.getPickBlock(state, target, world, pos, player));
	}

	@SideOnly(Side.CLIENT)
	private void addHitEffects(LogisticsTileGenericPipe mainPipe, IBlockState state, World world, RayTraceResult target, ParticleManager manager) {
		final TextureAtlasSprite icon = mainPipe.pipe.getIconProvider().getIcon(mainPipe.pipe.getIconIndexForItem());
		final EnumFacing sideHit = target.sideHit;
		final float b = 0.1F;
		final AxisAlignedBB boundingBox = state.getBoundingBox(world, target.getBlockPos());

		double px = target.getBlockPos().getX() + rand.nextDouble() * (boundingBox.maxX - boundingBox.minX - (b * 2.0F)) + b + boundingBox.minX;
		double py = target.getBlockPos().getY() + rand.nextDouble() * (boundingBox.maxY - boundingBox.minY - (b * 2.0F)) + b + boundingBox.minY;
		double pz = target.getBlockPos().getZ() + rand.nextDouble() * (boundingBox.maxZ - boundingBox.minZ - (b * 2.0F)) + b + boundingBox.minZ;

		switch (sideHit) {
			case DOWN:
				py = target.getBlockPos().getY() + boundingBox.minY - b;
				break;
			case UP:
				py = target.getBlockPos().getY() + boundingBox.maxY + b;
				break;
			case NORTH:
				pz = target.getBlockPos().getZ() + boundingBox.minZ - b;
				break;
			case SOUTH:
				pz = target.getBlockPos().getZ() + boundingBox.maxZ + b;
				break;
			case WEST:
				px = target.getBlockPos().getX() + boundingBox.minX - b;
				break;
			case EAST:
				px = target.getBlockPos().getX() + boundingBox.maxX + b;
				break;
		}

		// TODO spawn particles with icon
		/*
		particle type: EnumParticleTypes.BLOCK_CRACK

		EntityDiggingFX fx = new EntityDiggingFX(world, px, py, pz, 0.0D, 0.0D, 0.0D, block, sideHit, world.getBlockMetadata(x, y, z));
		fx.setParticleIcon(icon);
		manager.addEffect(fx.applyColourMultiplier(x, y, z).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
		*/
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean addHitEffects(IBlockState state, World world, RayTraceResult target, ParticleManager manager) {
		TileEntity tile = world.getTileEntity(target.getBlockPos());
		if (tile instanceof LogisticsTileGenericSubMultiBlock) {
			List<LogisticsTileGenericPipe> mainPipeList = ((LogisticsTileGenericSubMultiBlock) tile).getMainPipe();
			Optional<LogisticsTileGenericPipe> result = mainPipeList.stream()
					.filter(Objects::nonNull)
					.filter(LogisticsTileGenericPipe::isMultiBlock)
					.filter(mainPipe -> Objects.nonNull(pipe.doRayTrace(world, mainPipe.getPos(), Minecraft.getMinecraft().player)))
					.findFirst();

			result.ifPresent(mainPipe -> addHitEffects(mainPipe, state, world, target, manager));
			if (result.isPresent()) {
				return true;
			}
		}
		return super.addHitEffects(state, world, target, manager);
	}
}
