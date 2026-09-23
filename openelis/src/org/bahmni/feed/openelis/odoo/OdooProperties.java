package org.bahmni.feed.openelis.odoo;

import org.bahmni.feed.openelis.AtomFeedProperties;
import us.mn.state.health.lims.common.util.StringUtil;

/**
 * Odoo connection and lab-payment enforcement settings from atomfeed.properties.
 */
public class OdooProperties {

	public static final String KEY_HOST = "odoo.host";
	public static final String KEY_PORT = "odoo.port";
	public static final String KEY_DATABASE = "odoo.database";
	public static final String KEY_USER = "odoo.user";
	public static final String KEY_PASSWORD = "odoo.password";
	public static final String KEY_LAB_SHOP = "odoo.lab.shop.name";
	public static final String KEY_ENFORCE = "enforce.lab.order.payment";

	private static final String DEFAULT_HOST = "localhost";
	private static final int DEFAULT_PORT = 8069;
	private static final String DEFAULT_DATABASE = "odoo";
	private static final String DEFAULT_LAB_SHOP = "Laboratory";

	private final AtomFeedProperties properties;

	public OdooProperties() {
		this(AtomFeedProperties.getInstance());
	}

	public OdooProperties(AtomFeedProperties properties) {
		this.properties = properties;
	}

	public boolean isEnforcementEnabled() {
		return "true".equalsIgnoreCase(trim(properties.getProperty(KEY_ENFORCE)));
	}

	public boolean isOdooConfigured() {
		return !StringUtil.isNullorNill(getOdooUser()) && !StringUtil.isNullorNill(getOdooPassword());
	}

	public String getOdooHost() {
		String host = trim(properties.getProperty(KEY_HOST));
		return StringUtil.isNullorNill(host) ? DEFAULT_HOST : host;
	}

	public int getOdooPort() {
		String port = trim(properties.getProperty(KEY_PORT));
		if (StringUtil.isNullorNill(port)) {
			return DEFAULT_PORT;
		}
		try {
			return Integer.parseInt(port);
		} catch (NumberFormatException e) {
			return DEFAULT_PORT;
		}
	}

	public String getOdooDatabase() {
		String database = trim(properties.getProperty(KEY_DATABASE));
		return StringUtil.isNullorNill(database) ? DEFAULT_DATABASE : database;
	}

	public String getOdooUser() {
		return trim(properties.getProperty(KEY_USER));
	}

	public String getOdooPassword() {
		return trim(properties.getProperty(KEY_PASSWORD));
	}

	public String getLabShopName() {
		String shop = trim(properties.getProperty(KEY_LAB_SHOP));
		return StringUtil.isNullorNill(shop) ? DEFAULT_LAB_SHOP : shop;
	}

	private static String trim(String value) {
		return value == null ? null : value.trim();
	}
}
