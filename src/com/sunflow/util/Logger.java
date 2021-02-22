package com.sunflow.util;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

	/*
	 * Log Methods without Marker
	 */

	public static void fatal(Object msg) {
		if (!Settings.logFatal) return;
		log(System.err, "FATAL", msg);
	}

	public static void error(Object msg) {
		if (!Settings.logError) return;
		log(System.err, "ERROR", msg);
	}

	public static void warn(Object msg) {
		if (!Settings.logWarning) return;
		log(System.out, "WARNING", msg);
	}

	public static void info(Object msg) {
		if (!Settings.logInfo) return;
		log(System.out, "INFO", msg);
	}

	public static void help(Object msg) {
		if (!Settings.logHelp) return;
		log(System.out, "HELP", msg);
	}

	public static void debug(Object msg) {
		if (!Settings.logDebug) return;
		log(System.out, "DEBUG", msg);
	}

	public static void net(Object msg) {
		if (!Settings.logNet) return;
		log(System.out, "NET", msg);
	}

	/*
	 * Log Methods with Marker
	 */

	public static void fatal(Object marker, Object msg) {
		if (!Settings.logFatal) return;
		log(System.err, "FATAL", marker, msg);
	}

	public static void error(Object marker, Object msg) {
		if (!Settings.logError) return;
		log(System.err, "ERROR", marker, msg);
	}

	public static void warn(Object marker, Object msg) {
		if (!Settings.logWarning) return;
		log(System.out, "WARNING", marker, msg);
	}

	public static void info(Object marker, Object msg) {
		if (!Settings.logInfo) return;
		log(System.out, "INFO", marker, msg);
	}

	public static void help(Object marker, Object msg) {
		if (!Settings.logHelp) return;
		log(System.out, "HELP", marker, msg);
	}

	public static void debug(Object marker, Object msg) {
		if (!Settings.logDebug) return;
		log(System.out, "DEBUG", marker, msg);
	}

	public static void net(Object marker, Object msg) {
		if (!Settings.logNet) return;
		log(System.out, "NET", marker, msg);
	}
	/*
	 * Log Methods without Marker But with Throwable
	 */

	public static void fatal(Object msg, Throwable error) {
		if (!Settings.logFatal) return;
		log(System.err, "FATAL", msg);
		error.printStackTrace();
	}

	public static void error(Object msg, Throwable error) {
		if (!Settings.logError) return;
		log(System.err, "ERROR", msg);
		error.printStackTrace();
	}

	/*
	 * Log Methods with Marker and Throwable
	 */

	public static void fatal(Object marker, Object msg, Throwable error) {
		if (!Settings.logFatal) return;
		log(System.err, "FATAL", marker, msg);
		error.printStackTrace();
	}

	public static void error(Object marker, Object msg, Throwable error) {
		if (!Settings.logError) return;
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
