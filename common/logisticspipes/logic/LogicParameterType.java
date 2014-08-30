package logisticspipes.logic;

import lombok.Getter;
import net.minecraftforge.common.util.ForgeDirection;

public enum LogicParameterType {
	Number(long.class),
	Float(double.class),
	Boolean(boolean.class),
	Direction(ForgeDirection.class);
	@Getter
	private final Class<?> javaClass;
	private LogicParameterType(Class<?> clazz) {
		this.javaClass = clazz;
	}
}
