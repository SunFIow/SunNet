package com.sunflow.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TSQueue<T> {
	protected ReentrantLock lock = new ReentrantLock();
	protected Deque<T> deqQueue;

	protected ReentrantLock muxBlocking = new ReentrantLock();

	public TSQueue() {
		lock = new ReentrantLock();
		deqQueue = new ArrayDeque<>();
//		deqQueue = new ConcurrentLinkedDeque<T>();
//		deqQueue = new LinkedList<>();
	}

	protected synchronized void tsrunnable(Runnable r) {
		lock.lock();
		try {
			r.run();
		} finally {
			lock.unlock();
		}
	}

	protected synchronized <S> S tssupplier(Supplier<S> s) {
		lock.lock();
		try {
			return s.get();
		} finally {
			lock.unlock();
		}
	}

	protected synchronized void tsconsumer(Consumer<T> c, T t) {
		lock.lock();
		try {
			c.accept(t);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Retrieves, but does not remove, the first element of this deque,
	 * or returns {@code null} if this deque is empty.
	 *
	 * @return the head of this deque, or {@code null} if this deque is empty
	 */
	public T front() { return tssupplier(deqQueue::peekFirst); }

	/**
	 * Retrieves, but does not remove, the last element of this deque,
	 * or returns {@code null} if this deque is empty.
	 *
	 * @return the tail of this deque, or {@code null} if this deque is empty
	 */
	public T back() { return tssupplier(deqQueue::peekLast); }

	/**
	 * Inserts the specified element at the front of this deque unless it would
	 * violate capacity restrictions. When using a capacity-restricted deque,
	 * this method is generally preferable to the {@link #addFirst} method,
	 * which can fail to insert an element only by throwing an exception.
	 *
	 * @param e
	 *            the element to add
	 * @return {@code true} if the element was added to this deque, else
	 *         {@code false}
	 * @throws ClassCastException
	 *             if the class of the specified element
	 *             prevents it from being added to this deque
	 * @throws NullPointerException
	 *             if the specified element is null and this
	 *             deque does not permit null elements
	 * @throws IllegalArgumentException
	 *             if some property of the specified
	 *             element prevents it from being added to this deque
	 */
	public void push_front(T item) { tsconsumer(deqQueue::offerFirst, item); wake(); }

	/**
	 * Inserts the specified element at the end of this deque unless it would
	 * violate capacity restrictions. When using a capacity-restricted deque,
	 * this method is generally preferable to the {@link #addLast} method,
	 * which can fail to insert an element only by throwing an exception.
	 *
	 * @param e
	 *            the element to add
	 * @return {@code true} if the element was added to this deque, else
	 *         {@code false}
	 * @throws ClassCastException
	 *             if the class of the specified element
	 *             prevents it from being added to this deque
	 * @throws NullPointerException
	 *             if the specified element is null and this
	 *             deque does not permit null elements
	 * @throws IllegalArgumentException
	 *             if some property of the specified
	 *             element prevents it from being added to this deque
	 */

	public void push_back(T item) { tsconsumer(deqQueue::offerLast, item); wake(); }

	/**
	 * Returns <tt>true</tt> if this collection contains no elements.
	 *
	 * @return <tt>true</tt> if this collection contains no elements
	 */
	public boolean empty() { return tssupplier(deqQueue::isEmpty); }

	/**
	 * Returns the number of elements in this deque.
	 *
	 * @return the number of elements in this deque
	 */
	public int count() { return tssupplier(deqQueue::size); }

	/**
	 * Removes all of the elements from this collection (optional operation).
	 * The collection will be empty after this method returns.
	 *
	 * @throws UnsupportedOperationException
	 *             if the <tt>clear</tt> operation
	 *             is not supported by this collection
	 */
	public void clear() { tsrunnable(deqQueue::clear); }

	/**
	 * Retrieves and removes the first element of this deque,
	 * or returns {@code null} if this deque is empty.
	 *
	 * @return the head of this deque, or {@code null} if this deque is empty
	 */
	public T pop_front() { return tssupplier(deqQueue::pollFirst); }

	/**
	 * Retrieves and removes the last element of this deque,
	 * or returns {@code null} if this deque is empty.
	 *
	 * @return the tail of this deque, or {@code null} if this deque is empty
	 */
	public T pop_back() { return tssupplier(deqQueue::pollLast); }

	public void wake() {
		synchronized (this) {
			notify();
		}
	}

	public void sleep() {
		synchronized (this) {
			while (empty()) try {
				wait();
			} catch (InterruptedException e) {
				Logger.error("TSQueue_sleep", "TSQueue got interrupted while waiting!", e);
			}
		}
	}
}
