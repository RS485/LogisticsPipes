package logisticspipes.routing;

import java.util.EnumSet;
import java.util.Objects;
import javax.annotation.Nonnull;

import net.minecraft.util.EnumFacing;

import network.rs485.logisticspipes.util.LPDataInput;
import network.rs485.logisticspipes.util.LPDataOutput;

public class LaserData {

	private int posX;
	private int posY;
	private int posZ;
	private boolean finalPipe = true;
	private boolean startPipe = false;
	private int length = 1;

	@Nonnull
	private EnumFacing dir;
	@Nonnull
	private EnumSet<PipeRoutingConnectionType> connectionType;

	public LaserData(int posX, int posY, int posZ, @Nonnull EnumFacing dir, @Nonnull EnumSet<PipeRoutingConnectionType> connectionType) {
		this.posX = posX;
		this.posY = posY;
		this.posZ = posZ;
		this.dir = dir;
		this.connectionType = connectionType;
	}

	public LaserData(LPDataInput input) {
		posX = input.readInt();
		posY = input.readInt();
		posZ = input.readInt();
		dir = Objects.requireNonNull(input.readFacing());
		finalPipe = input.readBoolean();
		startPipe = input.readBoolean();
		length = input.readInt();
		connectionType = EnumSet.noneOf(PipeRoutingConnectionType.class);
		for (PipeRoutingConnectionType type : PipeRoutingConnectionType.values()) {
			if (input.readBoolean()) {
				connectionType.add(type);
			}
		}
	}

	public void writeData(LPDataOutput output) {
		output.writeInt(posX);
		output.writeInt(posY);
		output.writeInt(posZ);
		output.writeFacing(dir);
		output.writeBoolean(finalPipe);
		output.writeBoolean(startPipe);
		output.writeInt(length);
		for (PipeRoutingConnectionType type : PipeRoutingConnectionType.values()) {
			output.writeBoolean(connectionType.contains(type));
		}
	}

	public int getPosX() {
		return this.posX;
	}

	public LaserData setPosX(int posX) {
		this.posX = posX;
		return this;
	}

	public int getPosY() {
		return this.posY;
	}

	public LaserData setPosY(int posY) {
		this.posY = posY;
		return this;
	}

	public int getPosZ() {
		return this.posZ;
	}

	public LaserData setPosZ(int posZ) {
		this.posZ = posZ;
		return this;
	}

	@Nonnull
	public EnumFacing getDir() {
		return this.dir;
	}

	public LaserData setDir(EnumFacing dir) {
		this.dir = dir;
		return this;
	}

	@Nonnull
	public EnumSet<PipeRoutingConnectionType> getConnectionType() {
		return this.connectionType;
	}

	public LaserData setConnectionType(@Nonnull EnumSet<PipeRoutingConnectionType> connectionType) {
		this.connectionType = connectionType;
		return this;
	}

	public boolean isFinalPipe() {
		return this.finalPipe;
	}

	public LaserData setFinalPipe(boolean finalPipe) {
		this.finalPipe = finalPipe;
		return this;
	}

	public boolean isStartPipe() {
		return this.startPipe;
	}

	public LaserData setStartPipe(boolean startPipe) {
		this.startPipe = startPipe;
		return this;
	}

	public int getLength() {
		return this.length;
	}

	public LaserData setLength(int length) {
		this.length = length;
		return this;
	}

	public boolean equals(final Object o) {
		if (o == this) return true;
		if (!(o instanceof LaserData)) return false;
		final LaserData other = (LaserData) o;
		if (this.getPosX() != other.getPosX()) return false;
		if (this.getPosY() != other.getPosY()) return false;
		if (this.getPosZ() != other.getPosZ()) return false;
		if (!Objects.equals(this.getDir(), other.getDir())) return false;
		if (!Objects.equals(this.getConnectionType(), other.getConnectionType())) return false;
		if (this.isFinalPipe() != other.isFinalPipe()) return false;
		if (this.isStartPipe() != other.isStartPipe()) return false;
		return this.getLength() == other.getLength();
	}

	public int hashCode() {
		final int PRIME = 59;
		int result = 1;
		result = result * PRIME + this.getPosX();
		result = result * PRIME + this.getPosY();
		result = result * PRIME + this.getPosZ();
		result = result * PRIME + this.getDir().hashCode();
		result = result * PRIME + this.getConnectionType().hashCode();
		result = result * PRIME + (this.isFinalPipe() ? 79 : 97);
		result = result * PRIME + (this.isStartPipe() ? 79 : 97);
		result = result * PRIME + this.getLength();
		return result;
	}

	public String toString() {
		return "LaserData(posX=" + this.getPosX() + ", posY=" + this.getPosY() + ", posZ=" + this.getPosZ() + ", dir=" + this.getDir() + ", connectionType=" + this.getConnectionType() + ", finalPipe=" + this.isFinalPipe() + ", startPipe=" + this.isStartPipe() + ", length=" + this.getLength() + ")";
	}
}
