package logisticspipes.utils.gui;

public interface ISubGuiControler {

	public void setSubGui(SubGuiScreen gui);

	public void resetSubGui();

	public boolean hasSubGui();

	public SubGuiScreen getSubGui();

	public LogisticsBaseGuiScreen getBaseScreen();

}
