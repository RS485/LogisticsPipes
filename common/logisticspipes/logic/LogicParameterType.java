package logisticspipes.logic;

import net.minecraft.util.math.Direction;

import lombok.Getter;

public enum LogicParameterType {
	Number(long.class),
	Float(double.class),
	Boolean(boolean.class),
	Direction(Direction.class);

	@Getter
	private final Class<?> javaClass;

	LogicParameterType(Class<?> clazz) {
		javaClass = clazz;
	}
}
