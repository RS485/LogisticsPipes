package logisticspipes.pipes.basic;

import logisticspipes.LPConstants;
import logisticspipes.LogisticsPipes;
import logisticspipes.config.Configs;
import logisticspipes.interfaces.IRotationProvider;
import logisticspipes.items.ItemLogisticsPipe;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.proxy.buildcraft.subproxies.IBCClickResult;
import logisticspipes.proxy.buildcraft.subproxies.IBCPipePluggable;
import logisticspipes.renderer.LogisticsPipeWorldRenderer;
import logisticspipes.renderer.newpipe.LogisticsNewRenderPipe;
import logisticspipes.textures.Textures;
import logisticspipes.ticks.QueuedTasks;
import logisticspipes.utils.MatrixTranformations;
import logisticspipes.utils.UtilBlockPos;
import logisticspipes.utils.UtilEnumFacing;
import logisticspipes.utils.UtilWorld;
import logisticspipes.utils.tuples.LPPosition;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;
import java.util.concurrent.Callable;

public class LogisticsBlockGenericPipe extends BlockContainer {

	UtilWorld utilWorld;
	UtilEnumFacing utilEnumFacing;
	UtilBlockPos utilBlockPos;

	public LogisticsBlockGenericPipe() {
		super(Material.glass);
		setRenderAllSides();
		setCreativeTab(null);
	}

	@Override
	public ArrayList<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		if (world.) {
			return null;
		}
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		int count = quantityDropped(metadata, fortune, world.rand);
		for (int i = 0; i < count; i++) {
			CoreUnroutedPipe pipe = LogisticsBlockGenericPipe.getPipe(world, pos);

			if (pipe == null) {
				pipe = LogisticsBlockGenericPipe.pipeRemoved.get(new LPPosition(pos));
			}

			if (pipe != null) {
				if (pipe.item != null && (pipe.canBeDestroyed() || pipe.destroyByPlayer())) {
					list.addAll(pipe.dropContents());
					list.add(new ItemStack(pipe.item, 1, damageDropped(metadata)));
				} else if (pipe.item != null) {
					LogisticsBlockGenericPipe.cacheTileToPreventRemoval(pipe);
				}
			}
		}
		return list;
	}



	@Override
	@SuppressWarnings("rawtypes")
	public void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe) tile).pipe instanceof PipeBlockRequestTable) {
			setBlockBounds(0, 0, 0, 1, 1, 1);
			super.addCollisionBoxesToList(world, pos, state, mask, list, collidingEntity);
			return;
		}
		setBlockBounds(LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MAX_POS, LPConstants.PIPE_MAX_POS, LPConstants.PIPE_MAX_POS);
		super.addCollisionBoxesToList(world, pos, state, mask, list, collidingEntity);
		if (tile instanceof LogisticsTileGenericPipe) {
			LogisticsTileGenericPipe tileG = (LogisticsTileGenericPipe) tile;

			if (tileG.isPipeConnected(EnumFacing.WEST)) {
				setBlockBounds(0.0F, LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MAX_POS, LPConstants.PIPE_MAX_POS, LPConstants.PIPE_MAX_POS);
				super.addCollisionBoxesToList(world, pos, state, mask, list, collidingEntity);
			}

			if (tileG.isPipeConnected(EnumFacing.EAST)) {
				setBlockBounds(LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MIN_POS, 1.0F, LPConstants.PIPE_MAX_POS, LPConstants.PIPE_MAX_POS);
				super.addCollisionBoxesToList(world, pos, state, mask, list, collidingEntity);
			}

			if (tileG.isPipeConnected(EnumFacing.DOWN)) {
				setBlockBounds(LPConstants.PIPE_MIN_POS, 0.0F, LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MAX_POS, LPConstants.PIPE_MAX_POS, LPConstants.PIPE_MAX_POS);
				super.addCollisionBoxesToList(world, pos, state, mask, list, collidingEntity);
			}

			if (tileG.isPipeConnected(EnumFacing.UP)) {
				setBlockBounds(LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MAX_POS, 1.0F, LPConstants.PIPE_MAX_POS);
				super.addCollisionBoxesToList(world, pos, state, mask, list, collidingEntity);
			}

			if (tileG.isPipeConnected(EnumFacing.NORTH)) {
				setBlockBounds(LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MIN_POS, 0.0F, LPConstants.PIPE_MAX_POS, LPConstants.PIPE_MAX_POS, LPConstants.PIPE_MAX_POS);
				super.addCollisionBoxesToList(world, pos, state, mask, list, collidingEntity);
			}

			if (tileG.isPipeConnected(EnumFacing.SOUTH)) {
				setBlockBounds(LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MAX_POS, LPConstants.PIPE_MAX_POS, 1.0F);
				super.addCollisionBoxesToList(world, pos, state, mask, list, collidingEntity);
			}

			float facadeThickness = LPConstants.FACADE_THICKNESS;

			if (tileG.tilePart.hasEnabledFacade(EnumFacing.EAST)) {
				setBlockBounds(1 - facadeThickness, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				super.addCollisionBoxesToList(world, pos, state, mask, list, collidingEntity);
			}

			if (tileG.tilePart.hasEnabledFacade(EnumFacing.WEST)) {
				setBlockBounds(0.0F, 0.0F, 0.0F, facadeThickness, 1.0F, 1.0F);
				super.addCollisionBoxesToList(world, pos, state, mask, list, collidingEntity);
			}

			if (tileG.tilePart.hasEnabledFacade(EnumFacing.UP)) {
				setBlockBounds(0.0F, 1 - facadeThickness, 0.0F, 1.0F, 1.0F, 1.0F);
				super.addCollisionBoxesToList(world, pos, state, mask, list, collidingEntity);
			}

			if (tileG.tilePart.hasEnabledFacade(EnumFacing.DOWN)) {
				setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, facadeThickness, 1.0F);
				super.addCollisionBoxesToList(world, pos, state, mask, list, collidingEntity);
			}

			if (tileG.tilePart.hasEnabledFacade(EnumFacing.SOUTH)) {
				setBlockBounds(0.0F, 0.0F, 1 - facadeThickness, 1.0F, 1.0F, 1.0F);
				super.addCollisionBoxesToList(world, pos, state, mask, list, collidingEntity);
			}

			if (tileG.tilePart.hasEnabledFacade(EnumFacing.NORTH)) {
				setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, facadeThickness);
				super.addCollisionBoxesToList(world, pos, state, mask, list, collidingEntity);
			}
		}
		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state){
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe) tile).pipe instanceof PipeBlockRequestTable) {
			return AxisAlignedBB.fromBounds((double) pos.getX() + 0, (double) pos.getY() + 0, (double) pos.getZ() + 0,
					(double) pos.getX() + 1, (double) pos.getY() + 1, (double) pos.getZ() + 1);
		}
		RaytraceResult rayTraceResult = doRayTrace(world, pos, Minecraft.getMinecraft().thePlayer);

		if (rayTraceResult != null && rayTraceResult.boundingBox != null) {
			AxisAlignedBB box = rayTraceResult.boundingBox;
			switch (rayTraceResult.hitPart) {
				case Pluggable: {
					float scale = 0.001F;
					box = box.expand(scale, scale, scale);
					break;
				}
				case Pipe: {
					float scale = 0.001F;
					box = box.expand(scale, scale, scale);
					break;
				}
			}
			return box.getOffsetBoundingBox(pos);
		}
		return super.getSelectedBoundingBoxFromPool(world, pos).expand(-0.85F, -0.85F, -0.85F);
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World world, BlockPos pos, Vec3 origin, Vec3 direction) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe) tile).pipe instanceof PipeBlockRequestTable) {
			setBlockBoundsBasedOnState(world, pos);
			origin = origin.addVector((-pos.getX()), (-pos.getY()), (-pos.getZ()));
			direction = direction.addVector((-pos.getX()), (-pos.getY()), (-pos.getZ()));
			this.setBlockBounds(0, 0, 0, 1, 1, 1);
			Vec3 vec32 = origin.getIntermediateWithXValue(direction, minX);
			Vec3 vec33 = origin.getIntermediateWithXValue(direction, maxX);
			Vec3 vec34 = origin.getIntermediateWithYValue(direction, minY);
			Vec3 vec35 = origin.getIntermediateWithYValue(direction, maxY);
			Vec3 vec36 = origin.getIntermediateWithZValue(direction, minZ);
			Vec3 vec37 = origin.getIntermediateWithZValue(direction, maxZ);
			if (!isVecInsideYZBounds(vec32)) {
				vec32 = null;
			}
			if (!isVecInsideYZBounds(vec33)) {
				vec33 = null;
			}
			if (!isVecInsideXZBounds(vec34)) {
				vec34 = null;
			}
			if (!isVecInsideXZBounds(vec35)) {
				vec35 = null;
			}
			if (!isVecInsideXYBounds(vec36)) {
				vec36 = null;
			}
			if (!isVecInsideXYBounds(vec37)) {
				vec37 = null;
			}
			Vec3 vec38 = null;
			if (vec32 != null && (vec38 == null || origin.squareDistanceTo(vec32) < origin.squareDistanceTo(vec38))) {
				vec38 = vec32;
			}
			if (vec33 != null && (vec38 == null || origin.squareDistanceTo(vec33) < origin.squareDistanceTo(vec38))) {
				vec38 = vec33;
			}
			if (vec34 != null && (vec38 == null || origin.squareDistanceTo(vec34) < origin.squareDistanceTo(vec38))) {
				vec38 = vec34;
			}
			if (vec35 != null && (vec38 == null || origin.squareDistanceTo(vec35) < origin.squareDistanceTo(vec38))) {
				vec38 = vec35;
			}
			if (vec36 != null && (vec38 == null || origin.squareDistanceTo(vec36) < origin.squareDistanceTo(vec38))) {
				vec38 = vec36;
			}
			if (vec37 != null && (vec38 == null || origin.squareDistanceTo(vec37) < origin.squareDistanceTo(vec38))) {
				vec38 = vec37;
			}
			if (vec38 == null) {
				return null;
			} else {
				byte b0 = -1;
				if (vec38 == vec32) {
					b0 = 4;
				}
				if (vec38 == vec33) {
					b0 = 5;
				}
				if (vec38 == vec34) {
					b0 = 0;
				}
				if (vec38 == vec35) {
					b0 = 1;
				}
				if (vec38 == vec36) {
					b0 = 2;
				}
				if (vec38 == vec37) {
					b0 = 3;
				}
				return new MovingObjectPosition(pos, b0, vec38.addVector(pos.getX(), pos.getY(), pos.getZ()));
			}
		}
		RaytraceResult raytraceResult = doRayTrace(world, pos, origin, direction);

		if (raytraceResult == null) {
			return null;
		} else {
			return raytraceResult.movingObjectPosition;
		}
	}

	public RaytraceResult doRayTrace(World world, BlockPos pos, EntityPlayer player) {
		double reachDistance = 5;

		if (player instanceof EntityPlayerMP) {
			reachDistance = ((EntityPlayerMP) player).theItemInWorldManager.getBlockReachDistance();
		}

		double eyeHeight = world.isRemote ? player.getEyeHeight() - player.getDefaultEyeHeight() : player.getEyeHeight();
		Vec3 lookVec = player.getLookVec();
		Vec3 origin = Vec3.createVectorHelper(player.posX, player.posY + eyeHeight, player.posZ);
		Vec3 direction = origin.addVector(lookVec.xCoord * reachDistance, lookVec.yCoord * reachDistance, lookVec.zCoord * reachDistance);

		return doRayTrace(world, pos, origin, direction);
	}

	private RaytraceResult doRayTrace(World world, BlockPos pos, Vec3 origin, Vec3 direction) {
		TileEntity pipeTileEntity = world.getTileEntity(pos);

		LogisticsTileGenericPipe tileG = null;
		if (pipeTileEntity instanceof LogisticsTileGenericPipe) {
			tileG = (LogisticsTileGenericPipe) pipeTileEntity;
		}

		if (tileG == null) {
			return null;
		}

		CoreUnroutedPipe pipe = tileG.pipe;

		if (!LogisticsBlockGenericPipe.isValid(pipe)) {
			return null;
		}

		/**
		 * pipe hits along x, y, and z axis, gate (all 6 sides) [and
		 * wires+facades]
		 */
		MovingObjectPosition[] hits = new MovingObjectPosition[31];
		AxisAlignedBB[] boxes = new AxisAlignedBB[31];
		EnumFacing[] sideHit = new EnumFacing[31];
		Arrays.fill(sideHit, UtilEnumFacing.UNKNOWN);

		// pipe

		for (EnumFacing side : LogisticsBlockGenericPipe.DIR_VALUES) {
			if (side == UtilEnumFacing.UNKNOWN || tileG.isPipeConnected(side)) {
				AxisAlignedBB bb = getPipeBoundingBox(side);
				setBlockBounds(bb);
				boxes[side.ordinal()] = bb;
				hits[side.ordinal()] = super.collisionRayTrace(world, pos, origin, direction);
				sideHit[side.ordinal()] = side;
			}
		}

		// pluggables

		for (EnumFacing side : UtilEnumFacing.VALID_DIRECTIONS) {
			if (tileG.getPipePluggable(side) != null) {
				AxisAlignedBB bb = tileG.getPipePluggable(side).getBoundingBox(side);
				setBlockBounds(bb);
				boxes[7 + side.ordinal()] = bb;
				hits[7 + side.ordinal()] = super.collisionRayTrace(world, pos, origin, direction);
				sideHit[7 + side.ordinal()] = side;
			}
		}

		// TODO: check wires

		// get closest hit

		double minLengthSquared = Double.POSITIVE_INFINITY;
		int minIndex = -1;

		for (int i = 0; i < hits.length; i++) {
			MovingObjectPosition hit = hits[i];
			if (hit == null) {
				continue;
			}

			double lengthSquared = hit.hitVec.squareDistanceTo(origin);

			if (lengthSquared < minLengthSquared) {
				minLengthSquared = lengthSquared;
				minIndex = i;
			}
		}

		// reset bounds

		setBlockBounds(0, 0, 0, 1, 1, 1);

		if (minIndex == -1) {
			return null;
		} else {
			Part hitPart;

			if (minIndex < 7) {
				hitPart = Part.Pipe;
			} else {
				hitPart = Part.Pluggable;
			}

			return new RaytraceResult(hitPart, hits[minIndex], boxes[minIndex], sideHit[minIndex]);
		}
	}

	private void setBlockBounds(AxisAlignedBB bb) {
		setBlockBounds((float) bb.minX, (float) bb.minY, (float) bb.minZ, (float) bb.maxX, (float) bb.maxY, (float) bb.maxZ);
	}

	private AxisAlignedBB getPipeBoundingBox(EnumFacing side) {
		float min = LPConstants.PIPE_MIN_POS;
		float max = LPConstants.PIPE_MAX_POS;

		if (side == UtilEnumFacing.UNKNOWN) {
			return AxisAlignedBB.fromBounds(min, min, min, max, max, max);
		}

		float[][] bounds = new float[3][2];
		// X START - END
		bounds[0][0] = min;
		bounds[0][1] = max;
		// Y START - END
		bounds[1][0] = 0;
		bounds[1][1] = min;
		// Z START - END
		bounds[2][0] = min;
		bounds[2][1] = max;

		MatrixTranformations.transform(bounds, side);
		return AxisAlignedBB.fromBounds(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]);
	}

	private boolean isVecInsideYZBounds(Vec3 par1Vec3) {
		return par1Vec3 == null ? false : par1Vec3.yCoord >= minY && par1Vec3.yCoord <= maxY && par1Vec3.zCoord >= minZ && par1Vec3.zCoord <= maxZ;
	}

	private boolean isVecInsideXZBounds(Vec3 par1Vec3) {
		return par1Vec3 == null ? false : par1Vec3.xCoord >= minX && par1Vec3.xCoord <= maxX && par1Vec3.zCoord >= minZ && par1Vec3.zCoord <= maxZ;
	}

	private boolean isVecInsideXYBounds(Vec3 par1Vec3) {
		return par1Vec3 == null ? false : par1Vec3.xCoord >= minX && par1Vec3.xCoord <= maxX && par1Vec3.yCoord >= minY && par1Vec3.yCoord <= maxY;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new LogisticsTileGenericPipe();
	}

	public static Map<Item, Class<? extends CoreUnroutedPipe>> pipes = new HashMap<Item, Class<? extends CoreUnroutedPipe>>();
	public static Map<LPPosition, CoreUnroutedPipe> pipeRemoved = new HashMap<LPPosition, CoreUnroutedPipe>();

	private static long lastRemovedDate = -1;

	public static enum Part {
		Pipe,
		Pluggable
	}

	public static class RaytraceResult {

		public final Part hitPart;
		public final MovingObjectPosition movingObjectPosition;
		public final AxisAlignedBB boundingBox;
		public final EnumFacing sideHit;

		RaytraceResult(Part hitPart, MovingObjectPosition movingObjectPosition, AxisAlignedBB boundingBox, EnumFacing side) {
			this.hitPart = hitPart;
			this.movingObjectPosition = movingObjectPosition;
			this.boundingBox = boundingBox;
			sideHit = side;
		}

		@Override
		public String toString() {
			return String.format("RayTraceResult: %s, %s", hitPart == null ? "null" : hitPart.name(), boundingBox == null ? "null" : boundingBox.toString());
		}
	}

	private static final EnumFacing[] DIR_VALUES = EnumFacing.values();
	private boolean skippedFirstIconRegister;
	private int renderMask = 0;
	protected final Random rand = new Random();

	@Override
	public float getBlockHardness(World par1World, BlockPos pos) {
		return Configs.pipeDurability;
	}

	@Override
	public int getRenderType() {
		return LPConstants.pipeModel;
	}
//TODO there is a canRenderInLayer
	@Override
	public boolean canRenderInPass(int pass) {
		LogisticsPipeWorldRenderer.renderPass = pass;
		return true;
	}

	@Override
	public int getRenderBlockPass() {
		return 1;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean canBeReplacedByLeaves(IBlockAccess world, BlockPos pos) {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	public void setRenderMask(int mask) {
		renderMask = mask;
	}

	public final void setRenderAllSides() {
		renderMask = 0x3f;
	}

	public void setRenderSide(EnumFacing side, boolean render) {
		if (render) {
			renderMask |= 1 << side.ordinal();
		} else {
			renderMask &= ~(1 << side.ordinal());
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
		return (renderMask & (1 << utilEnumFacing.getIdfromEnum(side))) != 0;
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side) {
		TileEntity tile = world.getTileEntity(pos);

		if (tile instanceof LogisticsTileGenericPipe) {
			if (((LogisticsTileGenericPipe) tile).pipe instanceof PipeBlockRequestTable) {
				return true;
			}
			return ((LogisticsTileGenericPipe) tile).isSolidOnSide(side);
		}

		return false;
	}

	@Override
	public boolean isNormalCube() {
		return false;
	}

	public static void removePipe(CoreUnroutedPipe pipe) {
		if (!LogisticsBlockGenericPipe.isValid(pipe)) {
			return;
		}

		if (pipe.canBeDestroyed() || pipe.destroyByPlayer()) {
			pipe.onBlockRemoval();
		} else if (pipe.preventRemove()) {
			LogisticsBlockGenericPipe.cacheTileToPreventRemoval(pipe);
		}

		World world = pipe.container.getWorld();

		if (world == null) {
			return;
		};


		BlockPos pos = pipe.container.pos;


		if (LogisticsBlockGenericPipe.lastRemovedDate != world.getTotalWorldTime()) {
			LogisticsBlockGenericPipe.lastRemovedDate = world.getTotalWorldTime();
			LogisticsBlockGenericPipe.pipeRemoved.clear();
		}

		LogisticsBlockGenericPipe.pipeRemoved.put(new LPPosition(pos.getX(), pos.getY(), pos.getZ()), pipe);
		world.removeTileEntity(pos);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		LogisticsBlockGenericPipe.removePipe(LogisticsBlockGenericPipe.getPipe(world, pos));
		super.breakBlock(world, pos, state);
		SimpleServiceLocator.buildCraftProxy.callBCRemovePipe(world, pos);
	}

	@Override
	public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state, int i) {

		if (world.isRemote) {
			return;
		}

		int i1 = quantityDropped(world.rand);
		for (int j1 = 0; j1 < i1; j1++) {
			if (world.rand.nextFloat() > f) {
				continue;
			}

			CoreUnroutedPipe pipe = LogisticsBlockGenericPipe.getPipe(world, pos);

			if (pipe == null) {
				pipe = LogisticsBlockGenericPipe.pipeRemoved.get(new LPPosition(pos));
			}

			if (pipe.item != null && (pipe.canBeDestroyed() || pipe.destroyByPlayer())) {
				for (ItemStack stack : pipe.dropContents()) {
					dropBlockAsItem(world, pos, state,i);
				}
				dropBlockAsItem(world, pos, state,i);
			} else if (pipe.item != null) {
				LogisticsBlockGenericPipe.cacheTileToPreventRemoval(pipe);
			}
		}
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int dmg) {
		// Returns null to be safe - the id does not depend on the meta
		return null;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos) {
		RaytraceResult rayTraceResult = doRayTrace(world, pos, Minecraft.getMinecraft().thePlayer);

		if (rayTraceResult != null && rayTraceResult.boundingBox != null) {
			switch (rayTraceResult.hitPart) {
				case Pluggable: {
					CoreUnroutedPipe pipe = LogisticsBlockGenericPipe.getPipe(world, pos);
					IBCPipePluggable pluggable = pipe.container.tilePart.getBCPipePluggable(rayTraceResult.sideHit);
					ItemStack[] drops = pluggable.getDropItems(pipe.container);
					if (drops != null && drops.length > 0) {
						return drops[0];
					}
				}
				case Pipe:
					return new ItemStack(LogisticsBlockGenericPipe.getPipe(world, pos).item);
			}
		}
		return null;
	}

	/* Wrappers ************************************************************ */
	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block block) {
		super.onNeighborBlockChange(world, pos, state, block);

		CoreUnroutedPipe pipe = LogisticsBlockGenericPipe.getPipe(world, pos);

		if (LogisticsBlockGenericPipe.isValid(pipe)) {
			pipe.container.scheduleNeighborChange();
		}
		SimpleServiceLocator.buildCraftProxy.callBCNeighborBlockChange(world, pos, block);
	}

	@Override
	public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		super.onBlockPlaced(world, pos, facing, hitX, hitY, hitZ, meta,placer);
		CoreUnroutedPipe pipe = LogisticsBlockGenericPipe.getPipe(world, pos);

		if (LogisticsBlockGenericPipe.isValid(pipe)) {
			pipe.onBlockPlaced();
		}

		return getBlockState().getBaseState();
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(world, pos,state, placer, stack);
		CoreUnroutedPipe pipe = LogisticsBlockGenericPipe.getPipe(world, pos);

		if (LogisticsBlockGenericPipe.isValid(pipe)) {
			pipe.onBlockPlacedBy(placer);
			if (pipe instanceof IRotationProvider) {
				double xPos = pipe.getPos().getX() + 0.5 - placer.posX;
				double zPos = pipe.getPos().getZ() + 0.5 - placer.posZ;
				double w = Math.atan2(xPos, zPos);
				double halfPI = Math.PI / 2;
				double halfhalfPI = halfPI / 2;
				w -= halfhalfPI;
				if (w < 0) {
					w += 2 * Math.PI;
				}
				if (0 < w && w <= halfPI) {
					((IRotationProvider) pipe).setRotation(1);
				} else if (halfPI < w && w <= 2 * halfPI) {
					((IRotationProvider) pipe).setRotation(2);
				} else if (2 * halfPI < w && w <= 3 * halfPI) {
					((IRotationProvider) pipe).setRotation(0);
				} else if (3 * halfPI < w && w <= 4 * halfPI) {
					((IRotationProvider) pipe).setRotation(3);
				}
			}
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ){
		super.onBlockActivated(world, pos,state, player, side, hitX, hitY, hitZ);

		world.notifyBlockOfStateChange(pos, LogisticsPipes.LogisticsPipeBlock);

		CoreUnroutedPipe pipe = LogisticsBlockGenericPipe.getPipe(world, pos);

		if (LogisticsBlockGenericPipe.isValid(pipe)) {
			ItemStack currentItem = player.getCurrentEquippedItem();

			if (currentItem == null) {
				// Fall through the end of the test
			} else if (currentItem.getItem() == Items.sign) {
				// Sign will be placed anyway, so lets show the sign gui
				return false;
			} else if (currentItem.getItem() instanceof ItemLogisticsPipe) {
				return false;
			} else if (SimpleServiceLocator.toolWrenchHandler.isWrench(currentItem.getItem())) {
				// Only check the instance at this point. Call the IToolWrench
				// interface callbacks for the individual pipe/logic calls
				return pipe.blockActivated(player);
			}
			if (pipe.canHoldBCParts()) {
				IBCClickResult result = SimpleServiceLocator.buildCraftProxy.handleBCClickOnPipe(world, pos, player, side, hitX, hitY, hitZ, pipe);
				if (result.handled()) {
					return true;
				}
				if (result.blocked()) {
					return false;
				}
			}
			return pipe.blockActivated(player);
		}

		return false;
	}

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, Entity entity){
		super.onEntityCollidedWithBlock(world, pos, entity);

		CoreUnroutedPipe pipe = LogisticsBlockGenericPipe.getPipe(world, pos);

		if (LogisticsBlockGenericPipe.isValid(pipe)) {
			pipe.onEntityCollidedWithBlock(entity);
		}
	}

	@Override
	public boolean canConnectRedstone(IBlockAccess world, BlockPos pos, EnumFacing side) {
		CoreUnroutedPipe pipe = LogisticsBlockGenericPipe.getPipe(world, pos);

		if (LogisticsBlockGenericPipe.isValid(pipe)) {
			return pipe.bcPipePart.canConnectRedstone();
		} else {
			return false;
		}
	}

	public int isProvidingStrongPower(World world, BlockPos pos, EnumFacing side) {
		CoreUnroutedPipe pipe = LogisticsBlockGenericPipe.getPipe(world, pos);

		if (LogisticsBlockGenericPipe.isValid(pipe)) {
			return pipe.bcPipePart.isPoweringTo(side);
		} else {
			return 0;
		}
	}

	@Override
	public boolean canProvidePower() {
		return true;
	}

	public int isProvidingWeakPower(World world, BlockPos pos,EnumFacing side) {
		CoreUnroutedPipe pipe = LogisticsBlockGenericPipe.getPipe(world, pos);

		if (LogisticsBlockGenericPipe.isValid(pipe)) {
			return pipe.bcPipePart.isIndirectlyPoweringTo(utilEnumFacing.getIdfromEnum(side));
		} else {
			return 0;
		}
	}

	/* Registration ******************************************************** */
	public static ItemLogisticsPipe registerPipe(Class<? extends CoreUnroutedPipe> clas) {
		ItemLogisticsPipe item = new ItemLogisticsPipe();
		item.setUnlocalizedName(clas.getSimpleName());
		GameRegistry.registerItem(item, item.getUnlocalizedName());

		LogisticsBlockGenericPipe.pipes.put(item, clas);

		CoreUnroutedPipe dummyPipe = LogisticsBlockGenericPipe.createPipe(item);
		if (dummyPipe != null) {
			item.setPipeIconIndex(dummyPipe.getIconIndexForItem(), dummyPipe.getTextureIndex());
			MainProxy.proxy.setIconProviderFromPipe(item, dummyPipe);
		}

		return item;
	}

	public static boolean isPipeRegistered(int key) {
		return LogisticsBlockGenericPipe.pipes.containsKey(key);
	}

	public static CoreUnroutedPipe createPipe(Item key) {
		Class<? extends CoreUnroutedPipe> pipe = LogisticsBlockGenericPipe.pipes.get(key);
		if (pipe != null) {
			try {
				return pipe.getConstructor(Item.class).newInstance(key);
			} catch (ReflectiveOperationException e) {
				LogisticsPipes.log.error("Could not construct class " + pipe.getSimpleName() + " for key " + key, e);
			}
		} else {
			LogisticsPipes.log.warn("Detected pipe with unknown key (" + key + "). Did you remove a buildcraft addon?");
		}

		return null;
	}

	public static boolean placePipe(CoreUnroutedPipe pipe, World world, BlockPos pos, Block block, int meta) {
		if (world.isRemote) {
			return true;
		}

		boolean placed = UtilWorld.blockExists(pos, world);

		if (placed) {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof LogisticsTileGenericPipe) {
				LogisticsTileGenericPipe tilePipe = (LogisticsTileGenericPipe) tile;
				tilePipe.initialize(pipe);
				tilePipe.sendUpdateToClient();
			}
			world.notifyBlockOfStateChange(pos, block);
		}

		return placed;
	}

	public static CoreUnroutedPipe getPipe(IBlockAccess blockAccess, BlockPos pos) {
		TileEntity tile = blockAccess.getTileEntity(pos);

		if (!(tile instanceof LogisticsTileGenericPipe) || tile.isInvalid()) {
			return null;
		} else {
			return ((LogisticsTileGenericPipe) tile).pipe;
		}
	}

	public static boolean isFullyDefined(CoreUnroutedPipe pipe) {
		return pipe != null && pipe.transport != null && pipe.container != null;
	}

	public static boolean isValid(CoreUnroutedPipe pipe) {
		return LogisticsBlockGenericPipe.isFullyDefined(pipe);
	}

	@Override
	public String getUnlocalizedName() {
		return "LogisticsPipes Pipe Block";
	}

	@Override
	public String getLocalizedName() {
		return getUnlocalizedName();
	}

	/**
	 * Spawn a digging particle effect in the world, this is a wrapper around
	 * EffectRenderer.addBlockHitEffects to allow the block more control over
	 * the particles. Useful when you have entirely different texture sheets for
	 * different sides/locations in the world.
	 *
	 * @param worldObj
	 *            The current world
	 * @param target
	 *            The target the player is looking at {x/y/z/side/sub}
	 * @param effectRenderer
	 *            A Reference to the current effect renderer.
	 * @return True to prevent vanilla digging particles form spawning.
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public boolean addHitEffects(World worldObj, MovingObjectPosition target, EffectRenderer effectRenderer) {
		int x = target.getBlockPos().getX();
		int y = target.getBlockPos().getY();
		int z = target.getBlockPos().getZ();

		CoreUnroutedPipe pipe = LogisticsBlockGenericPipe.getPipe(worldObj, utilBlockPos.getBlockposfromXYZ(x,y,z));
		if (pipe == null) {
			return false;
		}

		IIcon icon = pipe.getIconProvider().getIcon(pipe.getIconIndexForItem());

		int sideHit = utilEnumFacing.getIdfromEnum(target.sideHit);

		Block block = LogisticsPipes.LogisticsPipeBlock;
		float b = 0.1F;
		double px = x + rand.nextDouble() * (block.getBlockBoundsMaxX() - block.getBlockBoundsMinX() - (b * 2.0F)) + b + block.getBlockBoundsMinX();
		double py = y + rand.nextDouble() * (block.getBlockBoundsMaxY() - block.getBlockBoundsMinY() - (b * 2.0F)) + b + block.getBlockBoundsMinY();
		double pz = z + rand.nextDouble() * (block.getBlockBoundsMaxZ() - block.getBlockBoundsMinZ() - (b * 2.0F)) + b + block.getBlockBoundsMinZ();

		if (sideHit == 0) {
			py = y + block.getBlockBoundsMinY() - b;
		}

		if (sideHit == 1) {
			py = y + block.getBlockBoundsMaxY() + b;
		}

		if (sideHit == 2) {
			pz = z + block.getBlockBoundsMinZ() - b;
		}

		if (sideHit == 3) {
			pz = z + block.getBlockBoundsMaxZ() + b;
		}

		if (sideHit == 4) {
			px = x + block.getBlockBoundsMinX() - b;
		}

		if (sideHit == 5) {
			px = x + block.getBlockBoundsMaxX() + b;
		}

		EntityDiggingFX fx = new EntityDiggingFX(worldObj, px, py, pz, 0.0D, 0.0D, 0.0D, block, sideHit, worldObj.getBlockState(utilBlockPos.getBlockposfromXYZ(x,y,z)));
		fx.setParticleIcon(icon);
		effectRenderer.addEffect(fx.applyColourMultiplier(x, y, z).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
		return true;
	}

	/**
	 * Spawn particles for when the block is destroyed. Due to the nature of how
	 * this is invoked, the x/y/z locations are not always guaranteed to host
	 * your block. So be sure to do proper sanity checks before assuming that
	 * the location is this block.
	 *
	 * @param worldObj
	 *            The current world
	 * @param pos
	 *            Blockpos position to spawn the particle
	 * @param effectRenderer
	 *            A Reference to the current effect renderer.
	 * @return True to prevent vanilla break particles from spawning.
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public boolean addDestroyEffects(World worldObj, BlockPos pos, EffectRenderer effectRenderer) {
		CoreUnroutedPipe pipe = LogisticsBlockGenericPipe.getPipe(worldObj, pos);
		if (pipe == null) {
			return false;
		}

		TextureAtlasSprite icon = pipe.getIconProvider().getIcon(pipe.getIconIndexForItem());

		byte its = 4;
		for (int i = 0; i < its; ++i) {
			for (int j = 0; j < its; ++j) {
				for (int k = 0; k < its; ++k) {
					double px = pos.getX() + (i + 0.5D) / its;
					double py = pos.getY() + (j + 0.5D) / its;
					double pz = pos.getZ() + (k + 0.5D) / its;
					int random = rand.nextInt(6);
					EntityDiggingFX fx = new EntityDiggingFX(worldObj, px, py, pz, px - pos.getX() - 0.5D, py - pos.getY() - 0.5D, pz - pos.getZ() - 0.5D, LogisticsPipes.LogisticsPipeBlock, random, meta);
					fx.setParticleIcon(icon);
					effectRenderer.addEffect(fx.applyColourMultiplier(x, y, z));
				}
			}
		}
		return true;
	}

	private static void cacheTileToPreventRemoval(CoreUnroutedPipe pipe) {
		final World worldCache = pipe.getWorld();
		final BlockPos pos = pipe.getblockpos() ;
		final TileEntity tileCache = pipe.container;
		final CoreUnroutedPipe fPipe = pipe;
		fPipe.setPreventRemove(true);
		QueuedTasks.queueTask(new Callable<Object>() {

			@Override
			public Object call() throws Exception {
				if (!fPipe.preventRemove()) {
					return null;
				}
				boolean changed = false;
				if (worldCache.getBlock(xCache, yCache, zCache) != LogisticsPipes.LogisticsPipeBlock) {
					worldCache.setBlock(xCache, yCache, zCache, LogisticsPipes.LogisticsPipeBlock);
					changed = true;
				}
				if (worldCache.getTileEntity(pos) != tileCache) {
					worldCache.setTileEntity(pos, tileCache);
					changed = true;
				}
				if (changed) {
					worldCache.notifyBlockOfStateChange(pos, LogisticsPipes.LogisticsPipeBlock);
				}
				fPipe.setPreventRemove(false);
				return null;
			}
		});
	}
}
