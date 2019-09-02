package logisticspipes.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;

/**
 * Created by David on 12.05.2017.
 * All rights reserved.
 */
public class StreamHelper {

	public static <T> Collector<T, List<T>, T> singletonCollector() {
		return Collector.of(
				ArrayList::new,
				List::add,
				(left, right) -> {
					left.addAll(right);
					return left;
				},
				list -> {
					if (list.size() != 1) {
						throw new IllegalStateException();
					}
					return list.get(0);
				}
		);
	}
}
