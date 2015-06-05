package logisticspipes.utils;

public interface IHavePriority {

	// a simple interface, implemented by things which have an absolute priority, on a scale other than distance or workload.
	int getPriority();
}
