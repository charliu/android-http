package com.vim.ahttp;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutorFactory {

	private static final int DEFAULT_POOL_SIZE = 4;
	private static final int MAXIMUM_POOL_SIZE = 128;
	private static final int KEEP_ALIVE = 1;
	private static final int DEFAULT_THREAD_PRIORITY = Thread.NORM_PRIORITY - 1;

	public static Executor createDefaultExecutor() {
		return createExecutor(DEFAULT_POOL_SIZE, MAXIMUM_POOL_SIZE, DEFAULT_THREAD_PRIORITY,
				new LinkedBlockingQueue<Runnable>());
	}

	public static Executor createExecutor(int corePoolSize, int maxPoolSize, int threadPriority,
			BlockingQueue<Runnable> taskQueue) {
		return new ThreadPoolExecutor(corePoolSize, maxPoolSize, KEEP_ALIVE, TimeUnit.SECONDS, taskQueue,
				createThreadFactory(threadPriority));
	}

	private static ThreadFactory createThreadFactory(int threadPriority) {
		return new DefaultThreadFactory(threadPriority);
	}

	private static class DefaultThreadFactory implements ThreadFactory {

		private static final AtomicInteger poolNumber = new AtomicInteger(1);

		private final ThreadGroup group;
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;
		private final int threadPriority;

		DefaultThreadFactory(int threadPriority) {
			this.threadPriority = threadPriority;
			SecurityManager s = System.getSecurityManager();
			group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
			namePrefix = "http-pool-" + poolNumber.getAndIncrement() + "-thread-";
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
			if (t.isDaemon())
				t.setDaemon(false);
			t.setPriority(threadPriority);
			return t;
		}
	}
}
