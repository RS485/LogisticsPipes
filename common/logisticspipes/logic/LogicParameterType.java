package logisticspipes.logic;

import net.minecraftforge.common.util.ForgeDirection;

import lombok.Getter;

public enum LogicParameterType {
	Number(long.class),
	Float(double.class),
	Boolean(boolean.class),
	Direction(ForgeDirection.class);

	@Getter
	private final Class<?> javaClass;

	private LogicParameterType(Class<?> clazz) {
		javaClass = clazz;
	}
}
