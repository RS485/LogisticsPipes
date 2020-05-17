package logisticspipes.security;

import javax.annotation.Nonnull;

import net.minecraft.nbt.NBTTagCompound;

import logisticspipes.interfaces.routing.ISaveState;

public class SecuritySettings implements ISaveState {

	public String name;
	public boolean openGui = false;
	public boolean openRequest = false;
	public boolean openUpgrades = false;
	public boolean openNetworkMonitor = false;
	public boolean removePipes = false;
	public boolean accessRoutingChannels = false;

	public SecuritySettings(String name) {
		this.name = name;
	}

	@Override
	public void readFromNBT(@Nonnull NBTTagCompound nbttagcompound) {
		String prev = name;
		name = nbttagcompound.getString("name");
		if (name.equals("")) {
			name = prev;
		}
		openGui = nbttagcompound.getBoolean("openGui");
		openRequest = nbttagcompound.getBoolean("openRequest");
		openUpgrades = nbttagcompound.getBoolean("openUpgrades");
		openNetworkMonitor = nbttagcompound.getBoolean("openNetworkMonitor");
		removePipes = nbttagcompound.getBoolean("removePipes");
		accessRoutingChannels = nbttagcompound.getBoolean("accessRoutingChannels");
	}

	@Override
	public void writeToNBT(@Nonnull NBTTagCompound nbttagcompound) {
		if (name == null || name.isEmpty()) {
			return;
		}
		nbttagcompound.setString("name", name);
		nbttagcompound.setBoolean("openGui", openGui);
		nbttagcompound.setBoolean("openRequest", openRequest);
		nbttagcompound.setBoolean("openUpgrades", openUpgrades);
		nbttagcompound.setBoolean("openNetworkMonitor", openNetworkMonitor);
		nbttagcompound.setBoolean("removePipes", removePipes);
		nbttagcompound.setBoolean("accessRoutingChannels", accessRoutingChannels);
	}
}
