package network.rs485.debuggui.api;

public interface IObjectIdentification {

	boolean toStringObject(Object o);

	/**
	 *
	 * @param o
	 * @return null, if object isn't handled, otherwise the String value
	 */
	String handleObject(Object o);

}
