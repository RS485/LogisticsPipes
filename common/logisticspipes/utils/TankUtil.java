package logisticspipes.utils;

import java.util.function.Consumer;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import logisticspipes.interfaces.ITankUtil;

public class TankUtil implements ITankUtil {

	private IFluidHandler fluidhandler;

	public TankUtil(IFluidHandler fluidhandler) {
		this.fluidhandler = fluidhandler;
	}

	@Override
	public boolean containsTanks() {
		IFluidTankProperties[] tanks = fluidhandler.getTankProperties();
		return tanks != null && tanks.length > 0;
	}

	@Override
	public int fill(FluidIdentifierStack stack, boolean doFill) {
		return fluidhandler.fill(stack.makeFluidStack(), doFill);
	}

	@Override
	public FluidIdentifierStack drain(FluidIdentifierStack stack, boolean doDrain) {
		return FluidIdentifierStack.getFromStack(fluidhandler.drain(stack.makeFluidStack(), doDrain));
	}

	@Override
	public FluidIdentifierStack drain(int amount, boolean doDrain) {
		return FluidIdentifierStack.getFromStack(fluidhandler.drain(amount, doDrain));
	}

	@Override
	public void forEachFluid(Consumer<FluidIdentifierStack> fluidStackConsumer) {
		IFluidTankProperties[] tanks = fluidhandler.getTankProperties();
		if (tanks != null) {
			for (IFluidTankProperties tank : tanks) {
				if (tank.getContents() != null) {
					fluidStackConsumer.accept(FluidIdentifierStack.getFromStack(tank.getContents()));
				}
			}
		}
	}

	@Override
	public boolean canDrain(FluidIdentifier fluid) {
		IFluidTankProperties[] tanks = fluidhandler.getTankProperties();
		if (tanks != null) {
			for (IFluidTankProperties tank : tanks) {
				if (tank.canDrainFluidType(fluid.makeFluidStack(1))) {
					return true;
				}
			}
		}
		return false;
	}

	public int getFreeSpaceInsideTank(FluidIdentifier type) {
		int free = 0;
		IFluidTankProperties[] tanks = fluidhandler.getTankProperties();
		if (tanks != null && tanks.length > 0) {
			for (IFluidTankProperties tank : tanks) {
				FluidStack content = tank.getContents();
				if (content == null || FluidIdentifier.get(content) == type) {
					free += getFreeSpaceInsideTank(tank);
				}
			}
		}
		return free;
	}

	private int getFreeSpaceInsideTank(IFluidTankProperties tanks) {
		if (tanks == null) {
			return 0;
		}
		FluidStack liquid = tanks.getContents();
		if (liquid == null || liquid.getFluid() == null) {
			return tanks.getCapacity();
		}
		return tanks.getCapacity() - liquid.amount;
	}
}
