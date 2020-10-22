
package com.sunflow.error;

public class ConnectingException extends SunNetException {
	private static final long serialVersionUID = -182163982271097156L;

	/**
	 * Constructs an {@code ConnectingException} with {@code null}
	 * as its error detail message.
	 */
	public ConnectingException() { super(); }

	/**
	 * Constructs an {@code ConnectingException} with the specified detail message.
	 *
	 * @param message
	 *            The detail message (which is saved for later retrieval
	 *            by the {@link #getMessage()} method)
	 */
	public ConnectingException(String message) {
		super(message);
	}

	/**
	 * Constructs an {@code ConnectingException} with the specified detail message
	 * and cause.
	 *
	 * <p>
	 * Note that the detail message associated with {@code cause} is
	 * <i>not</i> automatically incorporated into this exception's detail
	 * message.
	 *
	 * @param message
	 *            The detail message (which is saved for later retrieval
	 *            by the {@link #getMessage()} method)
	 *
	 * @param cause
	 *            The cause (which is saved for later retrieval by the
	 *            {@link #getCause()} method). (A null value is permitted,
	 *            and indicates that the cause is nonexistent or unknown.)
	 */
	public ConnectingException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs an {@code ConnectingException} with the specified cause and a
	 * detail message of {@code (cause==null ? null : cause.toString())}
	 * (which typically contains the class and detail message of {@code cause}).
	 * This constructor is useful for IO exceptions that are little more
	 * than wrappers for other throwables.
	 *
	 * @param cause
	 *            The cause (which is saved for later retrieval by the
	 *            {@link #getCause()} method). (A null value is permitted,
	 *            and indicates that the cause is nonexistent or unknown.)
	 */
	public ConnectingException(Throwable cause) {
		super(cause);
	}
}
