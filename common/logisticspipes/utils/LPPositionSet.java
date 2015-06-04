package logisticspipes.utils;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.util.AxisAlignedBB;
import logisticspipes.utils.tuples.LPPosition;

public class LPPositionSet extends HashSet<LPPosition> implements IPositionRotateble {
	private static final long	serialVersionUID	= -3611750920959862658L;

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
		for(LPPosition pos: this) {
			x = Math.max(x, pos.getXD());
		}
		return x;
	}
	
	public double getMaxYD() {
		double y = Integer.MIN_VALUE;
		for(LPPosition pos: this) {
			y = Math.max(y, pos.getYD());
		}
		return y;
	}
	
	public double getMaxZD() {
		double z = Integer.MIN_VALUE;
		for(LPPosition pos: this) {
			z = Math.max(z, pos.getZD());
		}
		return z;
	}

	public double getMinXD() {
		double x = Integer.MAX_VALUE;
		for(LPPosition pos: this) {
			x = Math.min(x, pos.getXD());
		}
		return x;
	}
	
	public double getMinYD() {
		double y = Integer.MAX_VALUE;
		for(LPPosition pos: this) {
			y = Math.min(y, pos.getYD());
		}
		return y;
	}
	
	public double getMinZD() {
		double z = Integer.MAX_VALUE;
		for(LPPosition pos: this) {
			z = Math.min(z, pos.getZD());
		}
		return z;
	}

	public void copyPositions() {
		Set<LPPosition> tmp = new HashSet<LPPosition>();
		for(LPPosition pos: this) {
			tmp.add(pos.copy());
		}
		this.clear();
		this.addAll(tmp);
	}

	public void addToAll(LPPosition lpPosition) {
		for(LPPosition pos: this) {
			pos.add(lpPosition);
		}
	}

	public void rotateLeft() {
		for(LPPosition pos: this) {
			pos.rotateLeft();
		}
	}
	
	public void rotateRight() {
		for(LPPosition pos: this) {
			pos.rotateRight();
		}
	}

	public AxisAlignedBB toABB() {
		return AxisAlignedBB.getBoundingBox(getMinXD(), getMinYD(), getMinZD(), getMaxXD(), getMaxYD(), getMaxZD());
	}
}
