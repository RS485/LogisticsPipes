package logisticspipes.pipes;

import net.minecraft.item.Items;

public class PipeLogisticsChassiMk3 extends PipeLogisticsChassi {

	public PipeLogisticsChassiMk3() {
		super(Items.AIR);
	}

	@Override
	public int getChassiSize() {
		return 3;
	}

}
