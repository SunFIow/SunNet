package com.sunflow.util;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

public class Utils {

	public static <T> Supplier<T> getCachedSupplier(Supplier<T> original) {
		return new Supplier<T>() {
			private Supplier<T> delegate = this::firstTime;

			@Override
			public T get() { return delegate.get(); }

			private synchronized T firstTime() {
				T value = original.get();
				delegate = () -> value;
				return value;
			}
		};
	}

	public static <T> Callable<T> getCachedCallable(Callable<T> original) {
		return new Callable<T>() {
			private Callable<T> delegate = this::firstTime;

			@Override
			public T call() throws Exception { return delegate.call(); }

			private synchronized T firstTime() throws Exception {
				T value = original.call();
				delegate = () -> value;
				return value;
			}
		};
	}
}
