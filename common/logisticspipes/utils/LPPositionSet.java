package logisticspipes.utils;

import java.util.HashSet;
import java.util.Set;

import logisticspipes.utils.tuples.LPPosition;

import net.minecraft.util.AxisAlignedBB;

public class LPPositionSet extends HashSet<LPPosition> implements IPositionRotateble {

	private static final long serialVersionUID = -3611750920959862658L;

	public int getMaxX() {
		return (int) getMaxXD();
	}

	public int getMaxY() {
		return (int) getMaxYD();
	}

	public int getMaxZ() {
		return (int) getMaxZD();
	}

	public int getMinX() {
		return (int) getMaxXD();
	}

	public int getMinY() {
		return (int) getMaxYD();
	}

	public int getMinZ() {
		return (int) getMaxZD();
	}

	public double getMaxXD() {
		double x = Integer.MIN_VALUE;
		for (LPPosition pos : this) {
			x = Math.max(x, pos.getXD());
		}
		return x;
	}

	public double getMaxYD() {
		double y = Integer.MIN_VALUE;
		for (LPPosition pos : this) {
			y = Math.max(y, pos.getYD());
		}
		return y;
	}

	public double getMaxZD() {
		double z = Integer.MIN_VALUE;
		for (LPPosition pos : this) {
			z = Math.max(z, pos.getZD());
		}
		return z;
	}

	public double getMinXD() {
		double x = Integer.MAX_VALUE;
		for (LPPosition pos : this) {
			x = Math.min(x, pos.getXD());
		}
		return x;
	}

	public double getMinYD() {
		double y = Integer.MAX_VALUE;
		for (LPPosition pos : this) {
			y = Math.min(y, pos.getYD());
		}
		return y;
	}

	public double getMinZD() {
		double z = Integer.MAX_VALUE;
		for (LPPosition pos : this) {
			z = Math.min(z, pos.getZD());
		}
		return z;
	}

	public void copyPositions() {
		Set<LPPosition> tmp = new HashSet<LPPosition>();
		for (LPPosition pos : this) {
			tmp.add(pos.copy());
		}
		clear();
		addAll(tmp);
	}

	public void addToAll(LPPosition lpPosition) {
		for (LPPosition pos : this) {
			pos.add(lpPosition);
		}
	}

	@Override
	public void rotateLeft() {
		for (LPPosition pos : this) {
			pos.rotateLeft();
		}
	}

	@Override
	public void rotateRight() {
		for (LPPosition pos : this) {
			pos.rotateRight();
		}
	}

	public AxisAlignedBB toABB() {
		return AxisAlignedBB.getBoundingBox(getMinXD(), getMinYD(), getMinZD(), getMaxXD(), getMaxYD(), getMaxZD());
	}

	@Override
	public void mirrorX() {
		for (LPPosition pos : this) {
			pos.mirrorX();
		}
	}

	@Override
	public void mirrorZ() {
		for (LPPosition pos : this) {
			pos.mirrorZ();
		}
	}

	public void addFrom(AxisAlignedBB completeBox) {
		add(new LPPosition(completeBox.minX, completeBox.minY, completeBox.minZ));
		add(new LPPosition(completeBox.maxX, completeBox.maxY, completeBox.maxZ));
	}

	public LPPosition getCenter() {
		return new LPPosition((getMaxXD() - getMinXD()) / 2 + getMinXD(), (getMaxYD() - getMinYD()) / 2 + getMinYD(), (getMaxZD() - getMinZD()) / 2 + getMinZD());
	}
}
