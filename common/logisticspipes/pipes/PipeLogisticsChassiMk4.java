package logisticspipes.pipes;

import net.minecraft.item.Items;

public class PipeLogisticsChassiMk4 extends PipeLogisticsChassi {

	public PipeLogisticsChassiMk4() {
		super(Items.AIR);
	}

	@Override
	public int getChassiSize() {
		return 4;
	}

}
