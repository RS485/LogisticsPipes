/*
 * Copyright (c) 2016  RS485
 *
 * "LogisticsPipes" is distributed under the terms of the Minecraft Mod Public
 * License 1.0.1, or MMPL. Please check the contents of the license located in
 * https://github.com/RS485/LogisticsPipes/blob/dev/LICENSE.md
 *
 * This file can instead be distributed under the license terms of the
 * MIT license:
 *
 * Copyright (c) 2016  RS485
 *
 * This MIT license was reworded to only match this file. If you use the regular
 * MIT license in your project, replace this copyright notice (this line and any
 * lines below and NOT the copyright line above) with the lines from the original
 * MIT license located here: http://opensource.org/licenses/MIT
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this file and associated documentation files (the "Source Code"), to deal in
 * the Source Code without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Source Code, and to permit persons to whom the Source Code is furnished
 * to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Source Code, which also can be
 * distributed under the MIT.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package network.rs485.grow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class TickExecutor extends AbstractExecutorService implements ScheduledExecutorService {

	public static final long MILLISECONDS_PER_TICK = 50L;

	private final Thread tickingThread;

	private ConcurrentLinkedQueue<Runnable> taskQueue;

	private long currentTick;
	private long nextTask;
	private ReentrantLock schedulerLock;
	private LinkedList<TickScheduledTask<?>> scheduledTaskList;

	private CompletableFuture<Void> terminationFuture;
	private ReentrantReadWriteLock shutdownLock;
	private boolean shuttingDown;

	public TickExecutor(final @Nullable Thread tickingThread) {
		this.tickingThread = tickingThread;

		taskQueue = new ConcurrentLinkedQueue<>();

		currentTick = 0;
		nextTask = Long.MAX_VALUE;
		schedulerLock = new ReentrantLock();
		scheduledTaskList = new LinkedList<>();

		terminationFuture = new CompletableFuture<>();
		shutdownLock = new ReentrantReadWriteLock();
		shuttingDown = false;
	}

	public TickExecutor() {
		this(null);
	}

	private void checkShutDown() {
		if (shuttingDown || terminationFuture.isDone()) {
			throw new IllegalStateException("Executor is shut down");
		}
	}

	public <T> CompletableFuture<T> scheduleForCompletable(Supplier<T> supplier, long delay, TimeUnit unit) {
		final CompletableFuture<Void> scheduledFuture = new CompletableFuture<>();
		this.schedule(() -> scheduledFuture.complete(null), delay, unit);
		return scheduledFuture.thenApply((Void) -> supplier.get());
	}

	public <T> CompletableFuture<T> submitForCompletable(Supplier<T> supplier) {
		return CompletableFuture.supplyAsync(supplier, this);
	}

	public CompletableFuture<Void> submitForCompletable(Runnable runnable) {
		return CompletableFuture.runAsync(runnable, this);
	}

	private <V> void scheduleUnsafe(TickScheduledTask<V> task) {
		final int taskTick = task.getExecutorScheduledTick();
		boolean added = false;
		for (int i = 0; i < scheduledTaskList.size(); i++) {
			TickScheduledTask other = scheduledTaskList.get(i);
			if (taskTick < other.getExecutorScheduledTick()) {
				scheduledTaskList.add(i, task);
				added = true;
				break;
			}
		}
		if (taskTick < nextTask) {
			nextTask = taskTick;
		}
		if (!added) {
			scheduledTaskList.addLast(task);
		}
	}

	private <V> TickScheduledTask<V> schedule(TickScheduledTask<V> task) {
		schedulerLock.lock();
		try {
			scheduleUnsafe(task);
		} finally {
			schedulerLock.unlock();
		}
		return task;
	}

	@Override
	@Nonnull
	public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
		shutdownLock.readLock().lock();
		try {
			checkShutDown();
			int ticks = (int) (unit.toMillis(delay) / MILLISECONDS_PER_TICK);
			return schedule(new TickScheduledTask<>(Executors.callable(command), ticks));
		} finally {
			shutdownLock.readLock().unlock();
		}
	}

	@Override
	@Nonnull
	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
		shutdownLock.readLock().lock();
		try {
			checkShutDown();
			int ticks = (int) (unit.toMillis(delay) / MILLISECONDS_PER_TICK);
			return schedule(new TickScheduledTask<>(callable, ticks));
		} finally {
			shutdownLock.readLock().unlock();
		}
	}

	/**
	 * Does the same as scheduleWithFixedDelay.
	 */
	@Override
	@Nonnull
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
		return scheduleWithFixedDelay(command, initialDelay, period, unit);
	}

	@Override
	@Nonnull
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
		shutdownLock.readLock().lock();
		try {
			checkShutDown();
			int initialTicks = (int) (unit.toMillis(initialDelay) / MILLISECONDS_PER_TICK);
			int ticks = (int) (unit.toMillis(delay) / MILLISECONDS_PER_TICK);
			return schedule(new TickScheduledTask<>(Executors.callable(command), initialTicks, ticks));
		} finally {
			shutdownLock.readLock().unlock();
		}
	}

	@Override
	public void shutdown() {
		shuttingDown = true;
	}

	private void shutdownCleanup() {
		taskQueue = null;
		scheduledTaskList.forEach(tickScheduledTask -> tickScheduledTask.cancel(false));
		scheduledTaskList = null;
		terminationFuture.complete(null);
	}

	@Override
	@Nonnull
	public List<Runnable> shutdownNow() {
		shutdownLock.writeLock().lock();
		List<Runnable> copy;
		try {
			shuttingDown = true;

			copy = new ArrayList<>(taskQueue);

			shutdownCleanup();
		} finally {
			shutdownLock.writeLock().unlock();
		}

		return copy;
	}

	@Override
	public boolean isShutdown() {
		return shuttingDown;
	}

	@Override
	public boolean isTerminated() {
		return terminationFuture.isDone();
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		if (Thread.currentThread() == tickingThread) {
			throw new RuntimeException("The ticking thread may not wait for an execution");
		}

		try {
			terminationFuture.get(timeout, unit);
		} catch (CancellationException e) {
			// this would be a programming error
			throw new RuntimeException("Termination future was cancelled", e);
		} catch (ExecutionException e) {
			// this would be a programming error
			throw new RuntimeException("Termination future threw an exception", e);
		} catch (TimeoutException e) {
			return false;
		}
		return true;
	}

	@Override
	public void execute(Runnable command) {
		shutdownLock.readLock().lock();
		try {
			checkShutDown();
			taskQueue.add(command);
		} finally {
			shutdownLock.readLock().unlock();
		}
	}

	public void tick() {
		shutdownLock.readLock().lock();
		try {
			if (terminationFuture.isDone()) {
				throw new IllegalStateException("Ticked, although terminated");
			}

			final Iterator<Runnable> runnableIter = taskQueue.iterator();
			while (runnableIter.hasNext()) {
				Runnable r = runnableIter.next();

				// exception safe, as exception is catched in the future
				r.run();

				runnableIter.remove();
			}

			if (nextTask <= currentTick) {
				ArrayList<TickScheduledTask<?>> tasksToSchedule = new ArrayList<>();

				schedulerLock.lock();
				try {
					TickScheduledTask task = scheduledTaskList.peekFirst();
					while (task != null && task.getExecutorScheduledTick() <= currentTick) {
						task = scheduledTaskList.pollFirst();

						// exception safe, as exception is catched in the future
						task.run();

						if (task.isPeriodic()) {
							task.reschedule();
							tasksToSchedule.add(task);
						}
						task = scheduledTaskList.peekFirst();
					}
					if (task == null) {
						nextTask = Long.MAX_VALUE;
					} else {
						nextTask = task.getExecutorScheduledTick();
					}

					tasksToSchedule.forEach(this::scheduleUnsafe);
				} finally {
					schedulerLock.unlock();
				}
			}
		} finally {
			shutdownLock.readLock().unlock();
		}

		if (shuttingDown) {
			shutdownCleanup();
		}
		currentTick++;
	}

	private class TickScheduledTask<V> extends FutureTask<V> implements ScheduledFuture<V> {

		private long createdAt;

		/**
		 * A task should never be created for over 3.4 years in the future.
		 * Seriously.
		 */
		private int ticks;
		private boolean periodic;

		private void checkDelay(long delay) {
			if (delay < 0) {
				throw new IllegalArgumentException("Delay may not be lower than zero");
			}
		}

		/**
		 * Creates a run-once task.
		 */
		private TickScheduledTask(Callable<V> callable, int ticks) {
			super(callable);
			checkDelay(ticks);
			this.createdAt = currentTick;
			this.ticks = ticks;
			this.periodic = false;
		}

		/**
		 * Creates a repeating task.
		 */
		private TickScheduledTask(Callable<V> callable, int initialDelay, int ticks) {
			super(callable);
			checkDelay(initialDelay);
			checkDelay(ticks);
			this.createdAt = currentTick + initialDelay;
			this.ticks = ticks;
			this.periodic = true;
		}

		public int getExecutorScheduledTick() {
			return (int) (createdAt + ticks);
		}

		public int getTickDelay() {
			return (int) (createdAt + ticks - currentTick);
		}

		/**
		 * Approximate delay until next run.
		 */
		public long getMillisecondsDelay() {
			return getTickDelay() * MILLISECONDS_PER_TICK;
		}

		public boolean isPeriodic() {
			return periodic;
		}

		private void reschedule() {
			if (!periodic) {
				throw new IllegalStateException("Task is not periodic");
			}

			this.createdAt = currentTick;
		}

		@Override
		public long getDelay(TimeUnit unit) {
			return unit.convert(getMillisecondsDelay(), TimeUnit.MILLISECONDS);
		}

		@Override
		public int compareTo(Delayed o) {
			return Long.signum(getMillisecondsDelay() - o.getDelay(TimeUnit.MILLISECONDS));
		}
	}
}
