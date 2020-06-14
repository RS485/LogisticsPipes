package logisticspipes.pipes.upgrades.power;

public class IC2HVPowerSupplierUpgrade extends IC2PowerSupplierUpgrade {

	public static String getName() {
		return "power_supplier_eu_hv";
	}

	@Override
	public int getPowerLevel() {
		return 512;
	}
}
