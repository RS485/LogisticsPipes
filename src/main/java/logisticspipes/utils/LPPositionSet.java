package logisticspipes.utils;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;

import net.minecraft.util.math.AxisAlignedBB;

import lombok.SneakyThrows;

import network.rs485.logisticspipes.world.DoubleCoordinates;

public class LPPositionSet<T extends DoubleCoordinates> extends HashSet<T> implements IPositionRotateble {

	private static final long serialVersionUID = -3611750920959862658L;
	private Class<T> clazz;

	public LPPositionSet(Class<?> clazz) {
		this.clazz = (Class<T>) clazz;
	}

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
		double x = -Double.MAX_VALUE;
		for (DoubleCoordinates pos : this) {
			x = Math.max(x, pos.getXDouble());
		}
		return x;
	}

	public double getMaxYD() {
		double y = -Double.MAX_VALUE;
		for (DoubleCoordinates pos : this) {
			y = Math.max(y, pos.getYDouble());
		}
		return y;
	}

	public double getMaxZD() {
		double z = -Double.MAX_VALUE;
		for (DoubleCoordinates pos : this) {
			z = Math.max(z, pos.getZDouble());
		}
		return z;
	}

	public double getMinXD() {
		double x = Double.MAX_VALUE;
		for (DoubleCoordinates pos : this) {
			x = Math.min(x, pos.getXDouble());
		}
		return x;
	}

	public double getMinYD() {
		double y = Double.MAX_VALUE;
		for (DoubleCoordinates pos : this) {
			y = Math.min(y, pos.getYDouble());
		}
		return y;
	}

	public double getMinZD() {
		double z = Double.MAX_VALUE;
		for (DoubleCoordinates pos : this) {
			z = Math.min(z, pos.getZDouble());
		}
		return z;
	}

	public void addToAll(DoubleCoordinates lpPosition) {
		for (DoubleCoordinates pos : this) {
			pos.add(lpPosition);
		}
	}

	@Override
	public void rotateLeft() {
		this.forEach(DoubleCoordinates::rotateLeft);
	}

	@Override
	public void rotateRight() {
		this.forEach(DoubleCoordinates::rotateRight);
	}

	public AxisAlignedBB toABB() {
		return new AxisAlignedBB(getMinXD(), getMinYD(), getMinZD(), getMaxXD(), getMaxYD(), getMaxZD());
	}

	@Override
	public void mirrorX() {
		this.forEach(DoubleCoordinates::mirrorX);
	}

	@Override
	public void mirrorZ() {
		this.forEach(DoubleCoordinates::mirrorZ);
	}

	@SneakyThrows({ NoSuchMethodException.class, IllegalAccessException.class, InvocationTargetException.class, InstantiationException.class })
	public void addFrom(AxisAlignedBB completeBox) {
		add(clazz.getConstructor(Double.TYPE, Double.TYPE, Double.TYPE).newInstance(completeBox.minX, completeBox.minY, completeBox.minZ));
		add(clazz.getConstructor(Double.TYPE, Double.TYPE, Double.TYPE).newInstance(completeBox.maxX, completeBox.maxY, completeBox.maxZ));
	}

	public DoubleCoordinates getCenter() {
		return new DoubleCoordinates((getMaxXD() - getMinXD()) / 2 + getMinXD(), (getMaxYD() - getMinYD()) / 2 + getMinYD(), (getMaxZD() - getMinZD()) / 2 + getMinZD());
	}

	public T findClosest(DoubleCoordinates posToLookFor) {
		double distance = Double.MAX_VALUE;
		T closest = null;
		for (T posToLookAt : this) {
			if (closest == null || posToLookFor.distanceTo(posToLookAt) < distance) {
				closest = posToLookAt;
				distance = posToLookFor.distanceTo(posToLookAt);
			}
		}
		return closest;
	}
}
