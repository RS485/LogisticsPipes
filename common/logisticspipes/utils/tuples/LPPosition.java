package logisticspipes.utils.tuples;

import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;
import logisticspipes.utils.IPositionRotateble;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class LPPosition extends Triplet<Double, Double, Double> implements IPositionRotateble {

	public LPPosition(double xPos, double yPos, double zPos) {
		super(xPos, yPos, zPos);
	}

	public LPPosition(int xPos, int yPos, int zPos) {
		super((double)xPos, (double)yPos, (double)zPos);
	}

	public LPPosition(TileEntity tile) {
		super((double)tile.xCoord, (double)tile.yCoord, (double)tile.zCoord);
	}
	
	public LPPosition(CoreUnroutedPipe pipe) {
		super((double)pipe.getX(), (double)pipe.getY(), (double)pipe.getZ());
	}
	
	public LPPosition(IPipeInformationProvider pipe) {
		super((double)pipe.getX(), (double)pipe.getY(), (double)pipe.getZ());
	}

	public LPPosition(CoordinatesPacket packet) {
		super((double)packet.getPosX(), (double)packet.getPosY(), (double)packet.getPosZ());
	}

	public LPPosition(Entity entity) {
		super(entity.posX, entity.posY, entity.posZ);
	}

	public int getX() {
		return (int)(double)this.getValue1();
	}
	
	public int getY() {
		return (int)(double)this.getValue2();
	}
	
	public int getZ() {
		return (int)(double)this.getValue3();
	}

	public double getXD() {
		return this.getValue1();
	}
	
	public double getYD() {
		return this.getValue2();
	}
	
	public double getZD() {
		return this.getValue3();
	}
	
	public TileEntity getTileEntity(IBlockAccess world) {
		return world.getTileEntity(getX(), getY(), getZ());
	}
	
	public LPPosition moveForward(ForgeDirection dir, double steps) {
		switch(dir) {
			case UP:
				this.value2 += steps;
				break;
			case DOWN:
				this.value2 -= steps;
				break;
			case NORTH:
				this.value3 -= steps;
				break;
			case SOUTH:
				this.value3 += steps;
				break;
			case EAST:
				this.value1 += steps;
				break;
			case WEST:
				this.value1 -= steps;
				break;
			default:
		}
		return this;
	}

	public LPPosition moveForward(ForgeDirection dir) {
		return moveForward(dir, 1);
	}
	
	public LPPosition moveBackward(ForgeDirection dir, double steps) {
		return moveForward(dir, -1 * steps);
	}

	public LPPosition moveBackward(ForgeDirection dir) {
		return moveBackward(dir, 1);
	}

	@Override
	public String toString() {
		return "(" + this.getXD() + ", " + this.getYD() + ", " + this.getZD() + ")";
	}
	
	public String toIntBasedString() {
		return "(" + this.getXD() + ", " + this.getYD() + ", " + this.getZD() + ")";
	}

	@Override
	public LPPosition copy() {
		return new LPPosition(value1, value2, value3);
	}

	public Block getBlock(IBlockAccess world) {
		return world.getBlock(getX(), getY(), getZ());
	}

	public boolean blockExists(World world) {
		return world.blockExists(getX(), getY(), getZ());
	}

	public double distanceTo(LPPosition targetPos) {
		return Math.sqrt(Math.pow(targetPos.getXD() - this.getXD(), 2) + Math.pow(targetPos.getYD() - this.getYD(), 2) + Math.pow(targetPos.getZD() - this.getZD(), 2));
	}

	public LPPosition center() {
		this.value1 += 0.5D;
		this.value2 += 0.5D;
		this.value3 += 0.5D;
		return this;
	}

	public void writeToNBT(String prefix, NBTTagCompound nbt) {
		nbt.setDouble(prefix + "xPos", value1);
		nbt.setDouble(prefix + "yPos", value2);
		nbt.setDouble(prefix + "zPos", value3);
	}
	
	public static LPPosition readFromNBT(String prefix, NBTTagCompound nbt) {
		if(nbt.hasKey(prefix + "xPos") && nbt.hasKey(prefix + "yPos") && nbt.hasKey(prefix + "zPos")) {
			return new LPPosition(nbt.getDouble(prefix + "xPos"), nbt.getDouble(prefix + "yPos"), nbt.getDouble(prefix + "zPos"));
		}
		return null;
	}

	public LPPosition add(LPPosition toAdd) {
		this.value1 += toAdd.value1;
		this.value2 += toAdd.value2;
		this.value3 += toAdd.value3;
		return this;
	}

	public void setBlockToAir(World world) {
		world.setBlockToAir(getX(), getY(), getZ());
	}

	public void rotateLeft() {
		double tmp = this.value3;
		this.value3 = -this.value1;
		this.value1 = tmp;
	}
	
	public void rotateRight() {
		double tmp = this.value1;
		this.value1 = -this.value3;
		this.value3 = tmp;
	}
}
