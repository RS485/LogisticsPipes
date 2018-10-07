package logisticspipes.utils;

import logisticspipes.LogisticsPipes;
import net.minecraftforge.fml.common.discovery.ASMDataTable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

public class StaticResolverUtil {

	private static Set<ASMDataTable.ASMData> data = new HashSet<>();
	private static Map<Class<?>, Set<Class<?>>> classes = new HashMap<>();

	public static void useASMDataTable(@Nullable ASMDataTable table) {
		data.clear();
		classes.clear();
		if (table == null) return;
		data.addAll(table.getAll(StaticResolve.class.getCanonicalName()));
	}

	@Nonnull
	public static <T> Set<Class<? extends T>> findClassesByType(@Nonnull Class<T> cls) {
		if (data.isEmpty()) return Collections.emptySet();

		Set<Class<?>> classes = StaticResolverUtil.classes.computeIfAbsent(cls, c ->
				data.parallelStream()
						.map(d -> loadClass(d.getClassName()))
						.filter(Objects::nonNull)
						.filter(cls::isAssignableFrom)
						.collect(Collectors.toSet())
		);
		return (Set<Class<? extends T>>) (Set<?>) classes;
	}

	@Nullable
	private static Class<?> loadClass(@Nonnull String classPathSpec) {
		try {
			return LogisticsPipes.class.getClassLoader().loadClass(classPathSpec);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

}
