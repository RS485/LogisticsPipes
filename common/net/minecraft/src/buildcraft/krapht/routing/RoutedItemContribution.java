package net.minecraft.src.buildcraft.krapht.routing;

import java.util.UUID;

import net.minecraft.src.NBTTagCompound;

public class RoutedItemContribution implements net.minecraft.src.buildcraft.api.IPassiveItemContribution{
	
	public UUID sourceUUID;
	public UUID destinationUUID;
	private boolean isDefaultRouted;
	public boolean hasArrived;


	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		sourceUUID = UUID.fromString(nbttagcompound.getString("sourceUUID"));
		destinationUUID = UUID.fromString(nbttagcompound.getString("destinationUUID"));
		isDefaultRouted = nbttagcompound.getBoolean("isDefaultRouted");
		hasArrived = nbttagcompound.getBoolean("hasArrived");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setString("sourceUUID", sourceUUID.toString());
		nbttagcompound.setString("destinationUUID", destinationUUID.toString());
		nbttagcompound.setBoolean("isDefaultRouted", isDefaultRouted);
		nbttagcompound.setBoolean("hasArrived", hasArrived);
		
	}

}
