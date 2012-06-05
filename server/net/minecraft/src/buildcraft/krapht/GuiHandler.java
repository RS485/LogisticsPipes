package net.minecraft.src.buildcraft.krapht;

import net.minecraft.src.Container;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.buildcraft.transport.TileGenericPipe;
import net.minecraft.src.forge.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	@Override
	public Object getGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {

		if(!world.blockExists(x, y, z))
			return null;

		TileEntity tile = world.getBlockTileEntity(x, y, z);
		if(!(tile instanceof TileGenericPipe))
			return null;

		TileGenericPipe pipe = (TileGenericPipe)tile;
		
		switch(ID) {

		/*case GuiIDs.GUI_CRAFTINGPIPE_ID:
			return null;
		*/
		
		/*
		 * TODO Remove default and return null
		 * Open every GUI till we have there Container.
		 */
		default:
			return new Container() {
				@Override
				public boolean canInteractWith(EntityPlayer var1) {
					return true;
				}
				
			};
		}
	}
}
