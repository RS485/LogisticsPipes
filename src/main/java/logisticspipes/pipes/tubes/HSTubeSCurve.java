package logisticspipes.pipes.tubes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import lombok.AllArgsConstructor;
import lombok.Getter;

import logisticspipes.interfaces.ITubeOrientation;
import logisticspipes.interfaces.ITubeRenderOrientation;
import logisticspipes.pipes.basic.CoreMultiBlockPipe;
import logisticspipes.pipes.basic.LogisticsTileGenericSubMultiBlock;
import logisticspipes.renderer.newpipe.IHighlightPlacementRenderer;
import logisticspipes.renderer.newpipe.ISpecialPipeRenderer;
import logisticspipes.renderer.newpipe.tube.SCurveTubeRenderer;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.transport.PipeMultiBlockTransportLogistics;
import logisticspipes.utils.IPositionRotateble;
import logisticspipes.utils.LPPositionSet;
import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;
import network.rs485.logisticspipes.world.DoubleCoordinates;
import network.rs485.logisticspipes.world.DoubleCoordinatesType;

public class HSTubeSCurve extends CoreMultiBlockPipe {

	@Getter
	private CurveSOrientation orientation;
	private List<AxisAlignedBB> boxes = null;

	public HSTubeSCurve(Item item) {
		super(new PipeMultiBlockTransportLogistics(), item);
	}

	@Override
	public void writeData(LPDataOutput output) {
		if (orientation == null) {
			output.writeBoolean(false);
		} else {
			output.writeBoolean(true);
			output.writeEnum(orientation);
		}
	}

	@Override
	public void readData(LPDataInput input) {
		if (input.readBoolean()) {
			orientation = input.readEnum(CurveSOrientation.class);
		}
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
	public LPPositionSet<DoubleCoordinatesType<SubBlockTypeForShare>> getSubBlocks() {
		LPPositionSet<DoubleCoordinatesType<SubBlockTypeForShare>> list = new LPPositionSet<>(DoubleCoordinatesType.class);
		list.add(new DoubleCoordinatesType<>(0, 0, -1, SubBlockTypeForShare.S_CURVE_B));
		list.add(new DoubleCoordinatesType<>(0, 0, -2, SubBlockTypeForShare.S_CURVE_A));
		list.add(new DoubleCoordinatesType<>(1, 0, -1, SubBlockTypeForShare.S_CURVE_A));
		list.add(new DoubleCoordinatesType<>(1, 0, -2, SubBlockTypeForShare.S_CURVE_B));
		list.add(new DoubleCoordinatesType<>(1, 0, -3, SubBlockTypeForShare.NON_SHARE));
		return list;
	}

	@Override
	public LPPositionSet<DoubleCoordinatesType<SubBlockTypeForShare>> getRotatedSubBlocks() {
		LPPositionSet<DoubleCoordinatesType<SubBlockTypeForShare>> set = getSubBlocks();
		orientation.rotatePositions(set);
		return set;
	}

	@Override
	public void addCollisionBoxesToList(List<AxisAlignedBB> arraylist, AxisAlignedBB axisalignedbb) {
		if (boxes == null || boxes.isEmpty()) {
			boxes = new ArrayList<>();
			double x = getX();
			double y = getY();
			double z = getZ();
			for (int i = -1; i < 54; i++) {
				double xOne = x;
				double yOne = y;
				double zOne = z;
				double xTwo = x;
				double yTwo = y + 1;
				double zTwo = z;
				if (orientation.getRenderOrientation() == TurnSDirection.NORTH_INV || orientation.getRenderOrientation() == TurnSDirection.NORTH) {
					zOne += 1;
					zTwo += 1;
					zOne -= 4.0F * (i - 4) / 50;
					zTwo -= 4.0F * (i + 4) / 50;
					xOne -= 1;
					xTwo += 2;
				} else if (orientation.getRenderOrientation() == TurnSDirection.EAST_INV || orientation.getRenderOrientation() == TurnSDirection.EAST) {
					xOne += 4;
					xTwo += 4;
					xOne -= 4.0F * (i - 4) / 50;
					xTwo -= 4.0F * (i + 4) / 50;
					zOne -= 1;
					zTwo += 2;
				}
				AxisAlignedBB box = SCurveTubeRenderer.getObjectBoundsAt(new AxisAlignedBB(Math.min(xOne, xTwo), Math.min(yOne, yTwo), Math.min(zOne, zTwo), Math.max(xOne, xTwo), Math.max(yOne, yTwo),
						Math.max(zOne, zTwo)).offset(-x, -y, -z), orientation);
				if (box != null) {
					LPPositionSet<DoubleCoordinates> lpBox = new LPPositionSet<>(DoubleCoordinates.class);
					lpBox.addFrom(box);
					DoubleCoordinates center = lpBox.getCenter();
					box = new AxisAlignedBB(center.getXCoord() - 0.3D, center.getYCoord() - 0.3D, center.getZCoord() - 0.3D, center.getXCoord() + 0.3D,
							center.getYCoord() + 0.3D, center.getZCoord() + 0.3D);
					AxisAlignedBB cBox = getCompleteBox();
					double minX = Math.max(box.minX, cBox.minX);
					double minY = Math.max(box.minY, cBox.minY);
					double minZ = Math.max(box.minZ, cBox.minZ);
					double maxX = Math.min(box.maxX, cBox.maxX);
					double maxY = Math.min(box.maxY, cBox.maxY);
					double maxZ = Math.min(box.maxZ, cBox.maxZ);
					boxes.add(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).offset(x, y, z));
				}
			}
		}
		arraylist.addAll(boxes.stream()
				.filter(box -> box != null && (axisalignedbb == null || axisalignedbb.intersects(box)))
				.collect(Collectors.toList()));
	}

	@Override
	public AxisAlignedBB getCompleteBox() {
		return SCurveTubeRenderer.tubeSCurve.get(orientation.getRenderOrientation()).bounds().toAABB();
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
		EnumFacing dir1 = null;
		EnumFacing dir2 = null;
		double addition = 0;
		if (0 < w && w <= halfPI) {
			dir = EnumFacing.EAST;
			dir1 = EnumFacing.NORTH;
			dir2 = EnumFacing.SOUTH;
			addition = halfPI;
		} else if (halfPI < w && w <= 2 * halfPI) {
			dir = EnumFacing.NORTH;
			dir1 = EnumFacing.EAST;
			dir2 = EnumFacing.WEST;
		} else if (2 * halfPI < w && w <= 3 * halfPI) {
			dir = EnumFacing.WEST;
			dir1 = EnumFacing.NORTH;
			dir2 = EnumFacing.SOUTH;
			addition = halfPI;
		} else if (3 * halfPI < w && w <= 4 * halfPI) {
			dir = EnumFacing.SOUTH;
			dir1 = EnumFacing.EAST;
			dir2 = EnumFacing.WEST;
		}
		w = Math.atan2(player.getLookVec().x, player.getLookVec().z);
		w -= addition;
		if (w < 0) {
			w += 2 * Math.PI;
		}
		EnumFacing dir3 = null;
		if (0 < w && w <= 2 * halfPI) {
			dir3 = dir1;
		} else if (2 * halfPI < w && w <= 4 * halfPI) {
			dir3 = dir2;
		}
		for (CurveSOrientation curve : CurveSOrientation.values()) {
			if (curve.dir.equals(dir)) {
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
		orientation = CurveSOrientation.valueOf(data.getString("orientation"));
	}

	@Override
	public float getPipeLength() {
		return 4;
	}

	@Override
	public EnumFacing getExitForInput(EnumFacing commingFrom) {
		if (orientation.dir.getOpposite() == commingFrom) {
			return orientation.dir;
		}
		if (orientation.dir == commingFrom) {
			return orientation.dir.getOpposite();
		}
		return null;
	}

	@Override
	public TileEntity getConnectedEndTile(EnumFacing output) {
		boolean useOwn;
		if (orientation.getOffset().getLength() != 0) {
			if (orientation.dir.getOpposite() == output) {
				useOwn = false;
			} else if (orientation.dir == output) {
				useOwn = true;
			} else {
				return null;
			}
		} else {
			if (orientation.dir.getOpposite() == output) {
				useOwn = true;
			} else if (orientation.dir == output) {
				useOwn = false;
			} else {
				return null;
			}
		}
		if (useOwn) {
			return container.getTile(output);
		} else {
			DoubleCoordinates pos = new DoubleCoordinates(1, 0, -3);
			LPPositionSet<DoubleCoordinates> set = new LPPositionSet<>(DoubleCoordinates.class);
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
		return SCurveTubeRenderer.instance;
	}

	@Override
	public IHighlightPlacementRenderer getHighlightRenderer() {
		return SCurveTubeRenderer.instance;
	}

	@Override
	public DoubleCoordinates getItemRenderPos(float fPos, LPTravelingItem travelItem) {
		if ((orientation.getDir().getOpposite() == travelItem.input) == (orientation.getOffset().getLength() != 0)) {
			fPos = transport.getPipeLength() - fPos;
		}

		float x = 0.5F;
		float y = 0.5F;
		float z = 0.5F;
		if (orientation.getRenderOrientation() == TurnSDirection.NORTH) {
			z -= fPos;
			z += 0.5F;
			x -= 0F;
			double a = fPos / transport.getPipeLength() * 3;
			double b = -0.030238483815369 * Math.pow(a, 5) + 0.225914176523007 * Math.pow(a, 4) - 0.502711673373567 * Math.pow(a, 3) + 0.233256545765967 * Math
					.pow(a, 2) - 0.074807924321475 * a + 0.000099653425518;
			x += b * transport.getPipeLength() / 3;
		} else if (orientation.getRenderOrientation() == TurnSDirection.NORTH_INV) {
			z -= fPos;
			z += 0.5F;
			x -= 0F;
			double a = fPos / transport.getPipeLength() * 3;
			double b = -0.030238483815369 * Math.pow(a, 5) + 0.225914176523007 * Math.pow(a, 4) - 0.502711673373567 * Math.pow(a, 3) + 0.233256545765967 * Math
					.pow(a, 2) - 0.074807924321475 * a + 0.000099653425518;
			x -= b * transport.getPipeLength() / 3;
		} else if (orientation.getRenderOrientation() == TurnSDirection.EAST) {
			x -= fPos;
			x += 3.5F;
			z -= 1F;
			double a = fPos / transport.getPipeLength() * 3;
			double b = -0.030238483815369 * Math.pow(a, 5) + 0.225914176523007 * Math.pow(a, 4) - 0.502711673373567 * Math.pow(a, 3) + 0.233256545765967 * Math
					.pow(a, 2) - 0.074807924321475 * a + 0.000099653425518;
			z -= b * transport.getPipeLength() / 3;
		} else if (orientation.getRenderOrientation() == TurnSDirection.EAST_INV) {
			x -= fPos;
			x += 3.5F;
			z += 1F;
			double a = fPos / transport.getPipeLength() * 3;
			double b = -0.030238483815369 * Math.pow(a, 5) + 0.225914176523007 * Math.pow(a, 4) - 0.502711673373567 * Math.pow(a, 3) + 0.233256545765967 * Math
					.pow(a, 2) - 0.074807924321475 * a + 0.000099653425518;
			z += b * transport.getPipeLength() / 3;
		}
		return new DoubleCoordinates(x, y, z);
	}

	@Override
	public double getItemRenderPitch(float fPos, LPTravelingItem travelItem) {
		return 0;
	}

	@Override
	public double getItemRenderYaw(float fPos, LPTravelingItem travelItem) {
		if ((orientation.getDir().getOpposite() == travelItem.input) == (orientation.getOffset().getLength() != 0)) {
			fPos = transport.getPipeLength() - fPos;
		}
		double b;
		if (fPos < 0.5) {
			double a = 0.5 / transport.getPipeLength() * 3;
			b = -0.15119241907684 * Math.pow(a, 4) + 0.903656706092028 * Math.pow(a, 3) - 1.50813502012070 * Math.pow(a, 2) + 0.466513091531934 * a
					- 0.074807924321475;
			b = b * transport.getPipeLength() * -13;
			b = b * fPos / 0.5;
		} else if (fPos < 3.5) {
			double a = fPos / transport.getPipeLength() * 3;
			b = -0.15119241907684 * Math.pow(a, 4) + 0.903656706092028 * Math.pow(a, 3) - 1.50813502012070 * Math.pow(a, 2) + 0.466513091531934 * a
					- 0.074807924321475;
			b = b * transport.getPipeLength() * -13;
		} else {
			double a = 3.5 / transport.getPipeLength() * 3;
			b = -0.15119241907684 * Math.pow(a, 4) + 0.903656706092028 * Math.pow(a, 3) - 1.50813502012070 * Math.pow(a, 2) + 0.466513091531934 * a
					- 0.074807924321475;
			b = b * transport.getPipeLength() * -13;
			b = b * (transport.getPipeLength() - fPos) / (transport.getPipeLength() - 3.5);
		}
		if (orientation.getRenderOrientation() == TurnSDirection.NORTH) {
			return b;
		} else if (orientation.getRenderOrientation() == TurnSDirection.NORTH_INV) {
			return -b;
		} else if (orientation.getRenderOrientation() == TurnSDirection.EAST) {
			return 90 + b;
		} else if (orientation.getRenderOrientation() == TurnSDirection.EAST_INV) {
			return 90 - b;
		}
		return 0;
	}

	@Override
	public double getBoxRenderScale(float fPos, LPTravelingItem travelItem) {
		return 1;
	}

	@Override
	public boolean isHSTube() {
		return true;
	}

	@AllArgsConstructor
	public enum CurveSOrientation implements ITubeOrientation {
		//@formatter:off
		// Name: Placement from  _ TurnDirection
		NORTH_EAST(TurnSDirection.NORTH_INV, new DoubleCoordinates(0, 0, 0), EnumFacing.NORTH, EnumFacing.EAST),
		NORTH_WEST(TurnSDirection.NORTH, new DoubleCoordinates(0, 0, 0), EnumFacing.NORTH, EnumFacing.WEST),
		EAST_SOUTH(TurnSDirection.EAST_INV, new DoubleCoordinates(0, 0, 0), EnumFacing.EAST, EnumFacing.SOUTH),
		EAST_NORTH(TurnSDirection.EAST, new DoubleCoordinates(0, 0, 0), EnumFacing.EAST, EnumFacing.NORTH),
		SOUTH_WEST(TurnSDirection.NORTH_INV, new DoubleCoordinates(-1, 0, 3), EnumFacing.SOUTH, EnumFacing.WEST),
		SOUTH_EAST(TurnSDirection.NORTH, new DoubleCoordinates(1, 0, 3), EnumFacing.SOUTH, EnumFacing.EAST),
		WEST_NORTH(TurnSDirection.EAST_INV, new DoubleCoordinates(-3, 0, -1), EnumFacing.WEST, EnumFacing.NORTH),
		WEST_SOUTH(TurnSDirection.EAST, new DoubleCoordinates(-3, 0, 1), EnumFacing.WEST, EnumFacing.SOUTH);
		//@formatter:on

		@Getter
		TurnSDirection renderOrientation;
		@Getter
		DoubleCoordinates offset;
		@Getter
		EnumFacing dir;
		@Getter
		EnumFacing looking;

		@Override
		public void rotatePositions(IPositionRotateble set) {
			renderOrientation.rotatePositions(set);
		}

		@Override
		public void setOnPipe(CoreMultiBlockPipe pipe) {
			((HSTubeSCurve) pipe).orientation = this;
		}
	}

	@AllArgsConstructor
	public enum TurnSDirection implements ITubeRenderOrientation {
		//@formatter:off
		NORTH(EnumFacing.NORTH),
		EAST(EnumFacing.EAST),
		NORTH_INV(EnumFacing.SOUTH),
		EAST_INV(EnumFacing.WEST);
		//@formatter:on

		@Getter
		private EnumFacing dir1;

		public void rotatePositions(IPositionRotateble set) {
			if (this == NORTH) {
				set.mirrorX();
			} else if (this == EAST) {
				set.mirrorX();
				set.rotateRight();
			} else if (this == EAST_INV) {
				set.rotateRight();
			}
		}
	}
}
