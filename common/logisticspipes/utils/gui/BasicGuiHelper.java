package logisticspipes.utils.gui;

import logisticspipes.utils.gui.LogisticsBaseGuiScreen.Colors;

public class BasicGuiHelper {

	public static int ConvertEnumToColor(Colors color){
		switch(color){
			case Black:
				return 0xFF000000;
			case White:
				return 0xFFFFFFFF;
			case DarkGrey:
				return 0xFF555555;
			case MiddleGrey:
				return 0xFF8b8b8b;
			case LightGrey:
				return 0xFFC6C6C6;
			case Red:
				return 0xFFFF0000;
			
			default: 
				return 0;
			}
	}

}
