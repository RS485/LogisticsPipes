package logisticspipes.pipes.basic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;

import static logisticspipes.LPConstants.PIPE_MAX_POS;
import static logisticspipes.LPConstants.PIPE_MIN_POS;
import lombok.AllArgsConstructor;
import lombok.Data;

import logisticspipes.LPBlocks;
import logisticspipes.LogisticsPipes;
import logisticspipes.config.Configs;
import logisticspipes.interfaces.IRotationProvider;
import logisticspipes.interfaces.ITubeOrientation;
import logisticspipes.items.ItemLogisticsPipe;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.pipes.basic.ltgpmodcompat.LPMicroblockBlock;
import logisticspipes.proxy.MainProxy;
import logisticspipes.renderer.newpipe.LogisticsNewRenderPipe;
import logisticspipes.renderer.newpipe.PropertyCache;
import logisticspipes.renderer.newpipe.PropertyRenderList;
import logisticspipes.ticks.QueuedTasks;
import logisticspipes.utils.LPPositionSet;
import network.rs485.logisticspipes.config.ClientConfiguration;
import network.rs485.logisticspipes.proxy.mcmp.BlockAccessDelegate;
import network.rs485.logisticspipes.world.DoubleCoordinates;
import network.rs485.logisticspipes.world.DoubleCoordinatesType;

public class LogisticsBlockGenericPipe extends LPMicroblockBlock {

	public static boolean ignoreSideRayTrace = false;
	public static Map<Item, Function<Item, ? extends CoreUnroutedPipe>> pipes = new HashMap<>();
	public static Map<DoubleCoordinates, CoreUnroutedPipe> pipeRemoved = new HashMap<>();
	public static Map<DoubleCoordinates, BlockPos> pipeSubMultiRemoved = new HashMap<>();
	private static long lastRemovedDate = -1;
	protected final Random rand = new Random();

	public static final PropertyInteger rotationProperty = PropertyInteger.create("rotation", 0, 3);
	public static final PropertyEnum<PipeRenderModel> modelTypeProperty = PropertyEnum.create("model_type", PipeRenderModel.class);
	public static final Map<EnumFacing, PropertyBool> connectionPropertys = Arrays.stream(EnumFacing.values()).collect(Collectors
			.toMap(key -> key, key -> PropertyBool.create("connection_" + key.ordinal())));

	public static final PropertyRenderList propertyRenderList = new PropertyRenderList();
	public static final PropertyCache propertyCache = new PropertyCache();

	public static final AxisAlignedBB PIPE_CENTER_BB = new AxisAlignedBB(PIPE_MIN_POS, PIPE_MIN_POS, PIPE_MIN_POS, PIPE_MAX_POS, PIPE_MAX_POS, PIPE_MAX_POS);
	public static final List<AxisAlignedBB> PIPE_CONN_BB = Arrays.asList(
			new AxisAlignedBB(PIPE_MIN_POS, 0, PIPE_MIN_POS, PIPE_MAX_POS, PIPE_MIN_POS, PIPE_MAX_POS),
			new AxisAlignedBB(PIPE_MIN_POS, PIPE_MAX_POS, PIPE_MIN_POS, PIPE_MAX_POS, 1, PIPE_MAX_POS),
			new AxisAlignedBB(PIPE_MIN_POS, PIPE_MIN_POS, 0, PIPE_MAX_POS, PIPE_MAX_POS, PIPE_MIN_POS),
			new AxisAlignedBB(PIPE_MIN_POS, PIPE_MIN_POS, PIPE_MAX_POS, PIPE_MAX_POS, PIPE_MAX_POS, 1),
			new AxisAlignedBB(0, PIPE_MIN_POS, PIPE_MIN_POS, PIPE_MIN_POS, PIPE_MAX_POS, PIPE_MAX_POS),
			new AxisAlignedBB(PIPE_MAX_POS, PIPE_MIN_POS, PIPE_MIN_POS, 1, PIPE_MAX_POS, PIPE_MAX_POS)
	);

	public enum PipeRenderModel implements IStringSerializable {
		NONE,
		REQUEST_TABLE;

		@Override
		public String getName() {
			return name().toLowerCase();
		}
	}

	public LogisticsBlockGenericPipe() {
		super(Material.GLASS);
		IBlockState state = this.blockState.getBaseState()
				.withProperty(rotationProperty, 0)
				.withProperty(modelTypeProperty, PipeRenderModel.NONE);
		connectionPropertys.values().forEach(it -> state.withProperty(it, false));
		setDefaultState(state);
		setCreativeTab(LogisticsPipes.CREATIVE_TAB_LP);
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

		if (LogisticsBlockGenericPipe.lastRemovedDate != world.getTotalWorldTime()) {
			LogisticsBlockGenericPipe.lastRemovedDate = world.getTotalWorldTime();
			LogisticsBlockGenericPipe.pipeRemoved.clear();
			LogisticsBlockGenericPipe.pipeSubMultiRemoved.clear();
		}

		if (pipe.isMultiBlock()) {
			if (pipe.preventRemove()) {
				throw new UnsupportedOperationException("A multi block can't be protected against removal.");
			}
			LPPositionSet<DoubleCoordinatesType<CoreMultiBlockPipe.SubBlockTypeForShare>> list = ((CoreMultiBlockPipe) pipe).getRotatedSubBlocks();
			list.forEach(pos -> pos.add(new DoubleCoordinates(pipe)));
			for (DoubleCoordinates pos : pipe.container.subMultiBlock) {
				TileEntity tile = pos.getTileEntity(world);
				if (tile instanceof LogisticsTileGenericSubMultiBlock) {
					DoubleCoordinatesType<CoreMultiBlockPipe.SubBlockTypeForShare> equ = list.findClosest(pos);
					if (equ != null) {
						((LogisticsTileGenericSubMultiBlock) tile).removeSubType(equ.getType());
					}
					if (((LogisticsTileGenericSubMultiBlock) tile).removeMainPipe(new DoubleCoordinates(pipe))) {
						LogisticsBlockGenericSubMultiBlock.redirectedToMainPipe = true;
						pos.setBlockToAir(world);
						LogisticsBlockGenericSubMultiBlock.redirectedToMainPipe = false;
						LogisticsBlockGenericPipe.pipeSubMultiRemoved.put(new DoubleCoordinates(pos), pipe.container.getPos());
					} else {
						MainProxy.sendPacketToAllWatchingChunk(tile, ((LogisticsTileGenericSubMultiBlock) tile).getLPDescriptionPacket());
					}
				}
			}
		}

		BlockPos pos = pipe.container.getPos();
		LogisticsBlockGenericPipe.pipeRemoved.put(new DoubleCoordinates(pos), pipe);
		world.removeTileEntity(pos);
	}

	/* Registration ******************************************************** */
	public static ItemLogisticsPipe registerPipe(IForgeRegistry<Item> registry, String name, Function<Item, ? extends CoreUnroutedPipe> constructor) {
		ItemLogisticsPipe item = new ItemLogisticsPipe();
		LogisticsPipes.setName(item, String.format("pipe_%s", name));

		LogisticsBlockGenericPipe.pipes.put(item, constructor);

		CoreUnroutedPipe dummyPipe = LogisticsBlockGenericPipe.createPipe(item);
		if (dummyPipe != null) {
			item.setPipeIconIndex(dummyPipe.getIconIndexForItem(), dummyPipe.getTextureIndex());
			MainProxy.proxy.setIconProviderFromPipe(item, dummyPipe);
			item.setDummyPipe(dummyPipe);
		}

		registry.register(item);
		return item;
	}

	public static CoreUnroutedPipe createPipe(Item key) {
		Function<Item, ? extends CoreUnroutedPipe> pipe = LogisticsBlockGenericPipe.pipes.get(key);
		if (pipe != null) {
			return pipe.apply(key);
		} else {
			LogisticsPipes.log.warn("Detected pipe with unknown key (" + key + "). This should not have happend.");
		}

		return null;
	}

	public static boolean placePipe(CoreUnroutedPipe pipe, World world, BlockPos blockPos, Block block) {
		return LogisticsBlockGenericPipe.placePipe(pipe, world, blockPos, block, null);
	}

	public static boolean placePipe(CoreUnroutedPipe pipe, World world, BlockPos blockPos, Block block, ITubeOrientation orientation) {
		IBlockState oldBlockState = world.getBlockState(blockPos);
		boolean placed = world.setBlockState(blockPos, block.getDefaultState(), 0);

		if (world.isRemote) {
			return placed;
		}

		if (placed) {
			TileEntity tile = world.getTileEntity(blockPos);
			if (tile instanceof LogisticsTileGenericPipe) {
				LogisticsTileGenericPipe tilePipe = (LogisticsTileGenericPipe) tile;
				if (pipe instanceof CoreMultiBlockPipe) {
					if (orientation == null) {
						throw new NullPointerException();
					}
					CoreMultiBlockPipe mPipe = (CoreMultiBlockPipe) pipe;
					orientation.setOnPipe(mPipe);
					DoubleCoordinates placeAt = new DoubleCoordinates(blockPos);
					LogisticsBlockGenericSubMultiBlock.currentCreatedMultiBlock = placeAt;
					LPPositionSet<DoubleCoordinatesType<CoreMultiBlockPipe.SubBlockTypeForShare>> positions = ((CoreMultiBlockPipe) pipe).getSubBlocks();
					orientation.rotatePositions(positions);
					for (DoubleCoordinatesType<CoreMultiBlockPipe.SubBlockTypeForShare> pos : positions) {
						pos.add(placeAt);
						TileEntity subTile = world.getTileEntity(pos.getBlockPos());
						IBlockState oldSubBlockState = world.getBlockState(pos.getBlockPos());
						if (subTile instanceof LogisticsTileGenericSubMultiBlock) {
							((LogisticsTileGenericSubMultiBlock) subTile).addMultiBlockMainPos(placeAt);
							((LogisticsTileGenericSubMultiBlock) subTile).addSubTypeTo(pos.getType());
							MainProxy.sendPacketToAllWatchingChunk(subTile, ((LogisticsTileGenericSubMultiBlock) subTile).getLPDescriptionPacket());
						} else {
							world.setBlockState(pos.getBlockPos(), LPBlocks.subMultiblock.getDefaultState(), 0);
							subTile = world.getTileEntity(pos.getBlockPos());
							if (subTile instanceof LogisticsTileGenericSubMultiBlock) {
								((LogisticsTileGenericSubMultiBlock) subTile).addSubTypeTo(pos.getType());
							}
						}
						world.markAndNotifyBlock(pos.getBlockPos(), world.getChunkFromBlockCoords(pos.getBlockPos()), oldSubBlockState, world.getBlockState(pos.getBlockPos()), 3);
					}
					LogisticsBlockGenericSubMultiBlock.currentCreatedMultiBlock = null;
				}
				tilePipe.initialize(pipe);
				//				tilePipe.sendUpdateToClient();
			}
			world.markAndNotifyBlock(blockPos, world.getChunkFromBlockCoords(blockPos), oldBlockState, world.getBlockState(blockPos), 3);
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

	private static void cacheTileToPreventRemoval(CoreUnroutedPipe pipe) {
		final World worldCache = pipe.getWorld();
		final BlockPos posCache = pipe.getPos();
		final TileEntity tileCache = pipe.container;
		final CoreUnroutedPipe fPipe = pipe;
		fPipe.setPreventRemove(true);
		QueuedTasks.queueTask(() -> {
			if (!fPipe.preventRemove()) {
				return null;
			}
			boolean changed = false;
			if (worldCache.getBlockState(posCache) != null || worldCache.getBlockState(posCache).getBlock() != LPBlocks.pipe) {
				worldCache.setBlockState(posCache, LPBlocks.pipe.getDefaultState());
				changed = true;
			}
			if (worldCache.getTileEntity(posCache) != tileCache) {
				worldCache.setTileEntity(posCache, tileCache);
				changed = true;
			}
			if (changed) {
				worldCache.markAndNotifyBlock(posCache, worldCache.getChunkFromBlockCoords(posCache), worldCache.getBlockState(posCache), worldCache.getBlockState(posCache), 3);
			}
			fPipe.setPreventRemove(false);
			return null;
		});
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> getDrops(@Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune) {
		NonNullList<ItemStack> list = NonNullList.create();
		if (MainProxy.isClient(world)) {
			return list;
		}

		Random rand = world instanceof World ? ((World) world).rand : RANDOM;
		int count = quantityDropped(state, fortune, rand);
		for (int i = 0; i < count; i++) {
			CoreUnroutedPipe pipe = LogisticsBlockGenericPipe.getPipe(world, pos);

			if (pipe == null) {
				pipe = LogisticsBlockGenericPipe.pipeRemoved.get(new DoubleCoordinates(pos));
			}

			if (pipe != null) {
				if (pipe.item != null && (pipe.canBeDestroyed() || pipe.destroyByPlayer())) {
					list.addAll(pipe.dropContents());
					list.add(new ItemStack(pipe.item, 1, damageDropped(state)));
				} else if (pipe.item != null) {
					LogisticsBlockGenericPipe.cacheTileToPreventRemoval(pipe);
				}
			}
		}
		mcmpBlockAccess.addDrops(list, world, pos, state, fortune);
		return list;
	}

	@Nonnull
	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

	public void addCollisionBoxToList(LogisticsTileGenericPipe pipe, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean isActualState) {
		addCollisionBoxToList(pipe.getWorld().getBlockState(pipe.getPos()), pipe.getWorld(), pipe.getPos(), entityBox, collidingBoxes, entityIn, isActualState);
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, @Nonnull BlockPos pos, @Nonnull AxisAlignedBB entityBox, @Nonnull List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity, boolean isActualState) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof LogisticsTileGenericPipe) {
			LogisticsTileGenericPipe tile = (LogisticsTileGenericPipe) te;
			CoreUnroutedPipe pipe = tile.pipe;
			if (pipe != null && pipe.isPipeBlock()) {
				addCollisionBoxToList(pos, entityBox, collidingBoxes, Block.FULL_BLOCK_AABB);
				return;
			}
			if (pipe != null && pipe.isMultiBlock()) {
				((CoreMultiBlockPipe) pipe).addCollisionBoxesToList(collidingBoxes, entityBox);
				if (!pipe.actAsNormalPipe()) return;
			}

			Arrays.stream(EnumFacing.VALUES)
					.filter(tile::isPipeConnectedCached)
					.map(f -> PIPE_CONN_BB.get(f.getIndex()))
					.forEach(bb -> addCollisionBoxToList(pos, entityBox, collidingBoxes, bb));
		}
		addCollisionBoxToList(pos, entityBox, collidingBoxes, PIPE_CENTER_BB);
		mcmpBlockAccess.addCollisionBoxToList(state, world, pos, entityBox, collidingBoxes, entity, isActualState);
	}

	@Nonnull
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, @Nonnull BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe) tile).isPipeBlock()) {
			return new AxisAlignedBB((double) pos.getX() + 0, (double) pos.getY() + 0, (double) pos.getZ() + 0,
					(double) pos.getX() + 1, (double) pos.getY() + 1, (double) pos.getZ() + 1);
		}
		InternalRayTraceResult rayTraceResult = doRayTrace(world, pos, Minecraft.getMinecraft().player);

		if (rayTraceResult != null && rayTraceResult.boundingBox != null) {
			AxisAlignedBB box = rayTraceResult.boundingBox;
			if (rayTraceResult.hitPart == Part.PIPE) {
				float scale = 0.001F;
				box = box.expand(scale, scale, scale);
			}
			return box.offset(pos);
		}
		return super.getSelectedBoundingBox(state, world, pos);
	}

	@Override
	public RayTraceResult collisionRayTrace(IBlockState state, World world, @Nonnull BlockPos pos, @Nonnull Vec3d start, @Nonnull Vec3d end) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe) tile).isPipeBlock()) {
			Vec3d vec3d = start.subtract(pos.getX(), pos.getY(), pos.getZ());
			Vec3d vec3d1 = end.subtract(pos.getX(), pos.getY(), pos.getZ());
			RayTraceResult raytraceresult = FULL_BLOCK_AABB.calculateIntercept(vec3d, vec3d1);
			return raytraceresult == null ? null : new RayTraceResult(raytraceresult.hitVec.addVector(pos.getX(), pos.getY(), pos.getZ()), raytraceresult.sideHit, pos);
		}
		if (tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe) tile).pipe == null) { // Fallback for defect pipe
			return rayTrace(pos, start, end, Block.FULL_BLOCK_AABB);
		}

		InternalRayTraceResult result = doRayTrace(world, pos, start, end);

		if (result == null) {
			return mcmpBlockAccess.collisionRayTrace(state, world, pos, start, end);
		} else {
			RayTraceResult secondResult = mcmpBlockAccess.collisionRayTrace(state, world, pos, start, end);
			if (secondResult != null) {
				if (secondResult.hitVec.distanceTo(start) < result.rayTraceResult.hitVec.distanceTo(start)) {
					return secondResult;
				}
			}
			return result.rayTraceResult;
		}
	}

	public InternalRayTraceResult doRayTrace(World world, BlockPos pos, EntityPlayer player) {
		double reachDistance = 5;

		if (player instanceof EntityPlayerMP) {
			reachDistance = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
		}

		double eyeHeight = player.getEyeHeight();
		Vec3d lookVec = player.getLookVec();
		Vec3d start = new Vec3d(player.posX, player.posY + eyeHeight, player.posZ);
		Vec3d end = start.addVector(lookVec.x * reachDistance, lookVec.y * reachDistance, lookVec.z * reachDistance);

		return doRayTrace(world, pos, start, end);
	}

	public InternalRayTraceResult doRayTrace(World world, BlockPos pos, Vec3d start, Vec3d end) {
		TileEntity te = world.getTileEntity(pos);

		if (te instanceof LogisticsTileGenericPipe) {
			LogisticsTileGenericPipe tileG = (LogisticsTileGenericPipe) te;
			CoreUnroutedPipe pipe = tileG.pipe;
			if (!LogisticsBlockGenericPipe.isValid(pipe)) return null;

			if (pipe.isMultiBlock()) {
				InternalRayTraceResult result1 = doRayTraceMultiblock(tileG, (CoreMultiBlockPipe) pipe, start, end);

				if (!pipe.actAsNormalPipe())
					return result1;

				InternalRayTraceResult result2 = doRayTrace(tileG, pipe, start, end);

				return Stream.of(result1, result2)
						.filter(Objects::nonNull)
						.min(Comparator.comparing(r -> r.rayTraceResult.hitVec.squareDistanceTo(start)))
						.orElse(null);
			} else {
				return doRayTrace(tileG, pipe, start, end);
			}
		}
		return null;
	}

	@Data
	@AllArgsConstructor
	private static class Hit {

		public RayTraceResult rayTraceResult;
		public AxisAlignedBB box;
		public EnumFacing side;
		public Part part;
	}

	private InternalRayTraceResult doRayTrace(LogisticsTileGenericPipe tileG, CoreUnroutedPipe pipe, Vec3d start, Vec3d end) {
		if (tileG == null) return null;
		if (!LogisticsBlockGenericPipe.isValid(pipe)) return null;

		/*
		 * pipe hits along x, y, and z axis, gate (all 6 sides) [and
		 * wires+facades]
		 */
		ArrayList<Hit> list = new ArrayList<>();

		// pipe
		for (EnumFacing side : LogisticsBlockGenericPipe.DIR_VALUES) {
			if (side == null || tileG.isPipeConnectedCached(side)) {
				if (side != null && ignoreSideRayTrace) continue;
				AxisAlignedBB bb = getPipeBoundingBox(side);
				list.add(new Hit(rayTrace(tileG.getPos(), start, end, bb), bb, side, Part.PIPE));
			}
		}

		// pluggables

		/*
		for (EnumFacing side : EnumFacing.VALUES) {
			if (tileG.getBCPipePluggable(side) != null) {
				if(side != null && ignoreSideRayTrace) continue;
				AxisAlignedBB bb = tileG.getBCPipePluggable(side).getBoundingBox(side);
				boxes[7 + side.ordinal()] = bb;
				hits[7 + side.ordinal()] = super.collisionRayTrace(new BoundingBoxDelegateBlockState(bb, state), tileG.getWorld(), tileG.getPos(), start, end);
				sideHit[7 + side.ordinal()] = side;
			}
		}
		*/

		// TODO: check wires

		// get closest hit

		return list.stream()
				.filter(r -> r.rayTraceResult != null)
				.min(Comparator.comparing(r -> r.rayTraceResult.hitVec.squareDistanceTo(start)))
				.map(r -> new InternalRayTraceResult(r.part, r.rayTraceResult, r.box, r.side))
				.orElse(null);
	}

	private InternalRayTraceResult doRayTraceMultiblock(LogisticsTileGenericPipe tileG, CoreMultiBlockPipe pipe, Vec3d start, Vec3d direction) {
		if (tileG == null) return null;
		if (!LogisticsBlockGenericPipe.isValid(pipe)) return null;

		List<RayTraceResult> hits = new ArrayList<>();
		List<AxisAlignedBB> boxes = new ArrayList<>();

		pipe.addCollisionBoxesToList(boxes, null);

		while (hits.size() < boxes.size()) {
			hits.add(null);
		}

		for (int i = 0; i < boxes.size(); i++) {
			AxisAlignedBB bb = boxes.get(i);
			hits.set(i, super.rayTrace(tileG.getPos(), start, direction, bb.offset(BlockPos.ORIGIN.subtract(tileG.getPos()))));
		}

		return hits.stream()
				.filter(Objects::nonNull)
				.min(Comparator.comparing(r -> r.hitVec.squareDistanceTo(start)))
				.map(r -> new InternalRayTraceResult(Part.PIPE, r, pipe.getCompleteBox(), null))
				.orElse(null);
	}

	private AxisAlignedBB getPipeBoundingBox(@Nullable EnumFacing side) {
		if (side == null) return PIPE_CENTER_BB;
		return PIPE_CONN_BB.get(side.getIndex());
	}

	@Override
	public TileEntity createNewTileEntity(@Nonnull World world, int metadata) {
		return new LogisticsTileGenericPipe();
	}

	public enum Part {
		PIPE,
		UNKNOWN
	}

	public static class InternalRayTraceResult {

		public final Part hitPart;
		public final RayTraceResult rayTraceResult;
		public final AxisAlignedBB boundingBox;
		public final EnumFacing sideHit;

		InternalRayTraceResult(Part hitPart, RayTraceResult rayTraceResult, AxisAlignedBB boundingBox, EnumFacing side) {
			this.hitPart = hitPart;
			this.rayTraceResult = rayTraceResult;
			this.boundingBox = boundingBox;
			sideHit = side;
		}

		@Override
		public String toString() {
			return String.format("RayTraceResult: %s, %s", hitPart == null ? "null" : hitPart.name(), boundingBox == null ? "null" : boundingBox.toString());
		}
	}

	private static final EnumFacing[] DIR_VALUES;

	static {
		DIR_VALUES = new EnumFacing[EnumFacing.VALUES.length + 1];
		DIR_VALUES[0] = null;
		System.arraycopy(EnumFacing.VALUES, 0, DIR_VALUES, 1, EnumFacing.VALUES.length);
	}

	@Override
	public float getBlockHardness(IBlockState state, World par1World, BlockPos pos) {
		return Configs.pipeDurability;
	}

	@Nonnull
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Nonnull
	@Override
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean isFullBlock(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isNormalCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isTopSolid(IBlockState state) {
		return false;
	}

	@Override
	public boolean canBeReplacedByLeaves(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos) {
		return false;
	}

	@Override
	public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		TileEntity tile = world.getTileEntity(pos);

		if (tile instanceof LogisticsTileGenericPipe) {
			if (((LogisticsTileGenericPipe) tile).isPipeBlock()) {
				return true;
			}
		}

		return super.isSideSolid(state, world, pos, side);
	}

	@Override
	public void breakBlock(World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		LogisticsBlockGenericPipe.removePipe(LogisticsBlockGenericPipe.getPipe(world, pos));
		super.breakBlock(world, pos, state);
	}

	@Override
	public void dropBlockAsItemWithChance(World world, @Nonnull final BlockPos pos, @Nonnull IBlockState state, float chance, int fortune) {

		if (world.isRemote) {
			return;
		}

		int i1 = quantityDropped(world.rand);
		for (int j1 = 0; j1 < i1; j1++) {
			if (world.rand.nextFloat() > chance) {
				continue;
			}

			CoreUnroutedPipe pipe = LogisticsBlockGenericPipe.getPipe(world, pos);

			if (pipe == null) {
				pipe = LogisticsBlockGenericPipe.pipeRemoved.get(new DoubleCoordinates(pos));
			}

			if (pipe == null) return;

			if (pipe.item != null && (pipe.canBeDestroyed() || pipe.destroyByPlayer())) {
				for (ItemStack stack : pipe.dropContents()) {
					spawnAsEntity(world, pos, stack);
				}
				spawnAsEntity(world, pos, new ItemStack(pipe.item, 1, damageDropped(state)));
				final NonNullList<ItemStack> list = NonNullList.create();
				CoreUnroutedPipe finalPipe = pipe;
				BlockAccessDelegate worldDelegate = new BlockAccessDelegate(world) {

					@Override
					public TileEntity getTileEntity(BlockPos testPos) {
						if (pos == testPos) {
							return finalPipe.container;
						}
						return super.getTileEntity(pos);
					}
				};
				mcmpBlockAccess.addDrops(list, worldDelegate, pos, state, fortune);
				for (ItemStack stack : list) {
					spawnAsEntity(world, pos, stack);
				}
			} else if (pipe.item != null) {
				LogisticsBlockGenericPipe.cacheTileToPreventRemoval(pipe);
			}
		}
	}

	@Nonnull
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		// Returns null to be safe - the id does not depend on the meta
		return null;
	}

	@SideOnly(Side.CLIENT)
	@Override
	@Nonnull
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		ItemStack pick = super.getPickBlock(state, target, world, pos, player);
		if (!pick.isEmpty()) {
			return pick;
		}
		InternalRayTraceResult rayTraceResult = doRayTrace(world, pos, player);

		if (rayTraceResult != null && rayTraceResult.boundingBox != null) {
			if (rayTraceResult.hitPart == Part.PIPE) {
				final CoreUnroutedPipe pipe = Objects.requireNonNull(LogisticsBlockGenericPipe.getPipe(world, pos));
				return new ItemStack(pipe.item);
			}
		}
		return ItemStack.EMPTY;
	}

	/* Wrappers ************************************************************ */
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos);

		CoreUnroutedPipe pipe = LogisticsBlockGenericPipe.getPipe(worldIn, pos);

		if (LogisticsBlockGenericPipe.isValid(pipe)) {
			pipe.container.scheduleNeighborChange();
		}
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, @Nonnull ItemStack stack) {
		super.onBlockPlacedBy(world, pos, state, placer, stack);
		CoreUnroutedPipe pipe = LogisticsBlockGenericPipe.getPipe(world, pos);

		if (LogisticsBlockGenericPipe.isValid(pipe)) {
			pipe.onBlockPlaced();
			pipe.onBlockPlacedBy(placer);
			if (pipe instanceof IRotationProvider) {
				((IRotationProvider) pipe).setFacing(placer.getHorizontalFacing().getOpposite());
			}
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float xOffset, float yOffset, float zOffset) {
		if (super.onBlockActivated(world, pos, state, player, hand, side, xOffset, yOffset, zOffset)) return true;

		ItemStack heldItem = player.inventory.mainInventory.get(player.inventory.currentItem);

		//world.notifyBlocksOfNeighborChange(pos, LogisticsPipes.LogisticsPipeBlock);
		CoreUnroutedPipe pipe = LogisticsBlockGenericPipe.getPipe(world, pos);

		if (LogisticsBlockGenericPipe.isValid(pipe)) {

			if (heldItem.isEmpty()) {
				// Fall through the end of the test
			} else if (heldItem.getItem() == Items.SIGN) {
				// Sign will be placed anyway, so lets show the sign gui
				return false;
			} else if (heldItem.getItem() instanceof ItemLogisticsPipe) {
				return false;
			}
			return pipe.blockActivated(player);
		}

		return false;
	}

	@Override
	public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
		super.onEntityCollidedWithBlock(world, pos, state, entity);

		CoreUnroutedPipe pipe = LogisticsBlockGenericPipe.getPipe(world, pos);

		if (LogisticsBlockGenericPipe.isValid(pipe)) {
			pipe.onEntityCollidedWithBlock(entity);
		}
	}

	@Override
	public boolean canProvidePower(IBlockState state) {
		return true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean addHitEffects(IBlockState state, World world, RayTraceResult target, ParticleManager effectRenderer) {
		if (super.addHitEffects(state, world, target, effectRenderer)) return true;
		BlockPos pos = target.getBlockPos();

		CoreUnroutedPipe pipe = LogisticsBlockGenericPipe.getPipe(world, pos);
		if (pipe == null) {
			return false;
		}

		TextureAtlasSprite icon = pipe.getIconProvider().getIcon(pipe.getIconIndexForItem());

		EnumFacing sideHit = target.sideHit;

		Block block = LPBlocks.pipe;
		float b = 0.1F;
		double px = target.hitVec.x + rand.nextDouble() * (state.getBoundingBox(world, pos).maxX - state.getBoundingBox(world, pos).minX - (b * 2.0F)) + b + state.getBoundingBox(world, pos).minX;
		double py = target.hitVec.y + rand.nextDouble() * (state.getBoundingBox(world, pos).maxY - state.getBoundingBox(world, pos).minY - (b * 2.0F)) + b + state.getBoundingBox(world, pos).minY;
		double pz = target.hitVec.z + rand.nextDouble() * (state.getBoundingBox(world, pos).maxZ - state.getBoundingBox(world, pos).minZ - (b * 2.0F)) + b + state.getBoundingBox(world, pos).minZ;

		if (sideHit == EnumFacing.DOWN) {
			py = target.hitVec.y + state.getBoundingBox(world, pos).minY - b;
		}

		if (sideHit == EnumFacing.UP) {
			py = target.hitVec.y + state.getBoundingBox(world, pos).maxY + b;
		}

		if (sideHit == EnumFacing.NORTH) {
			pz = target.hitVec.z + state.getBoundingBox(world, pos).minZ - b;
		}

		if (sideHit == EnumFacing.SOUTH) {
			pz = target.hitVec.z + state.getBoundingBox(world, pos).maxZ + b;
		}

		if (sideHit == EnumFacing.EAST) {
			px = target.hitVec.x + state.getBoundingBox(world, pos).minX - b;
		}

		if (sideHit == EnumFacing.WEST) {
			px = target.hitVec.x + state.getBoundingBox(world, pos).maxX + b;
		}

		Particle fx = effectRenderer.spawnEffectParticle(EnumParticleTypes.BLOCK_CRACK.getParticleID(), px, py, pz, 0.0D, 0.0D, 0.0D, Block.getStateId(world.getBlockState(target.getBlockPos())));
		fx.setParticleTexture(icon);
		effectRenderer.addEffect(fx.multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
		return true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager effectRenderer) {
		if (super.addDestroyEffects(world, pos, effectRenderer)) return true;
		CoreUnroutedPipe pipe = LogisticsBlockGenericPipe.getPipe(world, pos);
		if (pipe == null) {
			return false;
		}

		ClientConfiguration config = LogisticsPipes.getClientPlayerConfig();
		//if (config.isUseNewRenderer()) {
		LogisticsNewRenderPipe.renderDestruction(pipe, world, pos.getX(), pos.getY(), pos.getZ(), effectRenderer);
		/*} else {
			TextureAtlasSprite icon = pipe.getIconProvider().getIcon(pipe.getIconIndexForItem());

			byte its = 4;
			for (int i = 0; i < its; ++i) {
				for (int j = 0; j < its; ++j) {
					for (int k = 0; k < its; ++k) {
						if (pipe.isMultiBlock()) {
							LPPositionSet<DoubleCoordinatesType<CoreMultiBlockPipe.SubBlockTypeForShare>> set = ((CoreMultiBlockPipe) pipe).getRotatedSubBlocks();
							set.add(new DoubleCoordinatesType<>(0, 0, 0, CoreMultiBlockPipe.SubBlockTypeForShare.NON_SHARE));
							for (DoubleCoordinates pos : set) {
								int localx = x + pos.getXInt();
								int localy = y + pos.getYInt();
								int localz = z + pos.getZInt();
								double px = localx + (i + 0.5D) / its;
								double py = localy + (j + 0.5D) / its;
								double pz = localz + (k + 0.5D) / its;
								int random = rand.nextInt(6);
								EntityDiggingFX fx = new EntityDiggingFX(world, px, py, pz, px - localx - 0.5D, py - localy - 0.5D, pz - localz - 0.5D, LogisticsPipes.LogisticsPipeBlock, random, meta);
								fx.setParticleIcon(icon);
								effectRenderer.addEffect(fx.applyColourMultiplier(pos));
							}
						} else {
							double px = x + (i + 0.5D) / its;
							double py = y + (j + 0.5D) / its;
							double pz = z + (k + 0.5D) / its;
							int random = rand.nextInt(6);
							EntityDiggingFX fx = new EntityDiggingFX(world, px, py, pz, px - x - 0.5D, py - y - 0.5D, pz - z - 0.5D, LogisticsPipes.LogisticsPipeBlock, random, meta);
							fx.setParticleIcon(icon);
							effectRenderer.addEffect(fx.applyColourMultiplier(pos));
						}
					}
				}
			}
		}
		*/
		return true;
	}

	@Nonnull
	@Override
	protected BlockStateContainer createBlockState() {
		BlockStateContainer.Builder builder = new BlockStateContainer.Builder(this);
		builder.add(rotationProperty);
		builder.add(modelTypeProperty);
		connectionPropertys.values().forEach(builder::add);

		builder.add(propertyRenderList);
		builder.add(propertyCache);

		mcmpBlockAccess.addBlockState(builder);

		return builder.build();
	}

	@Nonnull
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState();
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return 0;
	}

	@Nonnull
	@Override
	public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		checkForRenderChanges(worldIn, pos);
		state = super.getActualState(state, worldIn, pos);

		CoreUnroutedPipe pipe = LogisticsBlockGenericPipe.getPipe(worldIn, pos);

		if (LogisticsBlockGenericPipe.isValid(pipe)) {
			if (pipe instanceof IRotationProvider) {
				state = state.withProperty(rotationProperty, ((IRotationProvider) pipe).getRotation());
			}

			for (EnumFacing side : EnumFacing.VALUES) {
				state = state.withProperty(connectionPropertys.get(side), pipe.container.renderState.pipeConnectionMatrix.isConnected(side));
			}

			if (pipe instanceof PipeBlockRequestTable) {
				state = state.withProperty(modelTypeProperty, PipeRenderModel.REQUEST_TABLE);
			}
		}

		return state;
	}

	@Nonnull
	@Override
	public IBlockState getExtendedState(@Nonnull IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		state = mcmpBlockAccess.getExtendedState(state, worldIn, pos);

		CoreUnroutedPipe pipe = LogisticsBlockGenericPipe.getPipe(worldIn, pos);

		if (LogisticsBlockGenericPipe.isValid(pipe)) {
			LogisticsNewRenderPipe.checkAndCalculateRenderCache(pipe.container);
			state = ((IExtendedBlockState) state).withProperty(propertyRenderList, pipe.container.renderState.cachedRenderer);
			state = ((IExtendedBlockState) state).withProperty(propertyCache, pipe.container.renderState.objectCache);
		} else {
			state = ((IExtendedBlockState) state).withProperty(propertyRenderList, LogisticsNewRenderPipe.getBasicPipeFrameRenderList());
		}
		return state;
	}

	private void checkForRenderChanges(IBlockAccess worldIn, BlockPos blockPos) {
		TileEntity tile = new DoubleCoordinates(blockPos).getTileEntity(worldIn);
		if (!(tile instanceof LogisticsTileGenericPipe)) return;
		((LogisticsTileGenericPipe) tile).renderState.checkForRenderUpdate(worldIn, blockPos);
	}

	@Override
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		return true;
	}
}
