package logisticspipes.pipes.basic.liquid;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.liquids.LiquidStack;
import net.minecraftforge.liquids.LiquidTank;

public class LogisticsLiquidSection extends LiquidTank {

	public LogisticsLiquidSection(int capacity) {
		super(capacity);
	}

	public LogisticsLiquidSection readFromNBT(NBTTagCompound compoundTag) {
		setLiquid(LiquidStack.loadLiquidStackFromNBT(compoundTag));
		return this;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound subTag) {
		if (this.getLiquid() != null) {
			return this.getLiquid().writeToNBT(subTag);
		}
		return null;
	}
}
