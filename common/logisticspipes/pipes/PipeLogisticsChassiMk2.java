package logisticspipes.pipes;

import net.minecraft.item.Items;

public class PipeLogisticsChassiMk2 extends PipeLogisticsChassi {

	public PipeLogisticsChassiMk2() {
		super(Items.AIR);
	}

	@Override
	public int getChassiSize() {
		return 2;
	}

}
