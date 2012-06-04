package net.minecraft.src.buildcraft.krapht.container;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.Slot;
import net.minecraft.src.buildcraft.core.BuildCraftContainer;

public class CraftingPipeContainer extends BuildCraftContainer {
	

	IInventory pipeinv;

	public CraftingPipeContainer(IInventory playerInventory, IInventory inventory) {
		super(inventory);
		pipeinv = inventory;
		
		//Source Items
		for(int i1 = 0; i1 < 9; i1++)
			addSlot(new Slot(playerInventory, i1, 18 + i1 * 18, 18));
		//Target Block
		addSlot(new Slot(playerInventory, 9, 90, 64));
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return pipeinv.isUseableByPlayer(entityplayer);
	}

}
