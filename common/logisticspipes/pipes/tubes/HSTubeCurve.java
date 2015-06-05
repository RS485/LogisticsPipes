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
import logisticspipes.renderer.newpipe.IHighlightPlacementRenderer;
import logisticspipes.renderer.newpipe.ISpecialPipeRenderer;
import logisticspipes.renderer.newpipe.tube.CurveTubeRenderer;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.transport.PipeMultiBlockTransportLogistics;
import logisticspipes.utils.IPositionRotateble;
import logisticspipes.utils.LPPositionSet;
import logisticspipes.utils.tuples.LPPosition;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

import net.minecraftforge.common.util.ForgeDirection;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class HSTubeCurve extends CoreMultiBlockPipe {

	@AllArgsConstructor
	public enum CurveOrientation implements ITubeOrientation {
		//@formatter:off
		// Name: Placement from  _ TurnDirection
		NORTH_NORTH_EAST(TurnDirection.NORTH_EAST, new LPPosition(2, 0, 2), ForgeDirection.NORTH, ForgeDirection.EAST),
		NORTH_WEST_NORTH(TurnDirection.WEST_NORTH, new LPPosition(0, 0, 0), ForgeDirection.NORTH, ForgeDirection.WEST),
		WEST_WEST_NORTH(TurnDirection.WEST_NORTH, new LPPosition(2, 0, -2), ForgeDirection.WEST, ForgeDirection.NORTH),
		WEST_SOUTH_WEST(TurnDirection.SOUTH_WEST, new LPPosition(0, 0, 0), ForgeDirection.WEST, ForgeDirection.SOUTH),
		SOUTH_SOUTH_WEST(TurnDirection.SOUTH_WEST, new LPPosition(-2, 0, -2), ForgeDirection.SOUTH, ForgeDirection.WEST),
		SOUTH_EAST_SOUTH(TurnDirection.EAST_SOUTH, new LPPosition(0, 0, 0), ForgeDirection.SOUTH, ForgeDirection.EAST),
		EAST_EAST_SOUTH(TurnDirection.EAST_SOUTH, new LPPosition(-2, 0, 2), ForgeDirection.EAST, ForgeDirection.SOUTH),
		EAST_NORTH_EAST(TurnDirection.NORTH_EAST, new LPPosition(0, 0, 0), ForgeDirection.EAST, ForgeDirection.NORTH);
		//@formatter:on
		@Getter
		TurnDirection renderOrientation;
		@Getter
		LPPosition offset;
		@Getter
		ForgeDirection from;
		@Getter
		ForgeDirection looking;

		@Override
		public void rotatePositions(IPositionRotateble set) {
			renderOrientation.rotatePositions(set);
		}

		@Override
		public void setOnPipe(CoreMultiBlockPipe pipe) {
			((HSTubeCurve) pipe).orientation = this;
		}
	}

	@AllArgsConstructor
	public enum TurnDirection implements ITubeRenderOrientation {
		//@formatter:off
		NORTH_EAST(ForgeDirection.NORTH, ForgeDirection.EAST),
		EAST_SOUTH(ForgeDirection.EAST, ForgeDirection.SOUTH),
		SOUTH_WEST(ForgeDirection.SOUTH, ForgeDirection.WEST),
		WEST_NORTH(ForgeDirection.WEST, ForgeDirection.NORTH);
		//@formatter:on
		ForgeDirection dir1;
		ForgeDirection dir2;

		public void rotatePositions(IPositionRotateble set) {
			if (this == WEST_NORTH) {
				return;
			} else if (this == NORTH_EAST) {
				set.rotateRight();
			} else if (this == EAST_SOUTH) {
				set.rotateLeft();
				set.rotateLeft();
			} else if (this == SOUTH_WEST) {
				set.rotateLeft();
			}
		}
	}

	@Getter
	private CurveOrientation orientation;

	public HSTubeCurve(Item item) {
		super(new PipeMultiBlockTransportLogistics(), item);
	}

	@Override
	public void writeData(LPDataOutputStream data) throws IOException {
		if (orientation == null) {
			data.writeBoolean(false);
		} else {
			data.writeBoolean(true);
			data.writeEnum(orientation);
		}
	}

	@Override
	public void readData(LPDataInputStream data) throws IOException {
		if (data.readBoolean()) {
			orientation = data.readEnum(CurveOrientation.class);
		}
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
	public LPPositionSet getSubBlocks() {
		LPPositionSet list = new LPPositionSet();
		list.add(new LPPosition(-1, 0, 0));
		list.add(new LPPosition(0, 0, 1));
		list.add(new LPPosition(-1, 0, 1));
		list.add(new LPPosition(-2, 0, 1));
		list.add(new LPPosition(-1, 0, 2));
		list.add(new LPPosition(-2, 0, 2));
		return list;
	}

	@Override
	public LPPositionSet getRotatedSubBlocks() {
		LPPositionSet set = getSubBlocks();
		orientation.rotatePositions(set);
		return set;
	}

	@Override
	@SideOnly(Side.CLIENT)
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addCollisionBoxesToList(List arraylist, AxisAlignedBB axisalignedbb) {
		double x = getX();
		double y = getY();
		double z = getZ();
		double angle = 0;
		double addOne = 0;
		double addTwo = 0;
		if (orientation.getRenderOrientation() == TurnDirection.NORTH_EAST) {
			angle = 3 * Math.PI / 2;
			addOne = LPConstants.PIPE_MAX_POS;
			addTwo = LPConstants.PIPE_MIN_POS;
			z -= 2;
			x += 1;
		} else if (orientation.getRenderOrientation() == TurnDirection.EAST_SOUTH) {
			angle = 2 * Math.PI / 2;
			addOne = LPConstants.PIPE_MIN_POS;
			addTwo = LPConstants.PIPE_MAX_POS;
			x += 3;
			z += 1;
		} else if (orientation.getRenderOrientation() == TurnDirection.SOUTH_WEST) {
			angle = Math.PI / 2;
			addOne = LPConstants.PIPE_MAX_POS;
			addTwo = LPConstants.PIPE_MIN_POS;
			z += 3;
		} else if (orientation.getRenderOrientation() == TurnDirection.WEST_NORTH) {
			angle = 0;
			addOne = LPConstants.PIPE_MIN_POS;
			addTwo = LPConstants.PIPE_MAX_POS;
			x -= 2;
		}
		for (int i = 0; i < 49; i++) {
			double xOne = x;
			double yMin = y + LPConstants.PIPE_MIN_POS;
			double zOne = z;
			double xTwo = x;
			double yMax = y + LPConstants.PIPE_MAX_POS;
			double zTwo = z;
			xOne += (2 + addOne) * Math.sin(angle + (2 * Math.PI / 200 * (i)));
			zOne += (2 + addOne) * Math.cos(angle + (2 * Math.PI / 200 * (i + 2)));
			xTwo += (2 + addTwo) * Math.sin(angle + (2 * Math.PI / 200 * (i + 1)));
			zTwo += (2 + addTwo) * Math.cos(angle + (2 * Math.PI / 200 * (i)));
			AxisAlignedBB box = AxisAlignedBB.getBoundingBox(Math.min(xOne, xTwo), yMin, Math.min(zOne, zTwo), Math.max(xOne, xTwo), yMax, Math.max(zOne, zTwo));
			if (box != null && (axisalignedbb == null || axisalignedbb.intersectsWith(box))) {
				arraylist.add(box);
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getCompleteBox() {
		return CurveTubeRenderer.tubeCurve.get(orientation.getRenderOrientation()).bounds().toAABB();
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
		ForgeDirection dir1 = ForgeDirection.UNKNOWN;
		ForgeDirection dir2 = ForgeDirection.UNKNOWN;
		double addition = 0;
		if (0 < w && w <= halfPI) {
			dir = ForgeDirection.WEST;
			dir1 = ForgeDirection.NORTH;
			dir2 = ForgeDirection.SOUTH;
			addition = halfPI;
		} else if (halfPI < w && w <= 2 * halfPI) {
			dir = ForgeDirection.SOUTH;
			dir1 = ForgeDirection.EAST;
			dir2 = ForgeDirection.WEST;
		} else if (2 * halfPI < w && w <= 3 * halfPI) {
			dir = ForgeDirection.EAST;
			dir1 = ForgeDirection.NORTH;
			dir2 = ForgeDirection.SOUTH;
			addition = halfPI;
		} else if (3 * halfPI < w && w <= 4 * halfPI) {
			dir = ForgeDirection.NORTH;
			dir1 = ForgeDirection.EAST;
			dir2 = ForgeDirection.WEST;
		}
		w = Math.atan2(player.getLookVec().xCoord, player.getLookVec().zCoord);
		w -= addition;
		if (w < 0) {
			w += 2 * Math.PI;
		}
		ForgeDirection dir3 = ForgeDirection.UNKNOWN;
		if (0 < w && w <= 2 * halfPI) {
			dir3 = dir1;
		} else if (2 * halfPI < w && w <= 4 * halfPI) {
			dir3 = dir2;
		}
		for (CurveOrientation curve : CurveOrientation.values()) {
			if (curve.from.equals(dir)) {
				if (curve.looking.equals(dir3)) {
					return curve;
				}
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
		orientation = CurveOrientation.valueOf(data.getString("orientation"));
	}

	@Override
	public float getPipeLength() {
		return (float) (Math.PI / 2 * 2.5);
	}

	@Override
	public ForgeDirection getExitForInput(ForgeDirection commingFrom) {
		TurnDirection ori = orientation.getRenderOrientation();
		if (ori.dir1 == commingFrom) {
			return ori.dir2;
		}
		if (ori.dir2 == commingFrom) {
			return ori.dir1;
		}
		return null;
	}

	@Override
	public TileEntity getConnectedEndTile(ForgeDirection output) {
		TurnDirection ori = orientation.getRenderOrientation();
		if (ori.dir2 == output) {
			return container.getTile(output);
		}
		if (ori.dir1 == output) {
			LPPosition pos = new LPPosition(-2, 0, 2);
			LPPositionSet set = new LPPositionSet();
			set.add(pos);
			orientation.rotatePositions(set);
			TileEntity subTile = pos.add(getLPPosition()).getTileEntity(getWorld());
			if (subTile instanceof LogisticsTileGenericSubMultiBlock) {
				return ((LogisticsTileGenericSubMultiBlock) subTile).getTile(output);
			}
		}
		return null;
	}

	@Override
	public boolean actAsNormalPipe() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ISpecialPipeRenderer getSpecialRenderer() {
		return CurveTubeRenderer.instance;
	}

	@Override
	public IHighlightPlacementRenderer getHighlightRenderer() {
		return CurveTubeRenderer.instance;
	}

	@Override
	public LPPosition getItemRenderPos(float fPos, LPTravelingItem travelItem) {
		if (orientation.getOffset().getX() == 0) {
			fPos = transport.getPipeLength() - fPos;
		}
		double angle = 0;
		int x = 0;
		int y = 0;
		int z = 0;
		if (orientation.getRenderOrientation() == TurnDirection.NORTH_EAST) {
			angle = 3 * Math.PI / 2;
			z -= 2;
			x += 1;
		} else if (orientation.getRenderOrientation() == TurnDirection.EAST_SOUTH) {
			angle = 2 * Math.PI / 2;
			x += 3;
			z += 1;
		} else if (orientation.getRenderOrientation() == TurnDirection.SOUTH_WEST) {
			angle = Math.PI / 2;
			z += 3;
		} else if (orientation.getRenderOrientation() == TurnDirection.WEST_NORTH) {
			angle = 0;
			x -= 2;
		}
		double xOne = x;
		double yMin = y + 0.5;
		double zOne = z;
		xOne += (2.5) * Math.sin(angle + (2 * Math.PI / 4 / transport.getPipeLength() * fPos));
		zOne += (2.5) * Math.cos(angle + (2 * Math.PI / 4 / transport.getPipeLength() * fPos));
		return new LPPosition(xOne, yMin, zOne);
	}

	@Override
	public double getItemRenderPitch(float fPos, LPTravelingItem travelItem) {
		// TODO Auto-generated method stub
		return super.getItemRenderPitch(fPos, travelItem);
	}

	@Override
	public double getItemRenderYaw(float fPos, LPTravelingItem travelItem) {
		if (orientation.getOffset().getX() == 0) {
			fPos = transport.getPipeLength() - fPos;
		}
		double angle = 0;
		if (orientation.getRenderOrientation() == TurnDirection.NORTH_EAST) {
			angle = 3 * Math.PI / 2;
		} else if (orientation.getRenderOrientation() == TurnDirection.EAST_SOUTH) {
			angle = 2 * Math.PI / 2;
		} else if (orientation.getRenderOrientation() == TurnDirection.SOUTH_WEST) {
			angle = Math.PI / 2;
		} else if (orientation.getRenderOrientation() == TurnDirection.WEST_NORTH) {
			angle = 0;
		}
		return 360 * (angle + 2 * Math.PI / 4 / transport.getPipeLength() * fPos) / (2 * Math.PI);
	}

	@Override
	public double getBoxRenderScale(float fPos, LPTravelingItem travelItem) {
		/*
		if(orientation.getOffset().getX() == 0) {
			fPos = this.transport.getPipeLength() - fPos;
		}
		if(fPos > this.transport.getPipeLength() - 0.5) {
			return 1 - (this.transport.getPipeLength() - fPos) * 0.1D;
		} else if(fPos > 0.5) {
			return 0.95D;
		} else {
			return 1 - fPos * 0.1D;
		}
		 */
		return 1;
	}

	@Override
	public boolean isHSTube() {
		return true;
	}
}
