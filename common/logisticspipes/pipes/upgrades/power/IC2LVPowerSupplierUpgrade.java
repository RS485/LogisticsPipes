package logisticspipes.pipes.upgrades.power;

public class IC2LVPowerSupplierUpgrade extends IC2PowerSupplierUpgrade {

	public static String getName() {
		return "power_supplier_eu_lv";
	}

	@Override
	public int getPowerLevel() {
		return 32;
	}
}
