package com.prism.managers;

import com.prism.Prism;
import com.prism.components.frames.ErrorDialog;
import com.prism.components.frames.WarningDialog;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadsManager {
	private static final Prism prism = Prism.getInstance();

	private static final ExecutorService executor;
	private static final Map<UUID, Future<?>> runningTasks;
	private static final Map<UUID, Thread> activeThreads;

	static {
		int corePoolSize = Math.max(2, Runtime.getRuntime().availableProcessors());
		executor = new ThreadPoolExecutor(
				corePoolSize,
				corePoolSize * 2,
				60L, TimeUnit.SECONDS,
				new LinkedBlockingQueue<>(),
				new NamedThreadFactory("PrismWorker"),
				new ThreadPoolExecutor.AbortPolicy()
		);
		runningTasks = new ConcurrentHashMap<>();
		activeThreads = new ConcurrentHashMap<>();
	}

	public static UUID submit(String name, Runnable runnable) {
		UUID id = UUID.randomUUID();
		Future<?> future = executor.submit(() -> {
			try {
				runnable.run();
			} catch (Exception e) {
				new ErrorDialog(prism, e);
			} finally {
				runningTasks.remove(id);
				activeThreads.remove(id);
			}
		});
		runningTasks.put(id, future);
		return id;
	}

	public static UUID submitAndTrackThread(String name, Runnable runnable) {
		UUID id = UUID.randomUUID();
		Thread t = new Thread(() -> {
			try {
				runnable.run();
			} catch (Exception e) {
				new WarningDialog(prism, e);
			} finally {
				//activeThreads.remove(id);
			}
		}, name + "-" + id.toString().substring(0, 8));
		activeThreads.put(id, t);
		t.start();
		return id;
	}

	public static boolean cancel(UUID id) {
		Future<?> f = runningTasks.remove(id);
		if (f != null) {
			return f.cancel(true);
		}
		Thread t = activeThreads.remove(id);
		if (t != null) {
			t.interrupt();
			return true;
		}
		return false;
	}

	public static boolean isAlive(UUID id) {
		return runningTasks.containsKey(id) ||
				(activeThreads.containsKey(id) && activeThreads.get(id).isAlive());
	}

	public static List<UUID> getRunningIds() {
		List<UUID> list = new ArrayList<>();
		list.addAll(runningTasks.keySet());
		list.addAll(activeThreads.keySet());
		return list;
	}

	public static void shutdown() {
		executor.shutdownNow();
		activeThreads.values().forEach(Thread::interrupt);
		activeThreads.clear();
		runningTasks.clear();
	}

	public static List<Thread> getAllThreads() {
		return activeThreads.values().stream().toList();
	}

	private static class NamedThreadFactory implements ThreadFactory {
		private final String prefix;
		private final AtomicInteger counter = new AtomicInteger(0);

		NamedThreadFactory(String prefix) {
			this.prefix = prefix;
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, prefix + "-" + counter.incrementAndGet());
			t.setDaemon(false);
			return t;
		}
	}
}