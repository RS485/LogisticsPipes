package net.minecraft.src.ic2.api;

import net.minecraft.src.Item;

public abstract interface IElectricItem {

	public abstract int getMaxCharge();

	public abstract int getChargedItemId();

	public abstract int getEmptyItemId();
	
}
