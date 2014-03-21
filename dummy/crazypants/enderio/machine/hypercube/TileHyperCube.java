package crazypants.enderio.machine.hypercube;

import net.minecraft.tileentity.TileEntity;

public class TileHyperCube extends TileEntity {
	public static enum IoMode {
		SEND("gui.send"), RECIEVE("gui.recieve"), BOTH("gui.sendRecieve"), NEITHER("gui.disabled");
		private IoMode(String unlocalisedName) {}
	}
	public static enum SubChannel {
		POWER, FLUID, ITEM;
		private SubChannel() {}
	}
	public Channel getChannel() {
		return null;
	}
	public IoMode getModeForChannel(SubChannel channel) {
		return null;
	}
}
