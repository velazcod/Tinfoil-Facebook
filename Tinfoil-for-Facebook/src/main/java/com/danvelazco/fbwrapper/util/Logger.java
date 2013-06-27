/*
 * Copyright (C) 2013 Daniel Velazco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.danvelazco.fbwrapper.util;

import android.util.Log;
import com.danvelazco.fbwrapper.BuildConfig;

/**
 * Logger wrapper class to handle {@link android.util.Log} depending on debug level.
 */
public class Logger {

    // Static field with default level
    private static int sLevel = BuildConfig.DEBUG ? Log.VERBOSE : Log.ERROR;

    /**
     * Get current debug level of this application.
     *
     * @return {@link int}, for example {@link android.util.Log#DEBUG}.<br />
     *         See {@link android.util.Log} for more
     */
    public static int getLevel() {
        return sLevel;
    }

    /**
     * Set the debug level of this application.
     *
     * @param level {@link int}, for example {@link android.util.Log#DEBUG}.<br />
     *              See {@link android.util.Log} for more.
     */
    public static void setLevel(int level) {
        sLevel = level;
    }

    /**
     * Send a {@link  android.util.Log#DEBUG} log message.
     *
     * @param tag {@link String} Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg {@link String} The message you would like logged.
     */
    public static void d(String tag, String msg) {
        if (Log.DEBUG >= sLevel) {
            Log.d(tag, msg);
        }
    }

    /**
     * Send a {@link  android.util.Log#ERROR} log message.
     *
     * @param tag {@link String} Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg {@link String} The message you would like logged.
     */
    public static void e(String tag, String msg) {
        if (Log.ERROR >= sLevel) {
            Log.e(tag, msg);
        }
    }

    /**
     * Send a {@link android.util.Log#ERROR} log message and log the exception.
     *
     * @param tag {@link String} Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg {@link String} The message you would like logged.
     * @param t   {@link Throwable} An exception to log
     */
    public static void e(String tag, String msg, Throwable t) {
        if (Log.ERROR >= sLevel) {
            Log.e(tag, msg, t);
        }
    }

    /**
     * Send a {@link  android.util.Log#INFO} log message.
     *
     * @param tag {@link String} Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg {@link String} The message you would like logged.
     */
    public static void i(String tag, String msg) {
        if (Log.INFO >= sLevel) {
            Log.i(tag, msg);
        }
    }

    /**
     * Send a {@link android.util.Log#INFO} log message and log the exception.
     *
     * @param tag {@link String} Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg {@link String} The message you would like logged.
     * @param t   {@link Throwable} An exception to log
     */
    public static void i(String tag, String msg, Throwable t) {
        if (Log.INFO >= sLevel) {
            Log.i(tag, msg, t);
        }
    }

    /**
     * Send a {@link  android.util.Log#VERBOSE} log message.
     *
     * @param tag {@link String} Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg {@link String} The message you would like logged.
     */
    public static void v(String tag, String msg) {
        if (Log.VERBOSE >= sLevel) {
            Log.v(tag, msg);
        }
    }

    /**
     * Send a {@link  android.util.Log#WARN} log message.
     *
     * @param tag {@link String} Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg {@link String} The message you would like logged.
     */
    public static void w(String tag, String msg) {
        if (Log.WARN >= sLevel) {
            Log.w(tag, msg);
        }
    }

    /**
     * Send a {@link android.util.Log#WARN} log message and log the exception.
     *
     * @param tag {@link String} Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg {@link String} The message you would like logged.
     * @param t   {@link Throwable} An exception to log
     */
    public static void w(String tag, String msg, Throwable t) {
        if (Log.WARN >= sLevel) {
            Log.w(tag, msg, t);
        }
    }

    /**
     * What a Terrible Failure: Report an exception that should never happen.
     *
     * @param tag {@link String} Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg {@link String} The message you would like logged.
     */
    public static void wtf(String tag, String msg) {
        if (Log.WARN >= sLevel) {
            Log.wtf(tag, msg);
        }
    }

    /**
     * What a Terrible Failure: Report an exception that should never happen.
     *
     * @param tag {@link String} Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg {@link String} The message you would like logged.
     * @param t   {@link Throwable} An exception to log
     */
    public static void wtf(String tag, String msg, Throwable t) {
        if (Log.WARN >= sLevel) {
            Log.wtf(tag, msg, t);
        }
    }

}
