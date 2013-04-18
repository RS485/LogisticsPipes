package appeng.api;

import net.minecraft.tileentity.TileEntity;
import appeng.api.me.util.IMEInventory;

public interface IExternalStorageHandler {
	
	boolean canHandle( TileEntity te );
	IMEInventory getInventory( TileEntity te );
	
}
