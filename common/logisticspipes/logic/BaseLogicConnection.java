package logisticspipes.logic;

import lombok.Getter;
import lombok.Setter;

public abstract class BaseLogicConnection {

	@Getter
	private final BaseLogicTask source;
	@Getter
	private final int sourceIndex;
	@Getter
	private final BaseLogicTask target;
	@Getter
	private final int targetIndex;
	@Getter
	private final LogicParameterType type;

	@Getter
	@Setter
	private boolean isInvalidConnection;

	public BaseLogicConnection(BaseLogicTask source, int sourceIndex, BaseLogicTask target, int targetIndex, LogicParameterType type) {
		this.source = source;
		this.sourceIndex = sourceIndex;
		this.target = target;
		this.targetIndex = targetIndex;
		this.type = type;
	}

}
