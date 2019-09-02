package network.rs485.logisticspipes.world;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import lombok.Getter;

import logisticspipes.network.abstractpackets.CoordinatesPacket;
import logisticspipes.pipes.basic.CoreUnroutedPipe;
import logisticspipes.routing.pathfinder.IPipeInformationProvider;

public class DoubleCoordinatesType<T> extends DoubleCoordinates {

	@Getter
	private T type;

	public DoubleCoordinatesType(double xCoord, double yCoord, double zCoord, T type) {
		super(xCoord, yCoord, zCoord);
		this.type = type;
	}

	public DoubleCoordinatesType(ICoordinates coords, T type) {
		super(coords);
		this.type = type;
	}

	public DoubleCoordinatesType(TileEntity tile, T type) {
		super(tile);
		this.type = type;
	}

	public DoubleCoordinatesType(CoreUnroutedPipe pipe, T type) {
		super(pipe);
		this.type = type;
	}

	public DoubleCoordinatesType(IPipeInformationProvider pipe, T type) {
		super(pipe);
		this.type = type;
	}

	public DoubleCoordinatesType(CoordinatesPacket packet, T type) {
		super(packet);
		this.type = type;
	}

	public DoubleCoordinatesType(Entity entity, T type) {
		super(entity);
		this.type = type;
	}

	public DoubleCoordinatesType(BlockPos pos, T type) {
		super(pos);
		this.type = type;
	}

	@Deprecated // Only called by reflection
	public DoubleCoordinatesType(double xCoord, double yCoord, double zCoord) {
		this(xCoord, yCoord, zCoord, null);
	}

	public DoubleCoordinatesType<T> add(DoubleCoordinates toAdd) {
		setXCoord(getXCoord() + toAdd.getXCoord());
		setYCoord(getYCoord() + toAdd.getYCoord());
		setZCoord(getZCoord() + toAdd.getZCoord());
		return this;
	}
}
