package logisticspipes.interfaces;

public interface IHUDButton {
	public int getX();
	public int getY();
	public int sizeX();
	public int sizeY();
	public void setFocused();
	public boolean isFocused();
	public void clearFocused();
	public void blockFocused();
	public boolean isblockFocused();
	public int focusedTime();
	public void clicked();
	public void renderButton(boolean hover, boolean clicked);
	public boolean shouldRenderButton();
	public boolean buttonEnabled();
}
