package logisticspipes.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Supplier;

import logisticspipes.LogisticsPipes;
import logisticspipes.utils.tuples.Pair;

public class StackTraceUtil {

	private static final Map<Thread, LinkedList<Pair<StackTraceElement, String>>> informationMap = new HashMap<>();

	public static abstract class Info {

		public abstract void end();
	}

	private static class DummyInfo extends Info {

		@Override
		public void end() {}
	}

	private static LinkedList<Pair<StackTraceElement, String>> getList() {
		return StackTraceUtil.informationMap.computeIfAbsent(Thread.currentThread(), k -> new LinkedList<>());
	}

	public static Info addTraceInformation(final Supplier<String> informationSupplier, Info... infos) {
		if (!LogisticsPipes.isDEBUG()) {
			return new DummyInfo();
		}
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		final StackTraceElement calledFrom = trace[2];
		return StackTraceUtil.addTraceInformationFor(calledFrom, informationSupplier.get(), infos);
	}

	public static Info addSuperTraceInformation(final Supplier<String> informationSupplier, Info... infos) {
		if (!LogisticsPipes.isDEBUG()) {
			return new DummyInfo();
		}
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		final StackTraceElement calledFrom = trace[3];
		return StackTraceUtil.addTraceInformationFor(calledFrom, informationSupplier.get(), infos);
	}

	private static Info addTraceInformationFor(final StackTraceElement calledFrom, final String information, final Info... infos) {
		synchronized (StackTraceUtil.informationMap) {
			StackTraceUtil.getList().addLast(new Pair<>(calledFrom, information));
			return new Info() {

				@Override
				public void end() {
					synchronized (StackTraceUtil.informationMap) {
						if (StackTraceUtil.getList().isEmpty()) {
							throw new RuntimeException("There are to many end() calls");
						} else {
							Pair<StackTraceElement, String> pair = StackTraceUtil.getList().getLast();
							if (!pair.getValue1().equals(calledFrom)) {
								System.out.println("Found: " + pair.getValue1());
								System.out.println("Looking for: " + calledFrom);
								throw new RuntimeException("There is an end() call missing");
							}
							StackTraceUtil.getList().removeLast();
						}
					}
					if (infos != null) {
						for (Info info : infos) {
							info.end();
						}
					}
				}
			};
		}
	}

	public static void printTrace() {
		synchronized (System.err) {
			synchronized (StackTraceUtil.informationMap) {
				// Print our stack trace
				@SuppressWarnings("unchecked")
				LinkedList<StackTraceElement> traceList = new LinkedList<>(Arrays.asList(Thread.currentThread().getStackTrace()));
				traceList.removeFirst();
				traceList.removeFirst();
				LinkedList<Pair<StackTraceElement, String>> paired = new LinkedList<>();
				Pair<StackTraceElement, String> lastFound = null;
				StackTraceElement current = traceList.removeLast();
				while (current != null) {
					Iterator<Pair<StackTraceElement, String>> iter = StackTraceUtil.getList().iterator();
					if (lastFound != null) {
						while (iter.hasNext()) {
							Pair<StackTraceElement, String> pair = iter.next();
							if (pair == lastFound) {
								break;
							}
						}
					}
					String result = null;
					while (iter.hasNext()) {
						Pair<StackTraceElement, String> pair = iter.next();
						if (StackTraceUtil.compare(current, pair.getValue1())) {
							lastFound = pair;
							result = pair.getValue2();
						}
					}
					paired.addFirst(new Pair<>(current, result));
					if (traceList.isEmpty()) {
						current = null;
					} else {
						current = traceList.removeLast();
					}
				}
				System.err.print("StackTrace");
				System.err.print(System.lineSeparator());
				for (Pair<StackTraceElement, String> pair : paired) {
					System.err.print("\tat " + pair.getValue1());
					if (pair.getValue2() != null) {
						System.err.print(" [" + pair.getValue2() + "]");
					}
					System.err.print(System.lineSeparator());
				}
				System.err.print("StackTrace end");
				System.err.print(System.lineSeparator());
			}
		}
	}

	private static boolean compare(StackTraceElement called, StackTraceElement infoProvider) {
		return called.getClassName().equals(infoProvider.getClassName()) && called.getMethodName().equals(infoProvider.getMethodName()) && called.getFileName().equals(infoProvider.getFileName()) && called.getLineNumber() >= infoProvider.getLineNumber();
	}
}
