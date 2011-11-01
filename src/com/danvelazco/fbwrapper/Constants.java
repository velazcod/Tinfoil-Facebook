package com.danvelazco.fbwrapper;

/**
 * Constants class containing fields for configuration
 * 
 * @author Daniel Velazco
 *
 */
public class Constants {

	/** TAG used for Logging */
	public static final String TAG = "FB-Wrapper";
	
	/** Desktop user agent (Google Chrome's user agent from a MacBook running 10.7.2, whatever) */ 
	public static final String USER_AGENT_DESKTOP = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) "
			+ "AppleWebKit/535.1 (KHTML, like Gecko) Chrome/14.0.835.202 Safari/535.1";
	
	/** URLs for Mobile site */
	public static final String URL_MOBILE_SITE = "https://m.facebook.com";
	public static final String URL_MOBILE_NOTIFICATIONS = "https://m.facebook.com";
	
	/** URLs for Desktop site */
	public static final String URL_DESKTOP_SITE = "https://www.facebook.com";
	public static final String URL_DESKTOP_NOTIFICATIONS = "https://www.facebook.com/notifications.php";
	
	/** Preferences */
	public final static String PREFS_ALLOW_CHECKINS = "prefs_allow_checkins";
	public final static String PREFS_OPEN_LINKS_INSIDE = "prefs_open_links_inside";
	public final static String PREFS_SITE_MODE = "prefs_mobile_site";
	public final static String PREFS_SITE_MODE_AUTO = "auto";
	public final static String PREFS_SITE_MODE_MOBILE = "mobile";
	public final static String PREFS_SITE_MODE_DESKTOP = "desktop";
	public final static String PREFS_LOGCAT_ENABLED = "pref_logcat";
	public final static String PREFS_ABOUT = "pref_about";
	
}