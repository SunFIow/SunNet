package com.sunflow.util;

/**
 * A task that may throw an exception.
 * Implementors define a single method with no arguments called
 * {@code execute}.
 *
 * <p>
 * The {@code Task} interface is similar to {@link
 * java.lang.Runnable}, in that both are designed for classes whose
 * instances are potentially executed by another thread. A
 * {@code Runnable}, however, cannot throw a checked exception.
 *
 * @author SunFlow
 */
@FunctionalInterface
public interface Task<T extends Exception> {
//public interface Task {
	/**
	 * Executes a task, or throws an exception if unable to do so.
	 *
	 * @throws Exception
	 *             if unable to execute
	 */

	void execute() throws T;
}
