package logisticspipes.pipes.basic;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Direction;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import logisticspipes.interfaces.ITubeOrientation;
import logisticspipes.renderer.newpipe.ISpecialPipeRenderer;
import logisticspipes.transport.LPTravelingItem;
import logisticspipes.transport.PipeMultiBlockTransportLogistics;
import logisticspipes.utils.LPPositionSet;
import logisticspipes.utils.tuples.Tuple2;
import network.rs485.logisticspipes.world.DoubleCoordinatesType;

public abstract class CoreMultiBlockPipe extends CoreUnroutedPipe {

	public enum SubBlockTypeForShare {
		NON_SHARE,
		S_CURVE_A,
		S_CURVE_B,
		CURVE_OUT_A,
		CURVE_INNER_A,
		CURVE_OUT_B,
		CURVE_INNER_B,
		GAIN_A,
		GAIN_B
	}

	private static List<Tuple2<SubBlockTypeForShare, SubBlockTypeForShare>> allowedCombinations;

	static {
		allowedCombinations = new ArrayList<>();
		allowedCombinations.add(new Tuple2<>(SubBlockTypeForShare.S_CURVE_A, SubBlockTypeForShare.S_CURVE_B));
		allowedCombinations.add(new Tuple2<>(SubBlockTypeForShare.S_CURVE_A, SubBlockTypeForShare.S_CURVE_A));
		allowedCombinations.add(new Tuple2<>(SubBlockTypeForShare.CURVE_OUT_A, SubBlockTypeForShare.CURVE_INNER_A));
		allowedCombinations.add(new Tuple2<>(SubBlockTypeForShare.CURVE_OUT_B, SubBlockTypeForShare.CURVE_INNER_B));
		allowedCombinations.add(new Tuple2<>(SubBlockTypeForShare.CURVE_OUT_A, SubBlockTypeForShare.S_CURVE_A));
		allowedCombinations.add(new Tuple2<>(SubBlockTypeForShare.CURVE_OUT_B, SubBlockTypeForShare.S_CURVE_A));
		allowedCombinations.add(new Tuple2<>(SubBlockTypeForShare.GAIN_A, SubBlockTypeForShare.GAIN_B));
		allowedCombinations.add(new Tuple2<>(SubBlockTypeForShare.GAIN_A, SubBlockTypeForShare.GAIN_A));
	}

	public static boolean canShare(List<SubBlockTypeForShare> list, SubBlockTypeForShare toAdd) {
		if (toAdd == SubBlockTypeForShare.NON_SHARE) return false;
		if (toAdd == null) return false;
		if (list.size() > 1) return false;
		if (list.isEmpty()) return true;
		SubBlockTypeForShare contained = list.get(0);
		if (contained == SubBlockTypeForShare.NON_SHARE) return false;
		for (Tuple2<SubBlockTypeForShare, SubBlockTypeForShare> allowed : allowedCombinations) {
			if (allowed.getValue1() == contained) {
				if (allowed.getValue2() == toAdd) {
					return true;
				}
			}
			if (allowed.getValue2() == contained) {
				if (allowed.getValue1() == toAdd) {
					return true;
				}
			}
		}
		return false;
	}

	public CoreMultiBlockPipe(PipeMultiBlockTransportLogistics transport, Item item) {
		super(transport, item);
	}

	@Override
	public boolean isMultiBlock() {
		return true;
	}

	/**
	 * North Orientated
	 *
	 * @return Relative Positions
	 */
	public abstract LPPositionSet<DoubleCoordinatesType<SubBlockTypeForShare>> getSubBlocks();

	public abstract LPPositionSet<DoubleCoordinatesType<SubBlockTypeForShare>> getRotatedSubBlocks();

	public abstract void addCollisionBoxesToList(List<AxisAlignedBB> arraylist, AxisAlignedBB axisalignedbb);

	public abstract AxisAlignedBB getCompleteBox();

	public abstract ITubeOrientation getTubeOrientation(EntityPlayer player, int xPos, int zPos);

	public abstract float getPipeLength();

	public double getDistanceWeight() {
		return 1.0 / 8.0;
	}

	public float getYawDiff(LPTravelingItem item) {
		return (float) (getItemRenderYaw(getPipeLength(), item) - getItemRenderYaw(0.0F, item));
	}

	public abstract Direction getExitForInput(Direction commingFrom);

	public abstract BlockEntity getConnectedEndTile(Direction output);

	@Override
	public abstract boolean actAsNormalPipe();

	@Override
	@SideOnly(Side.CLIENT)
	public abstract ISpecialPipeRenderer getSpecialRenderer();

	@Override
	public boolean canPipeConnect(BlockEntity tile, Direction side) {
		if (tile instanceof LogisticsTileGenericSubMultiBlock) {
			return true;
		}
		if (tile instanceof LogisticsTileGenericPipe) {
			if (((LogisticsTileGenericPipe) tile).pipe.isMultiBlock()) {
				return true;
			}
		}
		return super.canPipeConnect(tile, side);
	}
}
