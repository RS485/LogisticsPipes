package logisticspipes.security;

import logisticspipes.interfaces.routing.ISaveState;
import net.minecraft.nbt.NBTTagCompound;

public class SecuritySettings implements ISaveState{
	
	public String name;
	public boolean openGui = false;
	public boolean openRequest = false;
	public boolean openUpgrades = false;
	public boolean openNetworkMonitor = false;
	public boolean removePipes = false;
	
	public SecuritySettings(String name) {
		this.name = name;
	}
	
	
	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		String prev = name;
		name = nbttagcompound.getString("name");
		if(name.equals("")) {
			name = prev;
		}
		openGui = nbttagcompound.getBoolean("openGui");
		openRequest = nbttagcompound.getBoolean("openRequest");
		openUpgrades = nbttagcompound.getBoolean("openUpgrades");
		openNetworkMonitor = nbttagcompound.getBoolean("openNetworkMonitor");
		removePipes = nbttagcompound.getBoolean("removePipes");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		if (name == "" || name == null) return;
		nbttagcompound.setString("name", name);
		nbttagcompound.setBoolean("openGui", openGui);
		nbttagcompound.setBoolean("openRequest", openRequest);
		nbttagcompound.setBoolean("openUpgrades", openUpgrades);
		nbttagcompound.setBoolean("openNetworkMonitor", openNetworkMonitor);
		nbttagcompound.setBoolean("removePipes", removePipes);
	}
}
