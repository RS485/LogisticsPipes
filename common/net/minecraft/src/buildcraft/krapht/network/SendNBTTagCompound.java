package net.minecraft.src.buildcraft.krapht.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.src.CompressedStreamTools;
import net.minecraft.src.NBTTagCompound;

public class SendNBTTagCompound {
	public static void writeNBTTagCompound(NBTTagCompound tag, DataOutputStream data) throws IOException {
		if (tag == null)
        {
            data.writeShort(-1);
        }
        else
        {
            byte[] var3 = CompressedStreamTools.compress(tag);
            data.writeShort((short)var3.length);
            data.write(var3);
        }
	}
	
	public static NBTTagCompound readNBTTagCompound(DataInputStream data) throws IOException {
		short legth = data.readShort();
        if (legth < 0)
        {
            return null;
        }
        else
        {
            byte[] array = new byte[legth];
            data.readFully(array);
            return CompressedStreamTools.decompress(array);
        }

	}
}
