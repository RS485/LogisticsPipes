package logisticspipes.pipes;

import net.minecraft.item.Items;

public class PipeLogisticsChassiMk5 extends PipeLogisticsChassi {

	public PipeLogisticsChassiMk5() {
		super(Items.AIR);
	}

	@Override
	public int getChassiSize() {
		return 8;
	}

}
