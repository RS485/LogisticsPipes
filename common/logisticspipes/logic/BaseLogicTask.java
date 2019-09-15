package logisticspipes.logic;

import java.util.UUID;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;

import lombok.Getter;

public abstract class BaseLogicTask {

	// Graphical Interface
	@Getter
	protected int posX;
	@Getter
	protected int posY;
	@Getter
	protected String name = getTypeName();
	@Getter
	protected String comment = "";

	// Saving and Server/Client sync
	@Getter
	protected UUID uuid;

	public BaseLogicTask(CompoundTag nbt) {
		posX = nbt.getInteger("posX");
		posY = nbt.getInteger("posY");
		name = nbt.getString("name");
		comment = nbt.getString("comment");
		uuid = UUID.fromString(nbt.getString("uuid"));
	}

	public BaseLogicTask(int posX, int posY) {
		this.posX = posX;
		this.posY = posY;
		uuid = UUID.randomUUID();
	}

	public final CompoundTag getNBTTagCompound() {
		CompoundTag nbt = new CompoundTag();
		addToNBT(nbt);
		return nbt;
	}

	protected void addToNBT(CompoundTag nbt) {
		nbt.setInteger("posX", posX);
		nbt.setInteger("posY", posY);
		nbt.setString("name", name);
		nbt.setString("comment", comment);
		nbt.setString("uuid", uuid.toString());
	}

	public abstract int getAmountOfInput();

	public abstract int getAmountOfOutput();

	public abstract LogicParameterType getInputParameterType(int i);

	public abstract LogicParameterType getOutputParameterType(int i);

	public abstract void setInputParameter(int i, Object value);

	public abstract boolean isCalculated();

	public abstract Object getResult(int i);

	public abstract void resetState();

	public abstract String getTypeName();

	public abstract void syncTick(BlockEntity tile);
}
