package logisticspipes.security;

public class PermissionException extends Exception {
	private static final long serialVersionUID = 7761142652210614117L;

	public PermissionException() {
		super("Permission denied");
	}
}
