package logisticspipes.pipes.basic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;

import logisticspipes.LPConstants;
import logisticspipes.LogisticsPipes;
import logisticspipes.config.Configs;
import logisticspipes.items.ItemLogisticsPipe;
import logisticspipes.pipes.PipeBlockRequestTable;
import logisticspipes.proxy.MainProxy;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.renderer.LogisticsPipeWorldRenderer;
import logisticspipes.renderer.newpipe.LogisticsNewPipeWorldRenderer;
import logisticspipes.renderer.newpipe.LogisticsNewRenderPipe;
import logisticspipes.textures.Textures;
import logisticspipes.ticks.QueuedTasks;
import logisticspipes.utils.MatrixTranformations;
import logisticspipes.utils.TileBuffer;
import logisticspipes.utils.tuples.LPPosition;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class LogisticsBlockGenericPipe extends BlockContainer {

	public LogisticsBlockGenericPipe() {
		super(Material.glass);
		setRenderAllSides();
		setCreativeTab(null);
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		if (world.isRemote) {
			return null;
		}
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		int count = quantityDropped(metadata, fortune, world.rand);
		for (int i = 0; i < count; i++) {
			CoreUnroutedPipe pipe = getPipe(world, x, y, z);

			if (pipe == null) {
				pipe = pipeRemoved.get(new LPPosition(x, y, z));
			}

			if (pipe != null) {
				if (pipe.item != null && (pipe.canBeDestroyed() || pipe.destroyByPlayer())) {
					list.addAll(pipe.dropContents());
					list.add(new ItemStack(pipe.item, 1, damageDropped(metadata)));
				} else if(pipe.item != null) {
					cacheTileToPreventRemoval(pipe);
				}
			}
		}
		return list;
	}

	@Override
	@SideOnly(Side.CLIENT)
	@SuppressWarnings({"all"})
	public IIcon getIcon(IBlockAccess iblockaccess, int i, int j, int k, int l) {
		TileEntity tile = iblockaccess.getTileEntity(i, j, k);
		if (!(tile instanceof LogisticsTileGenericPipe)) {
			return null;
		}
		if(((LogisticsTileGenericPipe)tile).pipe instanceof PipeBlockRequestTable) {
			PipeBlockRequestTable table = (PipeBlockRequestTable) ((LogisticsTileGenericPipe)tile).pipe;
			return table.getTextureFor(l);
		}
		if (((LogisticsTileGenericPipe) tile).renderState.textureArray != null) {
			return ((LogisticsTileGenericPipe) tile).renderState.textureArray[l];
		}
		return ((LogisticsTileGenericPipe) tile).renderState.currentTexture;
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public void addCollisionBoxesToList(World world, int i, int j, int k, AxisAlignedBB axisalignedbb, List arraylist, Entity par7Entity) {
		TileEntity tile = world.getTileEntity(i, j, k);
		if(tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe)tile).pipe instanceof PipeBlockRequestTable) {
			setBlockBounds(0, 0, 0, 1, 1, 1);
			super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			return;
		}
		setBlockBounds(LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MAX_POS, LPConstants.PIPE_MAX_POS, LPConstants.PIPE_MAX_POS);
		super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
		if (tile instanceof LogisticsTileGenericPipe) {
			LogisticsTileGenericPipe tileG = (LogisticsTileGenericPipe) tile;

			if (tileG.isPipeConnected(ForgeDirection.WEST)) {
				setBlockBounds(0.0F, LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MAX_POS, LPConstants.PIPE_MAX_POS, LPConstants.PIPE_MAX_POS);
				super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			}

			if (tileG.isPipeConnected(ForgeDirection.EAST)) {
				setBlockBounds(LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MIN_POS, 1.0F, LPConstants.PIPE_MAX_POS, LPConstants.PIPE_MAX_POS);
				super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			}

			if (tileG.isPipeConnected(ForgeDirection.DOWN)) {
				setBlockBounds(LPConstants.PIPE_MIN_POS, 0.0F, LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MAX_POS, LPConstants.PIPE_MAX_POS, LPConstants.PIPE_MAX_POS);
				super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			}

			if (tileG.isPipeConnected(ForgeDirection.UP)) {
				setBlockBounds(LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MAX_POS, 1.0F, LPConstants.PIPE_MAX_POS);
				super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			}

			if (tileG.isPipeConnected(ForgeDirection.NORTH)) {
				setBlockBounds(LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MIN_POS, 0.0F, LPConstants.PIPE_MAX_POS, LPConstants.PIPE_MAX_POS, LPConstants.PIPE_MAX_POS);
				super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			}

			if (tileG.isPipeConnected(ForgeDirection.SOUTH)) {
				setBlockBounds(LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MAX_POS, LPConstants.PIPE_MAX_POS, 1.0F);
				super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			}

			float facadeThickness = LPConstants.FACADE_THICKNESS;

			if (tileG.tilePart.hasEnabledFacade(ForgeDirection.EAST)) {
				setBlockBounds(1 - facadeThickness, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
				super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			}

			if (tileG.tilePart.hasEnabledFacade(ForgeDirection.WEST)) {
				setBlockBounds(0.0F, 0.0F, 0.0F, facadeThickness, 1.0F, 1.0F);
				super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			}

			if (tileG.tilePart.hasEnabledFacade(ForgeDirection.UP)) {
				setBlockBounds(0.0F, 1 - facadeThickness, 0.0F, 1.0F, 1.0F, 1.0F);
				super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			}

			if (tileG.tilePart.hasEnabledFacade(ForgeDirection.DOWN)) {
				setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, facadeThickness, 1.0F);
				super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			}

			if (tileG.tilePart.hasEnabledFacade(ForgeDirection.SOUTH)) {
				setBlockBounds(0.0F, 0.0F, 1 - facadeThickness, 1.0F, 1.0F, 1.0F);
				super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			}

			if (tileG.tilePart.hasEnabledFacade(ForgeDirection.NORTH)) {
				setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, facadeThickness);
				super.addCollisionBoxesToList(world, i, j, k, axisalignedbb, arraylist, par7Entity);
			}
		}
		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z) {
		TileEntity tile = world.getTileEntity(x, y, z);
		if(tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe)tile).pipe instanceof PipeBlockRequestTable) {
			return AxisAlignedBB.getBoundingBox((double) x + 0, (double) y + 0, (double) z + 0, (double) x + 1, (double) y + 1, (double) z + 1);
		}
		RaytraceResult rayTraceResult = doRayTrace(world, x, y, z, Minecraft.getMinecraft().thePlayer);

		if (rayTraceResult != null && rayTraceResult.boundingBox != null) {
			AxisAlignedBB box = rayTraceResult.boundingBox;
			switch (rayTraceResult.hitPart) {
			case Gate:
			case Plug:
			case RobotStation: {
				float scale = 0.001F;
				box = box.expand(scale, scale, scale);
				break;
			}
			case Pipe: {
				float scale = 0.001F;
				box = box.expand(scale, scale, scale);
				break;
			}
			case Facade:
				break;
			}
			return box.getOffsetBoundingBox(x, y, z);
		}
		return super.getSelectedBoundingBoxFromPool(world, x, y, z).expand(-0.85F, -0.85F, -0.85F);
	}
	
	@Override
	public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 origin, Vec3 direction) {
		TileEntity tile = world.getTileEntity(x, y, z);
		if(tile instanceof LogisticsTileGenericPipe && ((LogisticsTileGenericPipe)tile).pipe instanceof PipeBlockRequestTable) {
			this.setBlockBoundsBasedOnState(world, x, y, z);
			origin = origin.addVector(( -x), ( -y), ( -z));
			direction = direction.addVector(( -x), ( -y), ( -z));
			this.setBlockBounds(0, 0, 0, 1, 1, 1);
			Vec3 vec32 = origin.getIntermediateWithXValue(direction, this.minX);
			Vec3 vec33 = origin.getIntermediateWithXValue(direction, this.maxX);
			Vec3 vec34 = origin.getIntermediateWithYValue(direction, this.minY);
			Vec3 vec35 = origin.getIntermediateWithYValue(direction, this.maxY);
			Vec3 vec36 = origin.getIntermediateWithZValue(direction, this.minZ);
			Vec3 vec37 = origin.getIntermediateWithZValue(direction, this.maxZ);
			if( !this.isVecInsideYZBounds(vec32)) {
				vec32 = null;
			}
			if( !this.isVecInsideYZBounds(vec33)) {
				vec33 = null;
			}
			if( !this.isVecInsideXZBounds(vec34)) {
				vec34 = null;
			}
			if( !this.isVecInsideXZBounds(vec35)) {
				vec35 = null;
			}
			if( !this.isVecInsideXYBounds(vec36)) {
				vec36 = null;
			}
			if( !this.isVecInsideXYBounds(vec37)) {
				vec37 = null;
			}
			Vec3 vec38 = null;
			if(vec32 != null && (vec38 == null || origin.squareDistanceTo(vec32) < origin.squareDistanceTo(vec38))) {
				vec38 = vec32;
			}
			if(vec33 != null && (vec38 == null || origin.squareDistanceTo(vec33) < origin.squareDistanceTo(vec38))) {
				vec38 = vec33;
			}
			if(vec34 != null && (vec38 == null || origin.squareDistanceTo(vec34) < origin.squareDistanceTo(vec38))) {
				vec38 = vec34;
			}
			if(vec35 != null && (vec38 == null || origin.squareDistanceTo(vec35) < origin.squareDistanceTo(vec38))) {
				vec38 = vec35;
			}
			if(vec36 != null && (vec38 == null || origin.squareDistanceTo(vec36) < origin.squareDistanceTo(vec38))) {
				vec38 = vec36;
			}
			if(vec37 != null && (vec38 == null || origin.squareDistanceTo(vec37) < origin.squareDistanceTo(vec38))) {
				vec38 = vec37;
			}
			if(vec38 == null) {
				return null;
			} else {
				byte b0 = -1;
				if(vec38 == vec32) {
					b0 = 4;
				}
				if(vec38 == vec33) {
					b0 = 5;
				}
				if(vec38 == vec34) {
					b0 = 0;
				}
				if(vec38 == vec35) {
					b0 = 1;
				}
				if(vec38 == vec36) {
					b0 = 2;
				}
				if(vec38 == vec37) {
					b0 = 3;
				}
				return new MovingObjectPosition(x, y, z, b0, vec38.addVector(x, y, z));
			}
		}
		RaytraceResult raytraceResult = doRayTrace(world, x, y, z, origin, direction);

		if (raytraceResult == null) {
			return null;
		} else {
			return raytraceResult.movingObjectPosition;
		}
	}

	public RaytraceResult doRayTrace(World world, int x, int y, int z, EntityPlayer player) {
		double reachDistance = 5;

		if (player instanceof EntityPlayerMP) {
			reachDistance = ((EntityPlayerMP) player).theItemInWorldManager.getBlockReachDistance();
		}

		double eyeHeight = world.isRemote ? player.getEyeHeight() - player.getDefaultEyeHeight() : player.getEyeHeight();
		Vec3 lookVec = player.getLookVec();
		Vec3 origin = Vec3.createVectorHelper(player.posX, player.posY + eyeHeight, player.posZ);
		Vec3 direction = origin.addVector(lookVec.xCoord * reachDistance, lookVec.yCoord * reachDistance, lookVec.zCoord * reachDistance);

		return doRayTrace(world, x, y, z, origin, direction);
	}

	private RaytraceResult doRayTrace(World world, int x, int y, int z, Vec3 origin, Vec3 direction) {
		TileEntity pipeTileEntity = world.getTileEntity(x, y, z);

		LogisticsTileGenericPipe tileG = null;
		if (pipeTileEntity instanceof LogisticsTileGenericPipe) {
			tileG = (LogisticsTileGenericPipe) pipeTileEntity;
		}

		if (tileG == null) {
			return null;
		}

		CoreUnroutedPipe pipe = tileG.pipe;

		if (!isValid(pipe)) {
			return null;
		}

		/**
		 * pipe hits along x, y, and z axis, gate (all 6 sides) [and
		 * wires+facades]
		 */
		MovingObjectPosition[] hits = new MovingObjectPosition[31];
		AxisAlignedBB[] boxes = new AxisAlignedBB[31];
		ForgeDirection[] sideHit = new ForgeDirection[31];
		Arrays.fill(sideHit, ForgeDirection.UNKNOWN);

		// pipe

		for (ForgeDirection side : DIR_VALUES) {
			if (side == ForgeDirection.UNKNOWN || tileG.isPipeConnected(side)) {
				AxisAlignedBB bb = getPipeBoundingBox(side);
				setBlockBounds(bb);
				boxes[side.ordinal()] = bb;
				hits[side.ordinal()] = super.collisionRayTrace(world, x, y, z, origin, direction);
				sideHit[side.ordinal()] = side;
			}
		}

		// gates

		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			if (pipe.hasGate(side)) {
				AxisAlignedBB bb = getGateBoundingBox(side);
				setBlockBounds(bb);
				boxes[7 + side.ordinal()] = bb;
				hits[7 + side.ordinal()] = super.collisionRayTrace(world, x, y, z, origin, direction);
				sideHit[7 + side.ordinal()] = side;
			}
		}

		// facades

		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			if (tileG.tilePart.hasFacade(side)) {
				AxisAlignedBB bb = getFacadeBoundingBox(side);
				setBlockBounds(bb);
				boxes[13 + side.ordinal()] = bb;
				hits[13 + side.ordinal()] = super.collisionRayTrace(world, x, y, z, origin, direction);
				sideHit[13 + side.ordinal()] = side;
			}
		}

		// plugs

		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			if (tileG.tilePart.hasPlug(side)) {
				AxisAlignedBB bb = getPlugBoundingBox(side);
				setBlockBounds(bb);
				boxes[19 + side.ordinal()] = bb;
				hits[19 + side.ordinal()] = super.collisionRayTrace(world, x, y, z, origin, direction);
				sideHit[19 + side.ordinal()] = side;
			}
		}

		// robotStations

		for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
			if (tileG.tilePart.hasRobotStation(side)) {
				AxisAlignedBB bb = getRobotStationBoundingBox(side);
				setBlockBounds(bb);
				boxes[25 + side.ordinal()] = bb;
				hits[25 + side.ordinal()] = super.collisionRayTrace(world, x, y, z, origin, direction);
				sideHit[25 + side.ordinal()] = side;
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
			} else if (minIndex < 13) {
				hitPart = Part.Gate;
			} else if (minIndex < 19) {
				hitPart = Part.Facade;
			} else if (minIndex < 25) {
				hitPart = Part.Plug;
			} else {
				hitPart = Part.RobotStation;
			}

			return new RaytraceResult(hitPart, hits[minIndex], boxes[minIndex], sideHit[minIndex]);
		}
	}

	private void setBlockBounds(AxisAlignedBB bb) {
		setBlockBounds((float) bb.minX, (float) bb.minY, (float) bb.minZ, (float) bb.maxX, (float) bb.maxY, (float) bb.maxZ);
	}

	private AxisAlignedBB getGateBoundingBox(ForgeDirection side) {
		float min = LPConstants.BC_PIPE_MIN_POS + 0.05F;
		float max = LPConstants.BC_PIPE_MAX_POS - 0.05F;

		float[][] bounds = new float[3][2];
		// X START - END
		bounds[0][0] = min;
		bounds[0][1] = max;
		// Y START - END
		bounds[1][0] = LPConstants.BC_PIPE_MIN_POS - 0.10F;
		bounds[1][1] = LPConstants.BC_PIPE_MIN_POS;
		// Z START - END
		bounds[2][0] = min;
		bounds[2][1] = max;

		MatrixTranformations.transform(bounds, side);
		return AxisAlignedBB.getBoundingBox(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]);
	}

	private AxisAlignedBB getFacadeBoundingBox(ForgeDirection side) {
		float[][] bounds = new float[3][2];
		// X START - END
		bounds[0][0] = 0.0F;
		bounds[0][1] = 1.0F;
		// Y START - END
		bounds[1][0] = 0.0F;
		bounds[1][1] = LPConstants.FACADE_THICKNESS;
		// Z START - END
		bounds[2][0] = 0.0F;
		bounds[2][1] = 1.0F;

		MatrixTranformations.transform(bounds, side);
		return AxisAlignedBB.getBoundingBox(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]);
	}

	private AxisAlignedBB getPlugBoundingBox(ForgeDirection side) {
		float[][] bounds = new float[3][2];
		// X START - END
		bounds[0][0] = 0.25F;
		bounds[0][1] = 0.75F;
		// Y START - END
		bounds[1][0] = 0.125F;
		bounds[1][1] = 0.251F;
		// Z START - END
		bounds[2][0] = 0.25F;
		bounds[2][1] = 0.75F;

		MatrixTranformations.transform(bounds, side);
		return AxisAlignedBB.getBoundingBox(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]);
	}

	private AxisAlignedBB getRobotStationBoundingBox(ForgeDirection side) {
		float[][] bounds = new float[3][2];
		// X START - END
		bounds[0][0] = 0.25F;
		bounds[0][1] = 0.75F;
		// Y START - END
		bounds[1][0] = 0.125F;
		bounds[1][1] = 0.251F;
		// Z START - END
		bounds[2][0] = 0.25F;
		bounds[2][1] = 0.75F;

		MatrixTranformations.transform(bounds, side);
		return AxisAlignedBB.getBoundingBox(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]);
	}

	private AxisAlignedBB getPipeBoundingBox(ForgeDirection side) {
		float min = LPConstants.PIPE_MIN_POS;
		float max = LPConstants.PIPE_MAX_POS;

		if (side == ForgeDirection.UNKNOWN) {
			return AxisAlignedBB.getBoundingBox(min, min, min, max, max, max);
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
		return AxisAlignedBB.getBoundingBox(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]);
	}
	
	private boolean isVecInsideYZBounds(Vec3 par1Vec3) {
		return par1Vec3 == null ? false : par1Vec3.yCoord >= this.minY && par1Vec3.yCoord <= this.maxY && par1Vec3.zCoord >= this.minZ && par1Vec3.zCoord <= this.maxZ;
	}
	
	private boolean isVecInsideXZBounds(Vec3 par1Vec3) {
		return par1Vec3 == null ? false : par1Vec3.xCoord >= this.minX && par1Vec3.xCoord <= this.maxX && par1Vec3.zCoord >= this.minZ && par1Vec3.zCoord <= this.maxZ;
	}
	
	private boolean isVecInsideXYBounds(Vec3 par1Vec3) {
		return par1Vec3 == null ? false : par1Vec3.xCoord >= this.minX && par1Vec3.xCoord <= this.maxX && par1Vec3.yCoord >= this.minY && par1Vec3.yCoord <= this.maxY;
	}

    public static IIcon getRequestTableTextureFromSide(int l) {
    	ForgeDirection dir = ForgeDirection.getOrientation(l);
		switch(dir) {
			case UP:
				return Textures.LOGISTICS_REQUEST_TABLE[0];
			case DOWN:
				return Textures.LOGISTICS_REQUEST_TABLE[1];
			default:
				return Textures.LOGISTICS_REQUEST_TABLE[4];
		}
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new LogisticsTileGenericPipe();
    }
    

	public static int facadeRenderColor = -1;
	public static Map<Item, Class<? extends CoreUnroutedPipe>> pipes = new HashMap<Item, Class<? extends CoreUnroutedPipe>>();
	public static Map<LPPosition, CoreUnroutedPipe> pipeRemoved = new HashMap<LPPosition, CoreUnroutedPipe>();

	private static long lastRemovedDate = -1;

	public static enum Part {
		Pipe,
		Gate,
		Facade,
		Plug,
		RobotStation
	}

	public static class RaytraceResult {

		public final Part hitPart;
		public final MovingObjectPosition movingObjectPosition;
		public final AxisAlignedBB boundingBox;
		public final ForgeDirection sideHit;

		RaytraceResult(Part hitPart, MovingObjectPosition movingObjectPosition, AxisAlignedBB boundingBox, ForgeDirection side) {
			this.hitPart = hitPart;
			this.movingObjectPosition = movingObjectPosition;
			this.boundingBox = boundingBox;
			this.sideHit = side;
		}

		@Override
		public String toString() {
			return String.format("RayTraceResult: %s, %s", hitPart == null ? "null" : hitPart.name(), boundingBox == null ? "null" : boundingBox.toString());
		}
	}
	private static final ForgeDirection[] DIR_VALUES = ForgeDirection.values();
	private boolean skippedFirstIconRegister;
	private int renderMask = 0;
	protected final Random rand = new Random();

	@Override
	public float getBlockHardness(World par1World, int par2, int par3, int par4) {
		return Configs.pipeDurability;
	}

	@Override
	public int getRenderType() {
		return LPConstants.pipeModel;
	}

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
	public boolean canBeReplacedByLeaves(IBlockAccess world, int x, int y, int z) {
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

	public void setRenderSide(ForgeDirection side, boolean render) {
		if (render) {
			renderMask |= 1 << side.ordinal();
		} else {
			renderMask &= ~(1 << side.ordinal());
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess blockAccess, int x, int y, int z, int side) {
		return (renderMask & (1 << side)) != 0;
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {
		TileEntity tile = world.getTileEntity(x, y, z);
		
		if (tile instanceof LogisticsTileGenericPipe) {
			if(((LogisticsTileGenericPipe)tile).pipe instanceof PipeBlockRequestTable) return true;
			return ((LogisticsTileGenericPipe) tile).isSolidOnSide(side);
		}

		return false;
	}

	@Override
	public boolean isNormalCube() {
		return false;
	}

	public static void removePipe(CoreUnroutedPipe pipe) {
		if (!isValid(pipe)) {
			return;
		}
		
		if(pipe.canBeDestroyed() || pipe.destroyByPlayer()) {
			pipe.onBlockRemoval();
		} else if(pipe.preventRemove()) {
			cacheTileToPreventRemoval(pipe);
		}

		World world = pipe.container.getWorldObj();

		if (world == null) {
			return;
		}

		int x = pipe.container.xCoord;
		int y = pipe.container.yCoord;
		int z = pipe.container.zCoord;

		if (lastRemovedDate != world.getTotalWorldTime()) {
			lastRemovedDate = world.getTotalWorldTime();
			pipeRemoved.clear();
		}

		pipeRemoved.put(new LPPosition(x, y, z), pipe);
		world.removeTileEntity(x, y, z);
		updateNeighbourSignalState(pipe);
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int par6) {
		removePipe(getPipe(world, x, y, z));
		super.breakBlock(world, x, y, z, block, par6);
	}

	@Override
	public void dropBlockAsItemWithChance(World world, int i, int j, int k, int l, float f, int dmg) {

		if (world.isRemote) {
			return;
		}

		int i1 = quantityDropped(world.rand);
		for (int j1 = 0; j1 < i1; j1++) {
			if (world.rand.nextFloat() > f) {
				continue;
			}

			CoreUnroutedPipe pipe = getPipe(world, i, j, k);

			if (pipe == null) {
				pipe = pipeRemoved.get(new LPPosition(i, j, k));
			}

			if (pipe.item != null && (pipe.canBeDestroyed() || pipe.destroyByPlayer())) {
				for(ItemStack stack:pipe.dropContents()) {
					dropBlockAsItem(world, i, j, k, stack);
				}
				dropBlockAsItem(world, i, j, k, new ItemStack(pipe.item, 1, damageDropped(l)));
			} else if(pipe.item != null) {
				cacheTileToPreventRemoval(pipe);
			}
		}
	}

	@Override
	public Item getItemDropped(int meta, Random rand, int dmg) {
		// Returns null to be safe - the id does not depend on the meta
		return null;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
		RaytraceResult rayTraceResult = doRayTrace(world, x, y, z, Minecraft.getMinecraft().thePlayer);

		if (rayTraceResult != null && rayTraceResult.boundingBox != null) {
			switch (rayTraceResult.hitPart) {
			case Gate:
				CoreUnroutedPipe pipe = getPipe(world, x, y, z);
				return pipe.bcPipePart.getGateItem(rayTraceResult.sideHit.ordinal());
			case Plug:
				return SimpleServiceLocator.buildCraftProxy.getPipePlugItemStack();
			case RobotStation:
				return SimpleServiceLocator.buildCraftProxy.getRobotStationItemStack();
			case Pipe:
				return new ItemStack(getPipe(world, x, y, z).item);
			case Facade:
				ForgeDirection dir = ForgeDirection.getOrientation(target.sideHit);
				return SimpleServiceLocator.buildCraftProxy.getDropFacade(getPipe(world, x, y, z), dir);
			}
		}
		return null;
	}

	/* Wrappers ************************************************************ */
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		super.onNeighborBlockChange(world, x, y, z, block);

		CoreUnroutedPipe pipe = getPipe(world, x, y, z);

		if (isValid(pipe)) {
			pipe.container.scheduleNeighborChange();
			pipe.container.redstoneInput = 0;
			
			for (int i = 0; i < ForgeDirection.VALID_DIRECTIONS.length; i++) {
				ForgeDirection d = ForgeDirection.getOrientation(i);
				pipe.container.redstoneInputSide[i] = getRedstoneInputToPipe(world, x, y, z, d);
				if (pipe.container.redstoneInput < pipe.container.redstoneInputSide[i]) {
					pipe.container.redstoneInput = pipe.container.redstoneInputSide[i];
				}
			}
			
			pipe.bcPipePart.refreshRedStoneInput(pipe.container.redstoneInput);
		}
	}

	private int getRedstoneInputToPipe(World world, int x, int y, int z,
			ForgeDirection d) {
		int i = d.ordinal();
		int input = world.isBlockProvidingPowerTo(x + d.offsetX, y + d.offsetY, z + d.offsetZ, i);
		if (input == 0) {
			input = world.getIndirectPowerLevelTo(x + d.offsetX, y + d.offsetY, z + d.offsetZ, i);
			if (input == 0 && d != ForgeDirection.DOWN) {
				Block block = world.getBlock(x + d.offsetX, y + d.offsetY, z + d.offsetZ);
				if (block instanceof BlockRedstoneWire) {
					return world.getBlockMetadata(x + d.offsetX, y + d.offsetY, z + d.offsetZ);
				}
			}
		}
		return input;
	}


	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float par6, float par7, float par8, int meta) {
		super.onBlockPlaced(world, x, y, z, side, par6, par7, par8, meta);
		CoreUnroutedPipe pipe = getPipe(world, x, y, z);

		if (isValid(pipe)) {
			pipe.onBlockPlaced();
		}

		return meta;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
		super.onBlockPlacedBy(world, x, y, z, placer, stack);
		CoreUnroutedPipe pipe = getPipe(world, x, y, z);

		if (isValid(pipe)) {
			pipe.onBlockPlacedBy(placer);
		}
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float xOffset, float yOffset, float zOffset) {
		super.onBlockActivated(world, x, y, z, player, side, xOffset, yOffset, zOffset);

		world.notifyBlocksOfNeighborChange(x, y, z, LogisticsPipes.LogisticsPipeBlock);

		CoreUnroutedPipe pipe = getPipe(world, x, y, z);

		if (isValid(pipe)) {
			ItemStack currentItem = player.getCurrentEquippedItem();

			// Right click while sneaking with empty hand to strip equipment
			// from the pipe.
			if (player.isSneaking() && currentItem == null) {
				if (SimpleServiceLocator.buildCraftProxy.stripEquipment(world, x, y, z, player, pipe, this)) {
					return true;
				}
			} else if (currentItem == null) {
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
			} else if(SimpleServiceLocator.buildCraftProxy.handleBCClickOnPipe(currentItem, pipe, world, x, y, z, player, side, this)) {
				return true;
			}
			if (pipe.hasGate()) {
				RaytraceResult rayTraceResult = doRayTrace(world, x, y, z, player);

				if (rayTraceResult != null && rayTraceResult.hitPart == Part.Gate) {
					pipe.bcPipePart.openGateGui(player, rayTraceResult.sideHit.ordinal());
					return true;
				}
			}
			return pipe.blockActivated(player);
		}

		return false;
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int i, int j, int k, Entity entity) {
		super.onEntityCollidedWithBlock(world, i, j, k, entity);

		CoreUnroutedPipe pipe = getPipe(world, i, j, k);

		if (isValid(pipe)) {
			pipe.onEntityCollidedWithBlock(entity);
		}
	}

	@Override
	public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side) {
		CoreUnroutedPipe pipe = getPipe(world, x, y, z);

		if (isValid(pipe)) {
			return pipe.canConnectRedstone();
		} else {
			return false;
		}
	}

	@Override
	public int isProvidingStrongPower(IBlockAccess iblockaccess, int x, int y, int z, int l) {
		CoreUnroutedPipe pipe = getPipe(iblockaccess, x, y, z);

		if (isValid(pipe)) {
			return pipe.isPoweringTo(l);
		} else {
			return 0;
		}
	}

	@Override
	public boolean canProvidePower() {
		return true;
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess world, int i, int j, int k, int l) {
		CoreUnroutedPipe pipe = getPipe(world, i, j, k);

		if (isValid(pipe)) {
			return pipe.isIndirectlyPoweringTo(l);
		} else {
			return 0;
		}
	}

	@SuppressWarnings({"all"})
	@Override
	public void randomDisplayTick(World world, int i, int j, int k, Random random) {
		CoreUnroutedPipe pipe = getPipe(world, i, j, k);

		if (isValid(pipe)) {
			pipe.randomDisplayTick(random);
		}
	}

	/* Registration ******************************************************** */
	public static ItemLogisticsPipe registerPipe(Class<? extends CoreUnroutedPipe> clas) {
		ItemLogisticsPipe item = new ItemLogisticsPipe();
		item.setUnlocalizedName(clas.getSimpleName());
		GameRegistry.registerItem(item, item.getUnlocalizedName());

		pipes.put(item, clas);

		CoreUnroutedPipe dummyPipe = createPipe(item);
		if (dummyPipe != null) {
			item.setPipeIconIndex(dummyPipe.getIconIndexForItem(), dummyPipe.getTextureIndex());
			MainProxy.proxy.setIconProviderFromPipe(item, dummyPipe);
		}

		return item;
	}

	public static boolean isPipeRegistered(int key) {
		return pipes.containsKey(key);
	}

	public static CoreUnroutedPipe createPipe(Item key) {

		try {
			Class<? extends CoreUnroutedPipe> pipe = pipes.get(key);
			if (pipe != null) {
				return pipe.getConstructor(Item.class).newInstance(key);
			} else {
				LogisticsPipes.log.warn("Detected pipe with unknown key (" + key + "). Did you remove a buildcraft addon?");
			}

		} catch (Throwable t) {
			LogisticsPipes.log.warn("Failed to create pipe with (" + key + "). No valid constructor found. Possibly a item ID conflit.");
		}

		return null;
	}

	public static boolean placePipe(CoreUnroutedPipe pipe, World world, int i, int j, int k, Block block, int meta) {
		if (world.isRemote) {
			return true;
		}

		boolean placed = world.setBlock(i, j, k, block, meta, 2);

		if (placed) {
			TileEntity tile = world.getTileEntity(i, j, k);
			if (tile instanceof LogisticsTileGenericPipe) {
				LogisticsTileGenericPipe tilePipe = (LogisticsTileGenericPipe) tile;
				tilePipe.initialize(pipe);
				tilePipe.sendUpdateToClient();
			}
			world.notifyBlockChange(i, j, k, block);
		}

		return placed;
	}

	public static CoreUnroutedPipe getPipe(IBlockAccess blockAccess, int i, int j, int k) {
		TileEntity tile = blockAccess.getTileEntity(i, j, k);

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
		return isFullyDefined(pipe);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		LogisticsNewRenderPipe.registerTextures(iconRegister);
		if (!skippedFirstIconRegister) {
			skippedFirstIconRegister = true;
			return;
		}
		for (Item i : pipes.keySet()) {
			CoreUnroutedPipe dummyPipe = createPipe(i);
			if (dummyPipe != null) {
				dummyPipe.getIconProvider().registerIcons(iconRegister);
			}
		}
	}

	/**
	 * Spawn a digging particle effect in the world, this is a wrapper around
	 * EffectRenderer.addBlockHitEffects to allow the block more control over
	 * the particles. Useful when you have entirely different texture sheets for
	 * different sides/locations in the world.
	 *
	 * @param worldObj The current world
	 * @param target The target the player is looking at {x/y/z/side/sub}
	 * @param effectRenderer A reference to the current effect renderer.
	 * @return True to prevent vanilla digging particles form spawning.
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public boolean addHitEffects(World worldObj, MovingObjectPosition target, EffectRenderer effectRenderer) {
		int x = target.blockX;
		int y = target.blockY;
		int z = target.blockZ;

		CoreUnroutedPipe pipe = getPipe(worldObj, x, y, z);
		if (pipe == null) {
			return false;
		}

		IIcon icon = pipe.getIconProvider().getIcon(pipe.getIconIndexForItem());

		int sideHit = target.sideHit;

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

		EntityDiggingFX fx = new EntityDiggingFX(worldObj, px, py, pz, 0.0D, 0.0D, 0.0D, block, sideHit, worldObj.getBlockMetadata(x, y, z));
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
	 * @param worldObj The current world
	 * @param x X position to spawn the particle
	 * @param y Y position to spawn the particle
	 * @param z Z position to spawn the particle
	 * @param meta The metadata for the block before it was destroyed.
	 * @param effectRenderer A reference to the current effect renderer.
	 * @return True to prevent vanilla break particles from spawning.
	 */
	@SideOnly(Side.CLIENT)
	@Override
	public boolean addDestroyEffects(World worldObj, int x, int y, int z, int meta, EffectRenderer effectRenderer) {
		CoreUnroutedPipe pipe = getPipe(worldObj, x, y, z);
		if (pipe == null) {
			return false;
		}

		IIcon icon = pipe.getIconProvider().getIcon(pipe.getIconIndexForItem());

		byte its = 4;
		for (int i = 0; i < its; ++i) {
			for (int j = 0; j < its; ++j) {
				for (int k = 0; k < its; ++k) {
					double px = x + (i + 0.5D) / its;
					double py = y + (j + 0.5D) / its;
					double pz = z + (k + 0.5D) / its;
					int random = rand.nextInt(6);
					EntityDiggingFX fx = new EntityDiggingFX(worldObj, px, py, pz, px - x - 0.5D, py - y - 0.5D, pz - z - 0.5D, LogisticsPipes.LogisticsPipeBlock, random, meta);
					fx.setParticleIcon(icon);
					effectRenderer.addEffect(fx.applyColourMultiplier(x, y, z));
				}
			}
		}
		return true;
	}

	@Override
	public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
		if (facadeRenderColor != -1) {
			return facadeRenderColor;
		}
		return super.colorMultiplier(world, x, y, z);
	}
	
	public static void updateNeighbourSignalState(CoreUnroutedPipe pipe) {
		TileBuffer[] neighbours = pipe.container.getTileCache();

		if (neighbours != null) {
			for (int i = 0; i < 6; i++) {
				if (neighbours[i] != null && neighbours[i].getTile() != null && !neighbours[i].getTile().isInvalid()) {
					SimpleServiceLocator.buildCraftProxy.checkUpdateNeighbour(neighbours[i].getTile());
					if(neighbours[i].getTile() instanceof LogisticsTileGenericPipe) {
						((LogisticsTileGenericPipe) neighbours[i].getTile()).pipe.updateSignalState();
					}
				}
			}
		}
	}
	
	private static void cacheTileToPreventRemoval(CoreUnroutedPipe pipe) {
		final World worldCache = pipe.getWorld();
		final int xCache = pipe.getX();
		final int yCache = pipe.getY();
		final int zCache = pipe.getZ();
		final TileEntity tileCache = pipe.container;
		final CoreUnroutedPipe fPipe = pipe;
		fPipe.setPreventRemove(true);
		QueuedTasks.queueTask(new Callable<Object>() {
			@Override
			public Object call() throws Exception {
				if(!fPipe.preventRemove()) return null;
				boolean changed = false;
				if(worldCache.getBlock(xCache, yCache, zCache) != LogisticsPipes.LogisticsPipeBlock) {
					worldCache.setBlock(xCache, yCache, zCache, LogisticsPipes.LogisticsPipeBlock);
					changed = true;
				}
				if(worldCache.getTileEntity(xCache, yCache, zCache) != tileCache) {
					worldCache.setTileEntity(xCache, yCache, zCache, tileCache);
					changed = true;
				}
				if(changed) {
					worldCache.notifyBlockChange(xCache, yCache, zCache, LogisticsPipes.LogisticsPipeBlock);
				}
				fPipe.setPreventRemove(false);
				return null;
			}
		});
	}
}
