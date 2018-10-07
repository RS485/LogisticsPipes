package logisticspipes.utils.debug;

import java.time.Duration;
import java.util.Arrays;
import java.util.OptionalLong;

public class PerformanceMeter {

	private final int meanCalcCallCount;
	private final boolean printToConsole;
	private final long[] meanCalcGroup;
	private int currentGroupIndex;

	public PerformanceMeter(int meanCalcCallCount, boolean printToConsole) {
		this.meanCalcCallCount = meanCalcCallCount;
		this.printToConsole = printToConsole;
		this.meanCalcGroup = new long[meanCalcCallCount];
		this.currentGroupIndex = 0;
	}

	private void increaseGroupIndex() {
		currentGroupIndex++;
		if (currentGroupIndex >= meanCalcCallCount) {
			currentGroupIndex = 0;

			if (printToConsole) {
				Duration sum = getSum();
				Duration mean = sum.dividedBy(meanCalcCallCount);
				System.out.printf("Sum: %s of %d measures - Mean time: %s%n",
						sum.toString(), meanCalcCallCount, mean.toString());
			}
		}
	}

	public Duration getSum() {
		OptionalLong durationSum = Arrays.stream(meanCalcGroup).reduce(Long::sum);
		if (durationSum.isPresent()) {
			return Duration.ofNanos(durationSum.getAsLong());
		} else {
			return Duration.ZERO;
		}
	}

	public Duration getCalculatedMean() {
		return getSum().dividedBy(meanCalcCallCount);
	}

	public void newPerfValue(long nanoDuration) {
		meanCalcGroup[currentGroupIndex] = nanoDuration;
		increaseGroupIndex();
	}

	public void newPerfValue(Duration duration) {
		newPerfValue(duration.toNanos());
	}
}
