package logisticspipes.logic;



import lombok.Getter;
import net.minecraft.util.EnumFacing;

public enum LogicParameterType {
	Number(long.class),
	Float(double.class),
	Boolean(boolean.class),
	Direction(EnumFacing.class);

	@Getter
	private final Class<?> javaClass;

	private LogicParameterType(Class<?> clazz) {
		javaClass = clazz;
	}
}
