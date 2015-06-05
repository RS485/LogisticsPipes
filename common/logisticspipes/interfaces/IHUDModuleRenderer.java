package logisticspipes.interfaces;

import java.util.List;

public interface IHUDModuleRenderer {

	public void renderContent(boolean shifted);

	public List<IHUDButton> getButtons();
}
