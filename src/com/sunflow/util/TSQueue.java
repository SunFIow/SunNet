package com.sunflow.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class TSQueue<T> {
	protected ReentrantLock lock = new ReentrantLock();
	protected Deque<T> deqQueue;

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
	public T front() {
//		lock.lock();
//		try {
//			return deqQueue.peekFirst();
//		} finally {
//			lock.unlock();
//		}
		return tssupplier(deqQueue::peekFirst);
	}

	/**
	 * Retrieves, but does not remove, the last element of this deque,
	 * or returns {@code null} if this deque is empty.
	 *
	 * @return the tail of this deque, or {@code null} if this deque is empty
	 */
	public T back() {
//		lock.lock();
//		try {
//			return deqQueue.peekLast();
//		} finally {
//			lock.unlock();
//		}
		return tssupplier(deqQueue::peekLast);
	}

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
	public void push_front(T item) {
//		lock.lock();
//		try {
//			deqQueue.offerFirst(item);
//		} finally {
//			lock.unlock();
//		}
		tsconsumer(deqQueue::offerFirst, item);
	}

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

	public void push_back(T item) {
//		lock.lock();
//		try {
//			deqQueue.offerLast(item);
//		} finally {
//			lock.unlock();
//		}
		tsconsumer(deqQueue::offerLast, item);
	}

	/**
	 * Returns <tt>true</tt> if this collection contains no elements.
	 *
	 * @return <tt>true</tt> if this collection contains no elements
	 */
	public boolean empty() {
//		lock.lock();
//		try {
//			return deqQueue.isEmpty();
//		} finally {
//			lock.unlock();
//		}
		return tssupplier(deqQueue::isEmpty);
	}

	/**
	 * Returns the number of elements in this deque.
	 *
	 * @return the number of elements in this deque
	 */
	public int count() {
//		lock.lock();
//		try {
//			return deqQueue.size();
//		} finally {
//			lock.unlock();
//		}
		return tssupplier(deqQueue::size);
	}

	/**
	 * Removes all of the elements from this collection (optional operation).
	 * The collection will be empty after this method returns.
	 *
	 * @throws UnsupportedOperationException
	 *             if the <tt>clear</tt> operation
	 *             is not supported by this collection
	 */
	public void clear() {
//		lock.lock();
//		try {
//			deqQueue.clear();
//		} finally {
//			lock.unlock();
//		}
		tsrunnable(deqQueue::clear);
	}

	/**
	 * Retrieves and removes the first element of this deque,
	 * or returns {@code null} if this deque is empty.
	 *
	 * @return the head of this deque, or {@code null} if this deque is empty
	 */
	public T pop_front() {
//		lock.lock();
//		try {
//			return deqQueue.pollFirst();
//		} finally {
//			lock.unlock();
//		}
		return tssupplier(deqQueue::pollFirst);
	}

	/**
	 * Retrieves and removes the last element of this deque,
	 * or returns {@code null} if this deque is empty.
	 *
	 * @return the tail of this deque, or {@code null} if this deque is empty
	 */
	public T pop_back() {
//		lock.lock();
//		try {
//			return deqQueue.pollLast();
//		} finally {
//			lock.unlock();
//		}
		return tssupplier(deqQueue::pollLast);
	}

}
