package logisticspipes.proxy.computers.interfaces;

public interface ILPCCTypeHolder {

	/**
	 * Returns the type holder for the CC/OC interoperability.
	 *
	 * @return an object array with at least one element.
	 */
	Object[] getTypeHolder();

	default void setCCType(Object type) {
		getTypeHolder()[0] = type;
	}

	default Object getCCType() {
		return getTypeHolder()[0];
	}
}
