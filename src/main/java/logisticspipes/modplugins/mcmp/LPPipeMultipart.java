package logisticspipes.modplugins.mcmp;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
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

import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.multipart.MultipartOcclusionHelper;
import mcmultipart.api.slot.EnumCenterSlot;
import mcmultipart.api.slot.IPartSlot;

import logisticspipes.LPBlocks;
import logisticspipes.pipes.basic.LogisticsBlockGenericPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericPipe;

public class LPPipeMultipart implements IMultipart {

	@Override
	public IPartSlot getSlotForPlacement(World world, BlockPos blockPos, IBlockState iBlockState, EnumFacing enumFacing, float v, float v1, float v2, EntityLivingBase entityLivingBase) {
		return EnumCenterSlot.CENTER;
	}

	@Override
	public IPartSlot getSlotFromWorld(IBlockAccess iBlockAccess, BlockPos blockPos, IBlockState iBlockState) {
		return EnumCenterSlot.CENTER;
	}

	@Override
	public Block getBlock() {
		return LPBlocks.pipe;
	}

	@Override
	public List<AxisAlignedBB> getOcclusionBoxes(IPartInfo part) {
		return Collections.singletonList(LogisticsBlockGenericPipe.PIPE_CENTER_BB);
	}

	@Override
	public IBlockState getExtendedState(IBlockAccess world, BlockPos pos, IPartInfo part, IBlockState state) {
		return null;
	}

	@Override
	public RayTraceResult collisionRayTrace(IPartInfo part, Vec3d start, Vec3d end) {
		return null;
	}

	@Override
	public IMultipartTile loadMultipartTile(World world, NBTTagCompound tag) {
		return new LPMultipartTile(null);
	}

	@Override
	public IMultipartTile convertToMultipartTile(TileEntity tileEntity) {
		return new LPMultipartTile((LogisticsTileGenericPipe) tileEntity);
	}

	@Override
	public boolean shouldWrapWorld() {
		return false;
	}

	public boolean testIntersection(IPartInfo self, IPartInfo otherPart) {
		return MultipartOcclusionHelper.testBoxIntersection(this.getOcclusionBoxes(self), otherPart.getPart().getOcclusionBoxes(otherPart));
	}

	public IBlockState getActualState(IBlockAccess world, BlockPos pos, IPartInfo part) {
		return part.getState();
	}

	public boolean canRenderInLayer(IBlockAccess world, BlockPos pos, IPartInfo part, IBlockState state, BlockRenderLayer layer) {
		return false;
	}

	public void onPartPlacedBy(IPartInfo part, EntityLivingBase placer, @Nonnull ItemStack stack) {}

	public boolean isSideSolid(IBlockAccess world, BlockPos pos, IPartInfo part, EnumFacing side) {
		return false;
	}

	public void randomDisplayTick(IPartInfo part, Random rand) {}

	public boolean addDestroyEffects(IPartInfo part, ParticleManager manager) {
		return false;
	}

	public boolean addHitEffects(IPartInfo part, RayTraceResult hit, ParticleManager manager) {
		return false;
	}

	public EnumBlockRenderType getRenderType(IPartInfo part) {
		return part.getState().getRenderType();
	}

	public static final AxisAlignedBB EMPTY_BLOCK_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);

	public AxisAlignedBB getBoundingBox(IPartInfo part) {
		return EMPTY_BLOCK_AABB;
	}

	public boolean canConnectRedstone(IBlockAccess world, BlockPos pos, IPartInfo part, EnumFacing side) {
		return false;
	}

	public int getWeakPower(IBlockAccess world, BlockPos pos, IPartInfo part, EnumFacing side) {
		return 0;
	}

	public int getStrongPower(IBlockAccess world, BlockPos pos, IPartInfo part, EnumFacing side) {
		return 0;
	}

	public boolean canCreatureSpawn(IBlockAccess world, BlockPos pos, IPartInfo part, EntityLiving.SpawnPlacementType type) {
		return false;
	}

	public boolean canSustainLeaves(IBlockAccess world, BlockPos pos, IPartInfo part) {
		return false;
	}

	public boolean canSustainPlant(IBlockAccess world, BlockPos pos, IPartInfo part, EnumFacing direction, IPlantable plantable) {
		return false;
	}

	public void fillWithRain(IPartInfo part) {}

	public int getComparatorInputOverride(IPartInfo part) {
		return 0;
	}

	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IPartInfo part, int fortune) {
		return Collections.emptyList();
	}

	public float getExplosionResistance(IPartInfo part, Entity exploder, Explosion explosion) {
		return 0;
	}

	public float getEnchantPowerBonus(IPartInfo part) {
		return 0;
	}

	public int getLightOpacity(IBlockAccess world, BlockPos pos, IPartInfo part) {
		return 0;
	}

	public int getLightOpacity(IBlockState state) {
		return 0;
	}

	public int getLightValue(IBlockAccess world, BlockPos pos, IPartInfo part) {
		return 0;
	}

	public int getLightValue(IBlockState state) {
		return 0;
	}

	@Nonnull
	public ItemStack getPickPart(IPartInfo part, RayTraceResult hit, EntityPlayer player) {
		return ItemStack.EMPTY;
	}

	public float getPlayerRelativePartHardness(IPartInfo part, RayTraceResult hit, EntityPlayer player) {
		return 0;
	}

	public Boolean isAABBInsideMaterial(IPartInfo part, AxisAlignedBB boundingBox, Material material) {
		return null;
	}

	public boolean isBeaconBase(IBlockAccess world, BlockPos pos, IPartInfo part, BlockPos beacon) {
		return false;
	}

	public boolean isBurning(IBlockAccess world, BlockPos pos, IPartInfo part) {
		return false;
	}

	public Boolean isEntityInsideMaterial(IBlockAccess world, BlockPos pos, IPartInfo part, Entity entity, double yToTest, Material material, boolean testingHead) {
		return null;
	}

	public boolean isFertile(IPartInfo part) {
		return false;
	}

	public boolean isFireSource(IPartInfo part, EnumFacing side) {
		return false;
	}

	public boolean isFlammable(IBlockAccess world, BlockPos pos, IPartInfo part, EnumFacing face) {
		return false;
	}

	public boolean isFoliage(IBlockAccess world, BlockPos pos, IPartInfo part) {
		return false;
	}

	public boolean isLadder(IBlockAccess world, BlockPos pos, IPartInfo part, EntityLivingBase entity) {
		return false;
	}

	public boolean isLeaves(IBlockAccess world, BlockPos pos, IPartInfo part) {
		return false;
	}

	public boolean isPassable(IBlockAccess world, BlockPos pos, IPartInfo part) {
		return false;
	}

	public boolean isWood(IBlockAccess world, BlockPos pos, IPartInfo part) {
		return false;
	}

	public void onPartClicked(IPartInfo part, EntityPlayer player, RayTraceResult hit) {}

	public void neighborChanged(IPartInfo part, Block neighborBlock, BlockPos neighborPos) {
	}

	public void onNeighborChange(IPartInfo part, BlockPos neighbor) {
	}

	public boolean onPartActivated(IPartInfo part, EntityPlayer player, EnumHand hand, RayTraceResult hit) {
		return false;
	}

	public void onPlantGrow(IPartInfo part, BlockPos source) {}

	public boolean canPlayerDestroy(IPartInfo part, EntityPlayer player) {
		return true;
	}

	public void onPartHarvested(IPartInfo part, EntityPlayer player) {}

	public void randomTick(IPartInfo part, Random random) {}

	public BlockFaceShape getPartFaceShape(IPartInfo part, EnumFacing face) {
		return BlockFaceShape.SOLID;
	}

	public boolean canPlacePartAt(World world, BlockPos pos) {
		return true;
	}

	public void breakPart(IPartInfo part) {
		this.getBlock().breakBlock(part.getPartWorld(), part.getPartPos(), part.getState());
	}

	public void updateTick(IPartInfo part, Random rand) {}

	public void addCollisionBoxToList(IPartInfo part, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entity, boolean unknown) {}

	public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state) {
		return null;
	}

	public AxisAlignedBB getSelectedBoundingBox(IPartInfo part) {
		return EMPTY_BLOCK_AABB;
	}

	public void onEntityCollidedWithPart(IPartInfo part, Entity entity) {}

	public void dropPartAsItem(IPartInfo part, int fortune) {}
}
