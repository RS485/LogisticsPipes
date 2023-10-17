package logisticspipes.proxy;

public class VersionNotSupportedException extends RuntimeException {

	private static final long serialVersionUID = 3229611374730119210L;

	public VersionNotSupportedException(String modName, String haveVersion, String targetVersion, String condition) {
		super("The " + modName + " Version '" + haveVersion + "' is not supported by this LP version" + condition + ". Please use '" + targetVersion + "'");
	}
}
