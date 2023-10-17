package logisticspipes.pipes.upgrades.power;

public class IC2MVPowerSupplierUpgrade extends IC2PowerSupplierUpgrade {

	public static String getName() {
		return "power_supplier_eu_mv";
	}

	@Override
	public int getPowerLevel() {
		return 128;
	}
}
