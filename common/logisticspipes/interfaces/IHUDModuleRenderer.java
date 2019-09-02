package logisticspipes.interfaces;

import java.util.List;

public interface IHUDModuleRenderer {

	void renderContent(boolean shifted);

	List<IHUDButton> getButtons();
}
