package logisticspipes.pipes.basic.ltgpmodcompat;

import java.util.Optional;
import java.util.Random;
import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import mcmultipart.MCMultiPart;
import mcmultipart.RayTraceHelper;
import mcmultipart.api.container.IMultipartContainerBlock;
import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.slot.IPartSlot;
import static mcmultipart.block.BlockMultipartContainer.getTile;
import mcmultipart.block.TileMultipartContainer;
import org.apache.commons.lang3.tuple.Pair;

import logisticspipes.LPConstants;
import logisticspipes.asm.ModDependentInterface;
import logisticspipes.asm.ModDependentMethod;
import logisticspipes.proxy.SimpleServiceLocator;
import network.rs485.logisticspipes.proxy.mcmp.subproxy.IMCMPBlockAccess;

@ModDependentInterface(modId = { LPConstants.mcmpModID }, interfacePath = { "mcmultipart.api.container.IMultipartContainerBlock" })
public abstract class LPMicroblockBlock extends BlockContainer implements IMultipartContainerBlock {

	public static IMCMPBlockAccess mcmpBlockAccess = SimpleServiceLocator.mcmpProxy.createMCMPBlockAccess();

	public LPMicroblockBlock(Material materialIn) {
		super(materialIn);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public boolean getWeakChanges(IBlockAccess world, BlockPos pos) {
		Block block = mcmpBlockAccess.getBlock();
		return block != null ? block.getWeakChanges(world, pos) : super.getWeakChanges(world, pos);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public boolean isFertile(@Nonnull World world, @Nonnull BlockPos pos) {
		Block block = mcmpBlockAccess.getBlock();
		return block != null ? block.isFertile(world, pos) : super.isFertile(world, pos);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public Boolean isEntityInsideMaterial(IBlockAccess world, BlockPos pos, IBlockState state, Entity entity, double yToTest, Material material,
			boolean testingHead) {
		Block block = mcmpBlockAccess.getBlock();
		return block != null ? block.isEntityInsideMaterial(world, pos, state, entity, yToTest, material, testingHead) : super.isEntityInsideMaterial(world, pos, state, entity, yToTest, material, testingHead);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public boolean isPassable(IBlockAccess world, BlockPos pos) {
		Block block = mcmpBlockAccess.getBlock();
		return block != null ? block.isPassable(world, pos) : super.isPassable(world, pos);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public void onPlantGrow(IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, BlockPos source) {
		Block block = mcmpBlockAccess.getBlock();
		if (block != null) {
			block.onPlantGrow(state, world, pos, source);
		} else {
			super.onPlantGrow(state, world, pos, source);
		}
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		Block block = mcmpBlockAccess.getBlock();
		return block != null ? block.getStrongPower(state, world, pos, side) : super.getStrongPower(state, world, pos, side);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public boolean getTickRandomly() {
		Block block = mcmpBlockAccess.getBlock();
		return block != null ? block.getTickRandomly() : super.getTickRandomly();
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public Boolean isAABBInsideMaterial(World world, BlockPos pos, AxisAlignedBB boundingBox, Material material) {
		Block block = mcmpBlockAccess.getBlock();
		return block != null ? block.isAABBInsideMaterial(world, pos, boundingBox, material) : super.isAABBInsideMaterial(world, pos, boundingBox, material);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
		Block block = mcmpBlockAccess.getBlock();
		if (block != null) {
			block.randomDisplayTick(state, world, pos, rand);
		} else {
			super.randomDisplayTick(state, world, pos, rand);
		}
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
		Block block = mcmpBlockAccess.getBlock();
		if (block != null) {
			block.onEntityCollidedWithBlock(world, pos, state, entity);
		} else {
			super.onEntityCollidedWithBlock(world, pos, state, entity);
		}
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public boolean hasCustomBreakingProgress(IBlockState state) {
		Block block = mcmpBlockAccess.getBlock();
		return block != null ? block.hasCustomBreakingProgress(state) : super.hasCustomBreakingProgress(state);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
		Block block = mcmpBlockAccess.getBlock();
		if (block != null) {
			block.updateTick(world, pos, state, rand);
		} else {
			super.updateTick(world, pos, state, rand);
		}
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public boolean isBurning(IBlockAccess world, BlockPos pos) {
		Block block = mcmpBlockAccess.getBlock();
		return block != null ? block.isBurning(world, pos) : super.isBurning(world, pos);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public boolean canSustainLeaves(IBlockState state, IBlockAccess world, BlockPos pos) {
		Block block = mcmpBlockAccess.getBlock();
		return block != null ? block.canSustainLeaves(state, world, pos) : super.canSustainLeaves(state, world, pos);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public int getComparatorInputOverride(IBlockState blockState, World world, BlockPos pos) {
		Block block = mcmpBlockAccess.getBlock();
		return block != null ? block.getComparatorInputOverride(blockState, world, pos) : super.getComparatorInputOverride(blockState, world, pos);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public void randomTick(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Random random) {
		Block block = mcmpBlockAccess.getBlock();
		if (block != null) {
			block.randomTick(world, pos, state, random);
		} else {
			super.randomTick(world, pos, state, random);
		}
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public boolean isSideSolid(IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, EnumFacing side) {
		Block block = mcmpBlockAccess.getBlock();
		return block != null ? block.isSideSolid(state, world, pos, side) : super.isSideSolid(state, world, pos, side);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
		Block block = mcmpBlockAccess.getBlock();
		return block != null ? block.getLightOpacity(state, world, pos) : super.getLightOpacity(state, world, pos);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public float getEnchantPowerBonus(World world, BlockPos pos) {
		Block block = mcmpBlockAccess.getBlock();
		return block != null ? block.getEnchantPowerBonus(world, pos) : super.getEnchantPowerBonus(world, pos);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
		Block block = mcmpBlockAccess.getBlock();
		return block != null ? block.isLadder(state, world, pos, entity) : super.isLadder(state, world, pos, entity);
	}

	@Nonnull
	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) {
		Block block = mcmpBlockAccess.getBlock();
		return block != null ? block.getBlockFaceShape(world, state, pos, face) : super.getBlockFaceShape(world, state, pos, face);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public boolean isLeaves(IBlockState state, IBlockAccess world, BlockPos pos) {
		Block block = mcmpBlockAccess.getBlock();
		return block != null ? block.isLeaves(state, world, pos) : super.isLeaves(state, world, pos);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
		Block block = mcmpBlockAccess.getBlock();
		return block != null ? block.getExplosionResistance(world, pos, exploder, explosion) : super.getExplosionResistance(world, pos, exploder, explosion);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		Block block = mcmpBlockAccess.getBlock();
		if (block != null) {
			block.neighborChanged(state, worldIn, pos, blockIn, fromPos);
		} else {
			super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
		}
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		Block block = mcmpBlockAccess.getBlock();
		return block != null ? block.getLightValue(state, world, pos) : super.getLightValue(state, world, pos);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public boolean isFoliage(IBlockAccess world, BlockPos pos) {
		Block block = mcmpBlockAccess.getBlock();
		return block != null ? block.isFoliage(world, pos) : super.isFoliage(world, pos);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		Block block = mcmpBlockAccess.getBlock();
		return block != null ? block.canConnectRedstone(state, world, pos, side) : super.canConnectRedstone(state, world, pos, side);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public void fillWithRain(World world, BlockPos pos) {
		Block block = mcmpBlockAccess.getBlock();
		if (block != null) {
			block.fillWithRain(world, pos);
		} else {
			super.fillWithRain(world, pos);
		}
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public boolean isBeaconBase(IBlockAccess world, BlockPos pos, BlockPos beacon) {
		Block block = mcmpBlockAccess.getBlock();
		return block != null ? block.isBeaconBase(world, pos, beacon) : super.isBeaconBase(world, pos, beacon);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public boolean isFlammable(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
		Block block = mcmpBlockAccess.getBlock();
		return block != null ? block.isFlammable(world, pos, face) : super.isFlammable(world, pos, face);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		Block block = mcmpBlockAccess.getBlock();
		return block != null ? block.getWeakPower(state, world, pos, side) : super.getWeakPower(state, world, pos, side);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public boolean canSustainPlant(@Nonnull IBlockState state, @Nonnull IBlockAccess world, BlockPos pos, @Nonnull EnumFacing direction, IPlantable plantable) {
		Block block = mcmpBlockAccess.getBlock();
		return block != null ? block.canSustainPlant(state, world, pos, direction, plantable) : super.canSustainPlant(state, world, pos, direction, plantable);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public boolean isWood(IBlockAccess world, BlockPos pos) {
		Block block = mcmpBlockAccess.getBlock();
		return block != null ? block.isWood(world, pos) : super.isWood(world, pos);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public boolean canCreatureSpawn(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, EntityLiving.SpawnPlacementType type) {
		Block block = mcmpBlockAccess.getBlock();
		return block != null ? block.canCreatureSpawn(state, world, pos, type) : super.canCreatureSpawn(state, world, pos, type);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public boolean isFireSource(@Nonnull World world, BlockPos pos, EnumFacing side) {
		Block block = mcmpBlockAccess.getBlock();
		return block != null ? block.isFireSource(world, pos, side) : super.isFireSource(world, pos, side);
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
		Block block = mcmpBlockAccess.getBlock();
		if (block != null) {
			block.onNeighborChange(world, pos, neighbor);
		} else {
			super.onNeighborChange(world, pos, neighbor);
		}
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public boolean removedByPlayer(@Nonnull IBlockState state, World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player, boolean willHarvest) {
		Pair<Vec3d, Vec3d> vectors = RayTraceHelper.getRayTraceVectors(player);
		RayTraceResult hit = collisionRayTrace(state, world, pos, vectors.getLeft(), vectors.getRight());
		Optional<TileMultipartContainer> tile = getTile(world, pos);
		if (hit != null && tile.isPresent() && hit.subHit >= 0) {
			if (!world.isRemote) {
				IPartSlot slot = MCMultiPart.slotRegistry.getValue(hit.subHit);
				boolean canRemove = tile.get().get(slot).map(i -> {
					if (i.getPart().canPlayerDestroy(i, player)) {
						i.getPart().onPartHarvested(i, player);
						if (player == null || !player.capabilities.isCreativeMode) {
							i.getPart().getDrops(i.getPartWorld(), pos, i, 0).forEach(s -> spawnAsEntity(world, pos, s));
						}
						return true;
					} else {
						return false;
					}
				}).orElse(true);
				if (canRemove)
					tile.get().removePart(slot);
			}
		}
		if (hit != null && hit.subHit == -1) {
			return super.removedByPlayer(state, world, pos, player, willHarvest);
		}
		return false;
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public float getPlayerRelativeBlockHardness(IBlockState state, @Nonnull EntityPlayer player, @Nonnull World world, @Nonnull BlockPos pos) {
		Pair<Vec3d, Vec3d> vectors = RayTraceHelper.getRayTraceVectors(player);
		RayTraceResult hit = collisionRayTrace(getDefaultState(), world, pos, vectors.getLeft(), vectors.getRight());
		if (hit != null && hit.subHit >= 0) {
			return getTile(world, pos).map(t -> t.get(MCMultiPart.slotRegistry.getValue(hit.subHit)).get())
					.map(i -> i.getPart().getPlayerRelativePartHardness(i, (RayTraceResult) hit.hitInfo, player)).orElse(0F);
		}
		if (hit != null) {
			return super.getPlayerRelativeBlockHardness(state, player, world, pos);
		}
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
		Pair<Vec3d, Vec3d> vectors = RayTraceHelper.getRayTraceVectors(MCMultiPart.proxy.getPlayer());
		RayTraceResult hit = collisionRayTrace(getDefaultState(), world, pos, vectors.getLeft(), vectors.getRight());
		if (hit != null && hit.subHit >= 0) {
			IPartInfo part = getTile(world, pos).get().get(MCMultiPart.slotRegistry.getValue(hit.subHit)).get();
			if (!part.getPart().addDestroyEffects(part, manager)) {
				IBlockState state = part.getPart().getActualState(part.getPartWorld(), pos, part);
				for (int i = 0; i < 4; ++i) {
					for (int j = 0; j < 4; ++j) {
						for (int k = 0; k < 4; ++k) {
							double xOff = (i + 0.5D) / 4.0D;
							double yOff = (j + 0.5D) / 4.0D;
							double zOff = (k + 0.5D) / 4.0D;
							manager.addEffect(new ParticleDigging(world, pos.getX() + xOff, pos.getY() + yOff, pos.getZ() + zOff, xOff - 0.5D,
									yOff - 0.5D, zOff - 0.5D, state) {
							}.setBlockPos(pos));
						}
					}
				}
			}
			return true;
		}
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public boolean addHitEffects(IBlockState mpState, World world, RayTraceResult hit, ParticleManager manager) {
		if (hit != null && hit.subHit >= 0) {
			BlockPos pos = hit.getBlockPos();
			IPartInfo part = getTile(world, pos).get().get(MCMultiPart.slotRegistry.getValue(hit.subHit)).get();
			if (!part.getPart().addHitEffects(part, (RayTraceResult) hit.hitInfo, manager)) {
				if (part.getPart().getRenderType(part) != EnumBlockRenderType.INVISIBLE) {
					int x = pos.getX(), y = pos.getY(), z = pos.getZ();
					AxisAlignedBB aabb = part.getPart().getBoundingBox(part);
					double pX = x + world.rand.nextDouble() * (aabb.maxX - aabb.minX - 0.2) + 0.1 + aabb.minX;
					double pY = y + world.rand.nextDouble() * (aabb.maxY - aabb.minY - 0.2) + 0.1 + aabb.minY;
					double pZ = z + world.rand.nextDouble() * (aabb.maxZ - aabb.minZ - 0.2) + 0.1 + aabb.minZ;

					switch (hit.sideHit) {
						case DOWN:
							pY = y + aabb.minY - 0.1;
							break;
						case UP:
							pY = y + aabb.maxY + 0.1;
							break;
						case NORTH:
							pZ = z + aabb.minZ - 0.1;
							break;
						case SOUTH:
							pZ = z + aabb.maxZ + 0.1;
							break;
						case WEST:
							pX = x + aabb.minX - 0.1;
							break;
						case EAST:
							pX = x + aabb.maxX + 0.1;
							break;
					}

					manager.addEffect(new ParticleDigging(world, pX, pY, pZ, 0.0D, 0.0D, 0.0D, part.getPart().getActualState(part.getPartWorld(), pos, part)) {
					}.setBlockPos(pos).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
				}
			}
			return true;
		}
		return false;
	}

	@Nonnull
	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public ItemStack getPickBlock(IBlockState state, RayTraceResult hit, World world, BlockPos pos, EntityPlayer player) {
		if (hit != null && hit.subHit >= 0) {
			return getTile(world, pos).map(t -> t.get(MCMultiPart.slotRegistry.getValue(hit.subHit))).filter(Optional::isPresent)
					.map(o -> o.get().getPart().getPickPart(o.get(), (RayTraceResult) hit.hitInfo, player)).orElse(ItemStack.EMPTY);
		}
		return ItemStack.EMPTY;
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX,
			float hitY, float hitZ) {
		Pair<Vec3d, Vec3d> vectors = RayTraceHelper.getRayTraceVectors(player);
		RayTraceResult hit = collisionRayTrace(getDefaultState(), world, pos, vectors.getLeft(), vectors.getRight());
		if (hit != null && hit.subHit >= 0) {
			return getTile(world, pos).map(t -> t.get(MCMultiPart.slotRegistry.getValue(hit.subHit)).get())
					.map(i -> i.getPart().onPartActivated(i, player, hand, (RayTraceResult) hit.hitInfo)).orElse(false);
		}
		return false;
	}

	@Override
	@ModDependentMethod(modId = LPConstants.mcmpModID)
	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
		Pair<Vec3d, Vec3d> vectors = RayTraceHelper.getRayTraceVectors(player);
		RayTraceResult hit = collisionRayTrace(getDefaultState(), world, pos, vectors.getLeft(), vectors.getRight());
		if (hit != null && hit.subHit >= 0) {
			getTile(world, pos).map(t -> t.get(MCMultiPart.slotRegistry.getValue(hit.subHit)).get())
					.ifPresent(i -> i.getPart().onPartClicked(i, player, (RayTraceResult) hit.hitInfo));
		}
	}
}
