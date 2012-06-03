package net.minecraft.src.buildcraft.krapht.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.src.buildcraft.core.network.BuildCraftPacket;

public abstract class LogisticsPipesPacket extends BuildCraftPacket {
	
	public LogisticsPipesPacket() {
		channel = NetworkConstants.LOGISTICS_PIPES_CHANNEL_NAME;
	}
}
