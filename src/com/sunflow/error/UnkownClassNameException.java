
package com.sunflow.error;

public class UnkownClassNameException extends ReadMessageException {
	private static final long serialVersionUID = 8618065180441049928L;

	/**
	 * Constructs an {@code ReadMessageException} with {@code null}
	 * as its error detail message.
	 */
	public UnkownClassNameException() { super(); }

	/**
	 * Constructs an {@code ReadMessageException} with the specified detail message.
	 *
	 * @param message
	 *            The detail message (which is saved for later retrieval
	 *            by the {@link #getMessage()} method)
	 */
	public UnkownClassNameException(String message) {
		super(message);
	}

	/**
	 * Constructs an {@code ReadMessageException} with the specified detail message
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
	public UnkownClassNameException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs an {@code ReadMessageException} with the specified cause and a
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
	public UnkownClassNameException(Throwable cause) {
		super(cause);
	}
}
