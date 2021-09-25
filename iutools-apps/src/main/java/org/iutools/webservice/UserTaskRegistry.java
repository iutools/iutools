package org.iutools.webservice;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;

public class UserTaskRegistry {
	private static Cache<String, Long> taskStartedAt =
		Caffeine.newBuilder().maximumSize(10000)
		.expireAfterWrite(10, TimeUnit.MINUTES)
			.build();

	public synchronized Long taskStartTime(String taskName) throws ServiceException {
		if (taskName == null) {
			throw new ServiceException("task name should not be null");
		} else {
			Long time = taskStartedAt.getIfPresent(taskName);
			if (time == null) {
				// Means this task has just been started
				time = System.currentTimeMillis();
				taskStartedAt.put(taskName, time);
			}
			return time;
		}
	}
}
