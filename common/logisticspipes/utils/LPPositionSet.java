package logisticspipes.utils;

import java.util.HashSet;
import java.util.Set;

import network.rs485.logisticspipes.world.DoubleCoordinates;

import net.minecraft.util.AxisAlignedBB;

public class LPPositionSet extends HashSet<DoubleCoordinates> implements IPositionRotateble {

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
		for (DoubleCoordinates pos : this) {
			x = Math.max(x, pos.getXD());
		}
		return x;
	}

	public double getMaxYD() {
		double y = Integer.MIN_VALUE;
		for (DoubleCoordinates pos : this) {
			y = Math.max(y, pos.getYD());
		}
		return y;
	}

	public double getMaxZD() {
		double z = Integer.MIN_VALUE;
		for (DoubleCoordinates pos : this) {
			z = Math.max(z, pos.getZD());
		}
		return z;
	}

	public double getMinXD() {
		double x = Integer.MAX_VALUE;
		for (DoubleCoordinates pos : this) {
			x = Math.min(x, pos.getXD());
		}
		return x;
	}

	public double getMinYD() {
		double y = Integer.MAX_VALUE;
		for (DoubleCoordinates pos : this) {
			y = Math.min(y, pos.getYD());
		}
		return y;
	}

	public double getMinZD() {
		double z = Integer.MAX_VALUE;
		for (DoubleCoordinates pos : this) {
			z = Math.min(z, pos.getZD());
		}
		return z;
	}

	public void copyPositions() {
		Set<DoubleCoordinates> tmp = new HashSet<DoubleCoordinates>();
		for (DoubleCoordinates pos : this) {
			tmp.add(pos.copy());
		}
		clear();
		addAll(tmp);
	}

	public void addToAll(DoubleCoordinates lpPosition) {
		for (DoubleCoordinates pos : this) {
			pos.add(lpPosition);
		}
	}

	@Override
	public void rotateLeft() {
		for (DoubleCoordinates pos : this) {
			pos.rotateLeft();
		}
	}

	@Override
	public void rotateRight() {
		for (DoubleCoordinates pos : this) {
			pos.rotateRight();
		}
	}

	public AxisAlignedBB toABB() {
		return AxisAlignedBB.getBoundingBox(getMinXD(), getMinYD(), getMinZD(), getMaxXD(), getMaxYD(), getMaxZD());
	}

	@Override
	public void mirrorX() {
		for (DoubleCoordinates pos : this) {
			pos.mirrorX();
		}
	}

	@Override
	public void mirrorZ() {
		for (DoubleCoordinates pos : this) {
			pos.mirrorZ();
		}
	}

	public void addFrom(AxisAlignedBB completeBox) {
		add(new DoubleCoordinates(completeBox.minX, completeBox.minY, completeBox.minZ));
		add(new DoubleCoordinates(completeBox.maxX, completeBox.maxY, completeBox.maxZ));
	}

	public DoubleCoordinates getCenter() {
		return new DoubleCoordinates((getMaxXD() - getMinXD()) / 2 + getMinXD(), (getMaxYD() - getMinYD()) / 2 + getMinYD(), (getMaxZD() - getMinZD()) / 2 + getMinZD());
	}
}
