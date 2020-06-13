package logisticspipes.proxy.computers.interfaces;

public interface ILPCCTypeHolder {
	Object[] ccType = new Object[1];

	default void setCCType(Object type) {
		ccType[0] = type;
	}

	default Object getCCType() {
		return ccType[0];
	}
}
