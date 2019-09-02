package logisticspipes.interfaces;

public interface IHUDButton {

	int getX();

	int getY();

	int sizeX();

	int sizeY();

	void setFocused();

	boolean isFocused();

	void clearFocused();

	void blockFocused();

	boolean isblockFocused();

	int focusedTime();

	void clicked();

	void renderButton(boolean hover, boolean clicked, boolean shifted);

	void renderAlways(boolean shifted);

	boolean shouldRenderButton();

	boolean buttonEnabled();
}
