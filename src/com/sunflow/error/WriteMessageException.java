
package com.sunflow.error;

public class WriteMessageException extends SunNetException {
	private static final long serialVersionUID = 9032414687919571331L;

	/**
	 * Constructs an {@code WriteMessageException} with {@code null}
	 * as its error detail message.
	 */
	public WriteMessageException() { super(); }

	/**
	 * Constructs an {@code WriteMessageException} with the specified detail message.
	 *
	 * @param message
	 *            The detail message (which is saved for later retrieval
	 *            by the {@link #getMessage()} method)
	 */
	public WriteMessageException(String message) {
		super(message);
	}

	/**
	 * Constructs an {@code WriteMessageException} with the specified detail message
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
	public WriteMessageException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs an {@code WriteMessageException} with the specified cause and a
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
	public WriteMessageException(Throwable cause) {
		super(cause);
	}
}
