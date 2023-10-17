package logisticspipes.utils.gui;

public interface ISubGuiControler {

	void setSubGui(SubGuiScreen gui);

	void resetSubGui();

	boolean hasSubGui();

	SubGuiScreen getSubGui();

	LogisticsBaseGuiScreen getBaseScreen();

}
