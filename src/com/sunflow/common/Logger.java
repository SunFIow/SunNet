package com.sunflow.common;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

	/*
	 * Log Methods without Marker
	 */

	public static void fatal(Object msg) {
		if (!Constants.logFatals) return;
		log(System.err, "FATAL", msg);
	}

	public static void error(Object msg) {
		if (!Constants.logErrors) return;
		log(System.err, "ERROR", msg);
	}

	public static void info(Object msg) {
		if (!Constants.logInfos) return;
		log(System.out, "INFO", msg);
	}

	public static void debug(Object msg) {
		if (!Constants.logDebugs) return;
		log(System.out, "DEBUG", msg);
	}

	/*
	 * Log Methods with Marker
	 */

	public static void fatal(Object marker, Object msg) {
		if (!Constants.logFatals) return;
		log(System.err, "FATAL", marker, msg);
	}

	public static void error(Object marker, Object msg) {
		if (!Constants.logErrors) return;
		log(System.err, "ERROR", marker, msg);
	}

	public static void info(Object marker, Object msg) {
		if (!Constants.logInfos) return;
		log(System.out, "INFO", marker, msg);
	}

	public static void debug(Object marker, Object msg) {
		if (!Constants.logDebugs) return;
		log(System.out, "DEBUG", marker, msg);
	}

	/*
	 * Log Methods without Marker But with Throwable
	 */

	public static void fatal(Object msg, Throwable error) {
		if (!Constants.logFatals) return;
		log(System.err, "FATAL", msg);
		error.printStackTrace();
	}

	public static void error(Object msg, Throwable error) {
		if (!Constants.logErrors) return;
		log(System.err, "ERROR", msg);
		error.printStackTrace();
	}

	/*
	 * Log Methods with Marker and Throwable
	 */

	public static void fatal(Object marker, Object msg, Throwable error) {
		if (!Constants.logFatals) return;
		log(System.err, "FATAL", marker, msg);
		error.printStackTrace();
	}

	public static void error(Object marker, Object msg, Throwable error) {
		if (!Constants.logErrors) return;
		log(System.err, "ERROR", marker, msg);
		error.printStackTrace();
	}

	/**
	 * Helper functions
	 */

	public static void log(PrintStream out, String level, Object msg) {
		log(out, "[" + level + "] " + msg);
	}

	public static void log(PrintStream out, String level, Object marker, Object msg) {
		log(out, "[" + marker + "/" + level + "] " + msg);
	}

	public static void log(PrintStream out, Object msg) {
		out.println(getTimeStamp() + msg);
	}

	private static String getTimeStamp() {
		Date now = new Date();
		SimpleDateFormat sdfDate = new SimpleDateFormat("[ddMMMyyyy HH:mm:ss.SSS] ");
		String timeStamp = sdfDate.format(now);
		return timeStamp;
	}
}
