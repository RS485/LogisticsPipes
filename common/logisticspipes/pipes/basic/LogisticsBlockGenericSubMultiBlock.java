package logisticspipes.pipes.basic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static logisticspipes.LogisticsPipes.LogisticsPipeBlock;
import static net.minecraft.util.EnumBlockRenderType.ENTITYBLOCK_ANIMATED;

import logisticspipes.proxy.MainProxy;
import network.rs485.logisticspipes.world.DoubleCoordinates;

public class LogisticsBlockGenericSubMultiBlock extends BlockContainer {

	protected final Random rand = new Random();

	public LogisticsBlockGenericSubMultiBlock() {
		super(Material.GLASS);
	}

	@Override
	@Nonnull
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, @Nonnull IBlockState state, int fortune) {
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
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof LogisticsTileGenericSubMultiBlock) {
			boolean handled = false;
			List<LogisticsTileGenericPipe> mainPipeList = ((LogisticsTileGenericSubMultiBlock) tile).getMainPipe();
			for (LogisticsTileGenericPipe mainPipe : mainPipeList) {
				if (mainPipe != null && mainPipe.pipe != null && mainPipe.pipe.isMultiBlock()) {
					if (LogisticsPipeBlock.doRayTrace(state, worldIn, pos, Minecraft.getMinecraft().thePlayer) != null) {
						mainPipe.pipe.getLPPosition().setBlockToAir(worldIn);
						handled = true;
					}
				}
			}
			if (!handled) {
				mainPipeList.stream()
						.filter(mainPipe -> mainPipe != null && mainPipe.pipe != null && mainPipe.pipe.isMultiBlock())
						.forEach(mainPipe -> {
							DoubleCoordinates mainPipePos = mainPipe.pipe.getLPPosition();
							mainPipePos.setBlockToAir(worldIn);
						});
			}
		}
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
	@Nonnull
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return ENTITYBLOCK_ANIMATED;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB entityBox,
			@Nonnull List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn) {
		TileEntity tile = worldIn.getTileEntity(pos);
		if (tile instanceof LogisticsTileGenericSubMultiBlock) {
			List<LogisticsTileGenericPipe> mainPipeList = ((LogisticsTileGenericSubMultiBlock) tile).getMainPipe();
			mainPipeList.stream()
					.filter(Objects::nonNull)
					.filter(mainPipe -> Objects.nonNull(mainPipe.pipe))
					.filter(mainPipe -> mainPipe.pipe.isMultiBlock())
					.forEach(mainPipe -> LogisticsPipeBlock.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn));
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
					.filter(mainPipe -> Objects.nonNull(mainPipe.pipe))
					.filter(mainPipe -> mainPipe.pipe.isMultiBlock())
					.map(mainPipe -> LogisticsPipeBlock.collisionRayTrace(blockState, worldIn, mainPipe.getPos(), start, end))
					.filter(Objects::nonNull)
					.findFirst()
					.map(result -> result.blockPos = pos)
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
					.filter(mainPipe -> Objects.nonNull(mainPipe.pipe))
					.filter(mainPipe -> mainPipe.pipe.isMultiBlock())
					.filter(mainPipe -> Objects.nonNull(LogisticsPipeBlock.doRayTrace(state, worldIn, mainPipe.getPos(), Minecraft.getMinecraft().thePlayer)))
					.map(mainPipe -> LogisticsPipeBlock.getSelectedBoundingBox(state, worldIn, mainPipe.getPos()))
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
	public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
		TileEntity tile = world.getTileEntity(pos);
		Optional<Boolean> result = Optional.empty();
		if (tile instanceof LogisticsTileGenericSubMultiBlock) {
			IBlockState state = tile.getBlockType().getExtendedState(tile.getBlockType().getDefaultState(), world, pos);
			List<LogisticsTileGenericPipe> mainPipeList = ((LogisticsTileGenericSubMultiBlock) tile).getMainPipe();
			result = mainPipeList.stream()
					.filter(Objects::nonNull)
					.filter(mainPipe -> Objects.nonNull(mainPipe.pipe))
					.filter(mainPipe -> mainPipe.pipe.isMultiBlock())
					.filter(mainPipe -> Objects.nonNull(LogisticsPipeBlock.doRayTrace(state, world, mainPipe.getPos(), Minecraft.getMinecraft().thePlayer)))
					.map(mainPipe -> LogisticsPipeBlock.addDestroyEffects(world, mainPipe.getPos(), manager))
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
					.filter(mainPipe -> Objects.nonNull(mainPipe.pipe))
					.filter(mainPipe -> mainPipe.pipe.isMultiBlock())
					.filter(mainPipe -> Objects.nonNull(LogisticsPipeBlock.doRayTrace(state, world, mainPipe.getPos(), player)))
					.map(mainPipe -> LogisticsPipeBlock.getPickBlock(state, target, world, mainPipe.getPos(), player))
					.findFirst();

			// TODO check the following code. Old and may be unnecessary.
			/*
			if (!mainPipeList.isEmpty() && mainPipeList.get(0).pipe != null && mainPipeList.get(0).pipe.isMultiBlock()) {
				return LogisticsPipeBlock
						.getPickBlock(target, world, mainPipeList.get(0).xCoord, mainPipeList.get(0).yCoord, mainPipeList.get(0).zCoord);
			}
			 */
		}
		return result.orElse(super.getPickBlock(state, target, world, pos, player));
	}

	private void addHitEffects(LogisticsTileGenericPipe mainPipe, IBlockState state, World worldObj, RayTraceResult target, ParticleManager manager) {
		final TextureAtlasSprite icon = mainPipe.pipe.getIconProvider().getIcon(mainPipe.pipe.getIconIndexForItem());
		final EnumFacing sideHit = target.sideHit;
		final float b = 0.1F;
		final AxisAlignedBB boundingBox = state.getBoundingBox(worldObj, target.getBlockPos());

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

		EntityDiggingFX fx = new EntityDiggingFX(worldObj, px, py, pz, 0.0D, 0.0D, 0.0D, block, sideHit, worldObj.getBlockMetadata(x, y, z));
		fx.setParticleIcon(icon);
		manager.addEffect(fx.applyColourMultiplier(x, y, z).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
		*/
	}

	@Override
	public boolean addHitEffects(IBlockState state, World worldObj, RayTraceResult target, ParticleManager manager) {
		TileEntity tile = worldObj.getTileEntity(target.getBlockPos());
		if (tile instanceof LogisticsTileGenericSubMultiBlock) {
			List<LogisticsTileGenericPipe> mainPipeList = ((LogisticsTileGenericSubMultiBlock) tile).getMainPipe();
			Optional<LogisticsTileGenericPipe> result = mainPipeList.stream()
					.filter(Objects::nonNull)
					.filter(mainPipe -> Objects.nonNull(mainPipe.pipe))
					.filter(mainPipe -> mainPipe.pipe.isMultiBlock())
					.filter(mainPipe -> Objects.nonNull(LogisticsPipeBlock.doRayTrace(state, worldObj, mainPipe.getPos(), Minecraft.getMinecraft().thePlayer)))
					.findFirst();

			result.ifPresent(mainPipe -> addHitEffects(mainPipe, state, worldObj, target, manager));
			if (result.isPresent()) {
				return true;
			}
		}
		return super.addHitEffects(state, worldObj, target, manager);
	}
}
