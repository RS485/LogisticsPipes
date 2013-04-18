package appeng.api.me.tiles;

import net.minecraft.inventory.IInventory;

public interface ITileIOCable {
	
	public enum Version {
		Basic, Precision, Fuzzy
	};
	
	Version getVersion();
	
	IInventory getConfiguration();

	String getName();
	
}
