package appeng.api;

import net.minecraft.nbt.NBTBase;

/**
 * Don't cast this... either compare with it, or copy it.
 * 
 * Don't Implement.
 */
public interface IAETagCompound {
	
	NBTBase copy();

	@Override
	boolean equals( Object a ); // compare to other TagCompounds or IAETagCompounds
	
}
