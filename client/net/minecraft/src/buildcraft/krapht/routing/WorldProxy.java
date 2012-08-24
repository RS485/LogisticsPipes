package net.minecraft.src.buildcraft.krapht.routing;

import net.minecraft.src.World;
import net.minecraft.src.ModLoader;

public class WorldProxy {
	public static World getMainWorld() {
		return ModLoader.getMinecraftInstance().theWorld;
	}
}
