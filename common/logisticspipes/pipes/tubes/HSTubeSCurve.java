package logisticspipes.pipes.tubes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
	@SideOnly(Side.CLIENT)
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addCollisionBoxesToList(List arraylist, AxisAlignedBB axisalignedbb) {
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
				AxisAlignedBB box = SCurveTubeRenderer.getObjectBoundsAt(AxisAlignedBB
						.getBoundingBox(Math.min(xOne, xTwo), Math.min(yOne, yTwo), Math.min(zOne, zTwo), Math.max(xOne, xTwo), Math.max(yOne, yTwo),
								Math.max(zOne, zTwo)).getOffsetBoundingBox(-x, -y, -z), orientation);
				if (box != null) {
					LPPositionSet<DoubleCoordinates> lpBox = new LPPositionSet<>(DoubleCoordinates.class);
					lpBox.addFrom(box);
					DoubleCoordinates center = lpBox.getCenter();
					box = AxisAlignedBB
							.getBoundingBox(center.getXCoord() - 0.3D, center.getYCoord() - 0.3D, center.getZCoord() - 0.3D, center.getXCoord() + 0.3D,
									center.getYCoord() + 0.3D, center.getZCoord() + 0.3D);
					if (box != null) {
						AxisAlignedBB cBox = getCompleteBox();
						if (box.minX < cBox.minX) {
							box.minX = cBox.minX;
						}
						if (box.minY < cBox.minY) {
							box.minY = cBox.minY;
						}
						if (box.minZ < cBox.minZ) {
							box.minZ = cBox.minZ;
						}
						if (box.maxX > cBox.maxX) {
							box.maxX = cBox.maxX;
						}
						if (box.maxY > cBox.maxY) {
							box.maxY = cBox.maxY;
						}
						if (box.maxZ > cBox.maxZ) {
							box.maxZ = cBox.maxZ;
						}
						box = box.getOffsetBoundingBox(x, y, z);
						boxes.add(box);
					}
				}
			}
		}
		arraylist.addAll(boxes.stream()
				.filter(box -> box != null && (axisalignedbb == null || axisalignedbb.intersectsWith(box)))
				.collect(Collectors.toList()));
	}

	@Override
	@SideOnly(Side.CLIENT)
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
		ForgeDirection dir = ForgeDirection.UNKNOWN;
		ForgeDirection dir1 = ForgeDirection.UNKNOWN;
		ForgeDirection dir2 = ForgeDirection.UNKNOWN;
		double addition = 0;
		if (0 < w && w <= halfPI) {
			dir = ForgeDirection.EAST;
			dir1 = ForgeDirection.NORTH;
			dir2 = ForgeDirection.SOUTH;
			addition = halfPI;
		} else if (halfPI < w && w <= 2 * halfPI) {
			dir = ForgeDirection.NORTH;
			dir1 = ForgeDirection.EAST;
			dir2 = ForgeDirection.WEST;
		} else if (2 * halfPI < w && w <= 3 * halfPI) {
			dir = ForgeDirection.WEST;
			dir1 = ForgeDirection.NORTH;
			dir2 = ForgeDirection.SOUTH;
			addition = halfPI;
		} else if (3 * halfPI < w && w <= 4 * halfPI) {
			dir = ForgeDirection.SOUTH;
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
	public ForgeDirection getExitForInput(ForgeDirection commingFrom) {
		if (orientation.dir.getOpposite() == commingFrom) {
			return orientation.dir;
		}
		if (orientation.dir == commingFrom) {
			return orientation.dir.getOpposite();
		}
		return null;
	}

	@Override
	public TileEntity getConnectedEndTile(ForgeDirection output) {
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
		double b = 0;
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
		NORTH_EAST(TurnSDirection.NORTH_INV, new DoubleCoordinates(0, 0, 0), ForgeDirection.NORTH, ForgeDirection.EAST),
		NORTH_WEST(TurnSDirection.NORTH, new DoubleCoordinates(0, 0, 0), ForgeDirection.NORTH, ForgeDirection.WEST),
		EAST_SOUTH(TurnSDirection.EAST_INV, new DoubleCoordinates(0, 0, 0), ForgeDirection.EAST, ForgeDirection.SOUTH),
		EAST_NORTH(TurnSDirection.EAST, new DoubleCoordinates(0, 0, 0), ForgeDirection.EAST, ForgeDirection.NORTH),
		SOUTH_WEST(TurnSDirection.NORTH_INV, new DoubleCoordinates(-1, 0, 3), ForgeDirection.SOUTH, ForgeDirection.WEST),
		SOUTH_EAST(TurnSDirection.NORTH, new DoubleCoordinates(1, 0, 3), ForgeDirection.SOUTH, ForgeDirection.EAST),
		WEST_NORTH(TurnSDirection.EAST_INV, new DoubleCoordinates(-3, 0, -1), ForgeDirection.WEST, ForgeDirection.NORTH),
		WEST_SOUTH(TurnSDirection.EAST, new DoubleCoordinates(-3, 0, 1), ForgeDirection.WEST, ForgeDirection.SOUTH);
		//@formatter:on

		@Getter
		TurnSDirection renderOrientation;
		@Getter
		DoubleCoordinates offset;
		@Getter
		ForgeDirection dir;
		@Getter
		ForgeDirection looking;

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
		NORTH(ForgeDirection.NORTH),
		EAST(ForgeDirection.EAST),
		NORTH_INV(ForgeDirection.SOUTH),
		EAST_INV(ForgeDirection.WEST);
		//@formatter:on

		@Getter
		private ForgeDirection dir1;

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
