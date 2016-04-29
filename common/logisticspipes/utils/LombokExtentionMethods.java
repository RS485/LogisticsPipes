package logisticspipes.utils;

import java.util.Optional;
import java.util.stream.Stream;

public class LombokExtentionMethods {
	public static <T> T getFirstOrDefault(Stream<T> stream, T def) {
		Optional<T> first = stream.findFirst();
		if(first.isPresent()) {
			return first.get();
		}
		return def;
	}
}
