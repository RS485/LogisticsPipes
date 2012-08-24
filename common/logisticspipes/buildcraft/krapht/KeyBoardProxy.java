package logisticspipes.buildcraft.krapht;

import org.lwjgl.input.Keyboard;

public class KeyBoardProxy {
	public static boolean isShiftDown() {
		return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
	}
	
}
