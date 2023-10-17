package logisticspipes.pipes.tubes;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import lombok.AllArgsConstructor;
import lombok.Getter;

import logisticspipes.LPConstants;
import logisticspipes.interfaces.ITubeOrientation;
import logisticspipes.interfaces.ITubeRenderOrientation;
import logisticspipes.pipes.basic.CoreMultiBlockPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericSubMultiBlock;
import logisticspipes.proxy.SimpleServiceLocator;
import logisticspipes.renderer.newpipe.IHighlightPlacementRenderer;
import logisticspipes.renderer.newpipe.ISpecialPipeRenderer;
import logisticspipes.renderer.newpipe.tube.SpeedupTubeRenderer;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemClient;
import logisticspipes.transport.LPTravelingItem.LPTravelingItemServer;
import logisticspipes.transport.PipeMultiBlockTransportLogistics;
import logisticspipes.utils.IPositionRotateble;
import logisticspipes.utils.LPPositionSet;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;
import network.rs485.logisticspipes.world.CoordinateUtils;
import network.rs485.logisticspipes.world.DoubleCoordinates;
import network.rs485.logisticspipes.world.DoubleCoordinatesType;

public class HSTubeSpeedup extends CoreMultiBlockPipe {

	@Getter
	private SpeedupDirection orientation;

	public HSTubeSpeedup(Item item) {
		super(new PipeMultiBlockTransportLogistics() {

			@Override
			public boolean canPipeConnect(TileEntity tile, EnumFacing side) {
				if (side.getOpposite() == ((HSTubeSpeedup) getMultiPipe()).orientation.dir1) {
					return super.canPipeConnect_internal(tile, side);
				}
				return false;
			}

			@Override
			protected void handleTileReachedServer(LPTravelingItemServer arrivingItem, TileEntity tile, EnumFacing dir) {
				if (dir.getOpposite() == ((HSTubeSpeedup) getMultiPipe()).orientation.dir1) {
					arrivingItem.setSpeed(LPConstants.PIPE_NORMAL_SPEED * 20);
					handleTileReachedServer_internal(arrivingItem, tile, dir);
				} else {
					super.handleTileReachedServer(arrivingItem, tile, dir);
				}
			}

			@Override
			protected void handleTileReachedClient(LPTravelingItemClient arrivingItem, TileEntity tile, EnumFacing dir) {
				if (dir.getOpposite() == ((HSTubeSpeedup) getMultiPipe()).orientation.dir1) {
					if (SimpleServiceLocator.pipeInformationManager.isItemPipe(tile)) {
						arrivingItem.setSpeed(LPConstants.PIPE_NORMAL_SPEED * 20);
						passToNextPipe(arrivingItem, tile);
					}
				} else {
					super.handleTileReachedClient(arrivingItem, tile, dir);
				}
			}

		}, item);
	}

	@Override
	public void writeData(LPDataOutput output) {
		output.writeEnum(orientation);
	}

	@Override
	public void readData(LPDataInput input) {
		orientation = input.readEnum(SpeedupDirection.class);
	}

	@Override
	public LPPositionSet<DoubleCoordinatesType<SubBlockTypeForShare>> getSubBlocks() {
		LPPositionSet<DoubleCoordinatesType<SubBlockTypeForShare>> set = new LPPositionSet<>(DoubleCoordinatesType.class);
		set.add(new DoubleCoordinatesType<>(0, 0, -1, SubBlockTypeForShare.NON_SHARE));
		set.add(new DoubleCoordinatesType<>(0, 0, -2, SubBlockTypeForShare.NON_SHARE));
		set.add(new DoubleCoordinatesType<>(0, 0, -3, SubBlockTypeForShare.NON_SHARE));
		return set;
	}

	@Override
	public LPPositionSet<DoubleCoordinatesType<SubBlockTypeForShare>> getRotatedSubBlocks() {
		LPPositionSet<DoubleCoordinatesType<SubBlockTypeForShare>> set = getSubBlocks();
		orientation.rotatePositions(set);
		return set;
	}

	@Override
	public void addCollisionBoxesToList(List<AxisAlignedBB> arraylist, AxisAlignedBB axisalignedbb) {
		DoubleCoordinates pos = getLPPosition();
		DoubleCoordinates posMin = new DoubleCoordinates(LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MIN_POS);
		DoubleCoordinates posMax = new DoubleCoordinates(LPConstants.PIPE_MAX_POS, LPConstants.PIPE_MAX_POS, -3);
		orientation.rotatePositions(posMin);
		orientation.rotatePositions(posMax);
		if (orientation == SpeedupDirection.EAST) {
			pos.add(new DoubleCoordinates(1, 0, 0));
		} else if (orientation == SpeedupDirection.SOUTH) {
			pos.add(new DoubleCoordinates(1, 0, 1));
		} else if (orientation == SpeedupDirection.WEST) {
			pos.add(new DoubleCoordinates(0, 0, 1));
		}
		posMin.add(pos);
		posMax.add(pos);
		LPPositionSet<DoubleCoordinates> set = new LPPositionSet<>(DoubleCoordinates.class);
		set.add(posMin);
		set.add(posMax);
		AxisAlignedBB box = set.toABB();
		if (box != null && (axisalignedbb == null || axisalignedbb.intersects(box))) {
			arraylist.add(box);
		}
	}

	@Override
	public AxisAlignedBB getCompleteBox() {
		return SpeedupTubeRenderer.tubeSpeedup.get(orientation).bounds().toAABB();
	}

	@Override
	public ITubeOrientation getTubeOrientation(EntityPlayer player, int xPos, int zPos) {
		double x = xPos + 0.5 - player.posX;
		double z = zPos + 0.5 - player.posZ;
		double w = Math.atan2(x, z);
		double halfPI = Math.PI / 2;
		double halfhalfPI = halfPI / 2;
		w -= halfhalfPI;
		if (w < 0) {
			w += 2 * Math.PI;
		}
		EnumFacing dir = null;
		if (0 < w && w <= halfPI) {
			dir = EnumFacing.WEST;
		} else if (halfPI < w && w <= 2 * halfPI) {
			dir = EnumFacing.SOUTH;
		} else if (2 * halfPI < w && w <= 3 * halfPI) {
			dir = EnumFacing.EAST;
		} else if (3 * halfPI < w && w <= 4 * halfPI) {
			dir = EnumFacing.NORTH;
		}
		for (SpeedupDirection ori : SpeedupDirection.values()) {
			if (ori.dir1.getOpposite().equals(dir)) {
				return ori;
			}
		}
		return null;
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		data.setString("orientation", orientation.name());
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		orientation = SpeedupDirection.valueOf(data.getString("orientation"));
	}

	@Override
	public float getPipeLength() {
		return 4;
	}

	@Override
	public EnumFacing getExitForInput(EnumFacing commingFrom) {
		return commingFrom.getOpposite();
	}

	@Override
	public TileEntity getConnectedEndTile(EnumFacing output) {
		if (orientation.dir1 == output) {
			DoubleCoordinates pos = new DoubleCoordinates(0, 0, -3);
			LPPositionSet<DoubleCoordinates> set = new LPPositionSet<>(DoubleCoordinates.class);
			set.add(pos);
			orientation.rotatePositions(set);
			TileEntity subTile = pos.add(getLPPosition()).getTileEntity(getWorld());
			if (subTile instanceof LogisticsTileGenericSubMultiBlock) {
				return ((LogisticsTileGenericSubMultiBlock) subTile).getTile(output);
			}
		} else if (orientation.dir1.getOpposite() == output) {
			return container.getTile(output);
		}
		return null;
	}

	@Override
	public boolean actAsNormalPipe() {
		return true;
	}

	@Override
	public ISpecialPipeRenderer getSpecialRenderer() {
		return SpeedupTubeRenderer.instance;
	}

	@Override
	public IHighlightPlacementRenderer getHighlightRenderer() {
		return SpeedupTubeRenderer.instance;
	}

	@Override
	public int getIconIndex(EnumFacing direction) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getTextureIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean hasSpecialPipeEndAt(EnumFacing dir) {
		return dir == orientation.dir1;
	}

	@Override
	public DoubleCoordinates getItemRenderPos(float fPos, LPTravelingItem travelItem) {
		DoubleCoordinates pos = new DoubleCoordinates(0.5D, 0.5D, 0.5D);
		float pPos = fPos;
		if (travelItem.input.getOpposite() == orientation.dir1) {
			CoordinateUtils.add(pos, orientation.dir1, 3);
			pPos = this.getPipeLength() - fPos;
		}
		if (pPos < 0.5) {
			if (travelItem.input == null) {
				return null;
			}
			if (!container.renderState.pipeConnectionMatrix.isConnected(travelItem.input.getOpposite())) {
				return null;
			}
			CoordinateUtils.add(pos, travelItem.input.getOpposite(), 0.5 - fPos);
		} else {
			if (travelItem.output == null) {
				return null;
			}
			CoordinateUtils.add(pos, travelItem.output, fPos - 0.5);
		}
		return pos;
	}

	@Override
	public boolean canPipeConnect(TileEntity tile, EnumFacing side) {
		if (tile instanceof LogisticsTileGenericSubMultiBlock) {
			if (this.getOrientation().getDir1() != side) {
				return false;
			}
		}
		return super.canPipeConnect(tile, side);
	}

	@Override
	public boolean isHSTube() {
		return true;
	}

	@AllArgsConstructor
	public enum SpeedupDirection implements ITubeRenderOrientation, ITubeOrientation {
		//@formatter:off
		NORTH(EnumFacing.NORTH),
		SOUTH(EnumFacing.SOUTH),
		EAST(EnumFacing.EAST),
		WEST(EnumFacing.WEST);
		//@formatter:on
		@Getter
		EnumFacing dir1;

		@Override
		public void rotatePositions(IPositionRotateble set) {
			if (this == SOUTH) {
				set.rotateLeft();
				set.rotateLeft();
			} else if (this == EAST) {
				set.rotateRight();
			} else if (this == WEST) {
				set.rotateLeft();
			}
		}

		@Override
		public ITubeRenderOrientation getRenderOrientation() {
			return this;
		}

		@Override
		public DoubleCoordinates getOffset() {
			return new DoubleCoordinates(0, 0, 0);
		}

		@Override
		public void setOnPipe(CoreMultiBlockPipe pipe) {
			((HSTubeSpeedup) pipe).orientation = this;
		}
	}
}
