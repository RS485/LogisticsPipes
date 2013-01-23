package logisticspipes.pipes.basic.liquid;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.liquids.LiquidTank;

public class LogisticsLiquidSection extends LiquidTank {

	public LogisticsLiquidSection(int capacity) {
		super(capacity);
	}

	public void readFromNBT(NBTTagCompound compoundTag) {
		setLiquid(LiquidStack.loadLiquidStackFromNBT(compoundTag));
	}

	public void writeToNBT(NBTTagCompound subTag) {
		if (this.getLiquid() != null) {
			this.getLiquid().writeToNBT(subTag);
		}
	}
}
