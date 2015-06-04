package logisticspipes.pipes.tubes;

import java.io.IOException;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import logisticspipes.LPConstants;
import logisticspipes.interfaces.ITubeOrientation;
import logisticspipes.interfaces.ITubeRenderOrientation;
import logisticspipes.network.LPDataInputStream;
import logisticspipes.network.LPDataOutputStream;
import logisticspipes.pipes.basic.CoreMultiBlockPipe;
import logisticspipes.pipes.tubes.HSTubeCurve.CurveOrientation;
import logisticspipes.pipes.tubes.HSTubeCurve.TurnDirection;
import logisticspipes.renderer.newpipe.IHighlightPlacementRenderer;
import logisticspipes.renderer.newpipe.ISpecialPipeRenderer;
import logisticspipes.renderer.newpipe.tube.SpeedupTubeRenderer;
import logisticspipes.transport.PipeMultiBlockTransportLogistics;
import logisticspipes.utils.IPositionRotateble;
import logisticspipes.utils.LPPositionSet;
import logisticspipes.utils.tuples.LPPosition;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class HSTubeSpeedup extends CoreMultiBlockPipe {

	@AllArgsConstructor
	public enum SpeedupDirection implements ITubeRenderOrientation, ITubeOrientation {
		NORTH(ForgeDirection.NORTH),
		SOUTH(ForgeDirection.SOUTH),
		EAST(ForgeDirection.EAST),
		WEST(ForgeDirection.WEST);
		ForgeDirection	dir1;
		
		public void rotatePositions(IPositionRotateble set) {
			if(this == SOUTH) {
				set.rotateLeft();
				set.rotateLeft();
			} else if(this == EAST) {
				set.rotateRight();
			} else if(this == WEST) {
				set.rotateLeft();
			}
		}
		
		@Override
		public ITubeRenderOrientation getRenderOrientation() {
			return this;
		}
		
		@Override
		public LPPosition getOffset() {
			return new LPPosition(0, 0, 0);
		}
		
		@Override
		public void setOnPipe(CoreMultiBlockPipe pipe) {
			((HSTubeSpeedup)pipe).orientation = this;
		}
	}

	@Getter
	private SpeedupDirection	orientation;

	public HSTubeSpeedup(Item item) {
		super(new PipeMultiBlockTransportLogistics() {

			@Override
			public boolean canPipeConnect(TileEntity tile, ForgeDirection side) {
				return super.canPipeConnect_internal(tile, side);
			}
			// TODO Auto-generated constructor stub
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
		set.add(new LPPosition(0, 0, -1));
		set.add(new LPPosition(0, 0, -2));
		set.add(new LPPosition(0, 0, -3));
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
		LPPosition pos = this.getLPPosition();
		LPPosition posMin = new LPPosition(LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MIN_POS, LPConstants.PIPE_MIN_POS);
		LPPosition posMax = new LPPosition(LPConstants.PIPE_MAX_POS, LPConstants.PIPE_MAX_POS, -3);
		orientation.rotatePositions(posMin);
		orientation.rotatePositions(posMax);
		if(orientation == SpeedupDirection.EAST) {
			pos.add(new LPPosition(1, 0, 0));
		} else if(orientation == SpeedupDirection.SOUTH) {
			pos.add(new LPPosition(1, 0, 1));
		} else if(orientation == SpeedupDirection.WEST) {
			pos.add(new LPPosition(0, 0, 1));
		}
		posMin.add(pos);
		posMax.add(pos);
		LPPositionSet set = new LPPositionSet();
		set.add(posMin);
		set.add(posMax);
		AxisAlignedBB box = set.toABB();
		if(box != null && (axisalignedbb == null || axisalignedbb.intersectsWith(box))) {
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
		if(w < 0) {
			w += 2 * Math.PI;
		}
		ForgeDirection dir = ForgeDirection.UNKNOWN;
		if(0 < w && w <= halfPI) {
			dir = ForgeDirection.WEST;
		} else if(halfPI < w && w <= 2*halfPI) {
			dir = ForgeDirection.SOUTH;
		} else if(2*halfPI < w && w <= 3*halfPI) {
			dir = ForgeDirection.EAST;
		} else if(3*halfPI < w && w <= 4*halfPI) {
			dir = ForgeDirection.NORTH;
		}
		for(SpeedupDirection ori:SpeedupDirection.values()) {
			if(ori.dir1.getOpposite().equals(dir)) {
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
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public TileEntity getConnectedEndTile(ForgeDirection output) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean renderNormalPipe() {
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
}
