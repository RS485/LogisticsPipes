package logisticspipes.pipes.tubes;

import java.io.IOException;
import java.util.List;

import logisticspipes.LPConstants;
import logisticspipes.interfaces.ITubeOrientation;
import logisticspipes.interfaces.ITubeRenderOrientation;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
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

import network.rs485.logisticspipes.world.CoordinateUtils;
import network.rs485.logisticspipes.world.DoubleCoordinates;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

import net.minecraftforge.common.util.ForgeDirection;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class HSTubeSpeedup extends CoreMultiBlockPipe {

	@AllArgsConstructor
	public enum SpeedupDirection implements ITubeRenderOrientation, ITubeOrientation {
		//@formatter:off
		NORTH(ForgeDirection.NORTH),
		SOUTH(ForgeDirection.SOUTH),
		EAST(ForgeDirection.EAST),
		WEST(ForgeDirection.WEST);
		//@formatter:on
		@Getter
		ForgeDirection dir1;

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

	@Getter
	private SpeedupDirection orientation;

	public HSTubeSpeedup(Item item) {
		super(new PipeMultiBlockTransportLogistics() {

			@Override
			public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
				if (side.getOpposite() == ((HSTubeSpeedup) getMultiPipe()).orientation.dir1) {
					return super.canPipeConnect_internal(tile, side);
				}
				return false;
			}

			@Override
			protected void handleTileReachedServer(LPTravelingItemServer arrivingItem, TileEntity tile, ForgeDirection dir) {
				if (dir.getOpposite() == ((HSTubeSpeedup) getMultiPipe()).orientation.dir1) {
					arrivingItem.setSpeed(LPConstants.PIPE_NORMAL_SPEED * 20);
					handleTileReachedServer_internal(arrivingItem, tile, dir);
				} else {
					super.handleTileReachedServer(arrivingItem, tile, dir);
				}
			}

			@Override
			protected void handleTileReachedClient(LPTravelingItemClient arrivingItem, TileEntity tile, ForgeDirection dir) {
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
	public void writeData(LPDataOutputStream data) throws IOException {
		data.writeEnum(orientation);
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		orientation = data.readEnum(SpeedupDirection.class);
	}

	@Override
	public LPPositionSet getSubBlocks() {
		LPPositionSet set = new LPPositionSet();
		set.add(new DoubleCoordinates(0, 0, -1));
		set.add(new DoubleCoordinates(0, 0, -2));
		set.add(new DoubleCoordinates(0, 0, -3));
		return set;
	}

	@Override
	public LPPositionSet getRotatedSubBlocks() {
		LPPositionSet set = getSubBlocks();
		orientation.rotatePositions(set);
		return set;
	}

	@Override
	public void addCollisionBoxesToList(List arraylist, AxisAlignedBB axisalignedbb) {
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
		LPPositionSet set = new LPPositionSet();
		set.add(posMin);
		set.add(posMax);
		AxisAlignedBB box = set.toABB();
		if (box != null && (axisalignedbb == null || axisalignedbb.intersectsWith(box))) {
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
		ForgeDirection dir = ForgeDirection.UNKNOWN;
		if (0 < w && w <= halfPI) {
			dir = ForgeDirection.WEST;
		} else if (halfPI < w && w <= 2 * halfPI) {
			dir = ForgeDirection.SOUTH;
		} else if (2 * halfPI < w && w <= 3 * halfPI) {
			dir = ForgeDirection.EAST;
		} else if (3 * halfPI < w && w <= 4 * halfPI) {
			dir = ForgeDirection.NORTH;
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
	public ForgeDirection getExitForInput(ForgeDirection commingFrom) {
		return commingFrom.getOpposite();
	}

	@Override
	public TileEntity getConnectedEndTile(ForgeDirection output) {
		if (orientation.dir1 == output) {
			DoubleCoordinates pos = new DoubleCoordinates(0, 0, -3);
			LPPositionSet set = new LPPositionSet();
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
	public int getIconIndex(ForgeDirection direction) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getTextureIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean hasSpecialPipeEndAt(ForgeDirection dir) {
		return dir == orientation.dir1;
	}

	@Override
	public DoubleCoordinates getItemRenderPos(float fPos, LPTravelingItem travelItem) {
		DoubleCoordinates pos = new DoubleCoordinates(0.5D, 0.5D, 0.5D);
		if (travelItem.input.getOpposite() == orientation.dir1) {
			CoordinateUtils.add(pos, orientation.dir1, 3.5);
		}
		if (fPos < 0.5) {
			if (travelItem.input == ForgeDirection.UNKNOWN) {
				return null;
			}
			if (!container.renderState.pipeConnectionMatrix.isConnected(travelItem.input.getOpposite())) {
				return null;
			}
			CoordinateUtils.add(pos, travelItem.input.getOpposite(), 0.5 - fPos);
		} else {
			if (travelItem.output == ForgeDirection.UNKNOWN) {
				return null;
			}
			CoordinateUtils.add(pos, travelItem.output, fPos - 0.5);
		}
		return pos;
	}

	@Override
	public boolean isHSTube() {
		return true;
	}
}
