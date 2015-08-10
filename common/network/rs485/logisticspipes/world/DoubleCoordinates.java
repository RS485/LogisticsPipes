package network.rs485.logisticspipes.world;

import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import logisticspipes.utils.IPositionRotateble;
import logisticspipes.utils.tuples.Triplet;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.common.util.ForgeDirection;

public class DoubleCoordinates extends Triplet<Double, Double, Double> implements IPositionRotateble {

	public DoubleCoordinates(double xPos, double yPos, double zPos) {
		super(xPos, yPos, zPos);
	}

	public DoubleCoordinates(int xPos, int yPos, int zPos) {
		super((double) xPos, (double) yPos, (double) zPos);
	}

	public DoubleCoordinates(TileEntity tile) {
		super((double) tile.xCoord, (double) tile.yCoord, (double) tile.zCoord);
	}

	public DoubleCoordinates(CoreUnroutedPipe pipe) {
		super((double) pipe.getX(), (double) pipe.getY(), (double) pipe.getZ());
	}

	public DoubleCoordinates(IPipeInformationProvider pipe) {
		super((double) pipe.getX(), (double) pipe.getY(), (double) pipe.getZ());
	}

	public DoubleCoordinates(CoordinatesPacket packet) {
		super((double) packet.getPosX(), (double) packet.getPosY(), (double) packet.getPosZ());
	}

	public DoubleCoordinates(Entity entity) {
		super(entity.posX, entity.posY, entity.posZ);
	}

	public int getX() {
		return (int) (double) getValue1();
	}

	public int getY() {
		return (int) (double) getValue2();
	}

	public int getZ() {
		return (int) (double) getValue3();
	}

	public double getXD() {
		return getValue1();
	}

	public double getYD() {
		return getValue2();
	}

	public double getZD() {
		return getValue3();
	}

	public TileEntity getTileEntity(IBlockAccess world) {
		return world.getTileEntity(getX(), getY(), getZ());
	}

	public DoubleCoordinates moveForward(ForgeDirection dir, double steps) {
		switch (dir) {
			case UP:
				value2 += steps;
				break;
			case DOWN:
				value2 -= steps;
				break;
			case NORTH:
				value3 -= steps;
				break;
			case SOUTH:
				value3 += steps;
				break;
			case EAST:
				value1 += steps;
				break;
			case WEST:
				value1 -= steps;
				break;
			default:
		}
		return this;
	}

	public DoubleCoordinates moveForward(ForgeDirection dir) {
		return moveForward(dir, 1);
	}

	public DoubleCoordinates moveBackward(ForgeDirection dir, double steps) {
		return moveForward(dir, -1 * steps);
	}

	public DoubleCoordinates moveBackward(ForgeDirection dir) {
		return moveBackward(dir, 1);
	}

	@Override
	public String toString() {
		return "(" + getXD() + ", " + getYD() + ", " + getZD() + ")";
	}

	public String toIntBasedString() {
		return "(" + getXD() + ", " + getYD() + ", " + getZD() + ")";
	}

	@Override
	public DoubleCoordinates copy() {
		return new DoubleCoordinates(value1, value2, value3);
	}

	public Block getBlock(IBlockAccess world) {
		return world.getBlock(getX(), getY(), getZ());
	}

	public boolean blockExists(World world) {
		return world.blockExists(getX(), getY(), getZ());
	}

	public double distanceTo(DoubleCoordinates targetPos) {
		return Math.sqrt(Math.pow(targetPos.getXD() - getXD(), 2) + Math.pow(targetPos.getYD() - getYD(), 2) + Math.pow(targetPos.getZD() - getZD(), 2));
	}

	public DoubleCoordinates center() {
		value1 += 0.5D;
		value2 += 0.5D;
		value3 += 0.5D;
		return this;
	}

	public void writeToNBT(String prefix, NBTTagCompound nbt) {
		nbt.setDouble(prefix + "xPos", value1);
		nbt.setDouble(prefix + "yPos", value2);
		nbt.setDouble(prefix + "zPos", value3);
	}

	public static DoubleCoordinates readFromNBT(String prefix, NBTTagCompound nbt) {
		if (nbt.hasKey(prefix + "xPos") && nbt.hasKey(prefix + "yPos") && nbt.hasKey(prefix + "zPos")) {
			return new DoubleCoordinates(nbt.getDouble(prefix + "xPos"), nbt.getDouble(prefix + "yPos"), nbt.getDouble(prefix + "zPos"));
		}
		return null;
	}

	public DoubleCoordinates add(DoubleCoordinates toAdd) {
		value1 += toAdd.value1;
		value2 += toAdd.value2;
		value3 += toAdd.value3;
		return this;
	}

	public void setBlockToAir(World world) {
		world.setBlockToAir(getX(), getY(), getZ());
	}

	@Override
	public void rotateLeft() {
		double tmp = value3;
		value3 = -value1;
		value1 = tmp;
	}

	@Override
	public void rotateRight() {
		double tmp = value1;
		value1 = -value3;
		value3 = tmp;
	}

	@Override
	public void mirrorX() {
		value1 = -value1;
	}

	@Override
	public void mirrorZ() {
		value3 = -value3;
	}
}
