package logisticspipes.pipes.upgrades.power;

public class IC2EVPowerSupplierUpgrade extends IC2PowerSupplierUpgrade {

	public static String getName() {
		return "power_supplier_eu_ev";
	}

	@Override
	public int getPowerLevel() {
		return 2048;
	}
}
