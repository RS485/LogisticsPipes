package logisticspipes.pipes;

import net.minecraft.item.Items;

public class PipeLogisticsChassiMk1 extends PipeLogisticsChassi {

	public PipeLogisticsChassiMk1() {
		super(Items.AIR);
	}

	@Override
	public int getChassiSize() {
		return 1;
	}

}
