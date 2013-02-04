package logisticspipes.security;

import logisticspipes.interfaces.routing.ISaveState;
import net.minecraft.nbt.NBTTagCompound;

public class SecuritySettings implements ISaveState{
	
	public String name;
	
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
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setString("name", name);
	}
}
