package org.bahmni.feed.openelis.odoo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import us.mn.state.health.lims.common.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Checks Laboratory shop Odoo sale-order lines / invoices for an EMR encounter
 * (OpenELIS sample UUID = OpenMRS encounter UUID).
 * <p>
 * Rules: Cash/Free require invoice state {@code paid}; other payment methods require
 * {@code open} or {@code paid}. Missing invoice or Odoo failure fails closed.
 */
public class LabOrderPaymentService {

	private static final Log log = LogFactory.getLog(LabOrderPaymentService.class);

	private static final String MSG_ODOO_UNAVAILABLE = "Sample collection blocked: billing system unavailable";
	private static final String MSG_NOT_CONFIGURED = "Sample collection blocked: billing is not configured";
	private static final String MSG_NO_BILLING = "Sample collection blocked: no lab billing found for this order";
	private static final String MSG_UNPAID = "Sample collection blocked: payment incomplete for lab order";

	private static final List<String> LINE_FIELDS = Arrays.asList("id", "name", "external_id", "order_id", "product_id");
	private static final List<String> ORDER_FIELDS = Arrays.asList("id", "name", "state", "payment_method", "invoice_ids", "shop_id");
	private static final List<String> INVOICE_FIELDS = Arrays.asList("id", "state", "payment_method", "number");

	private final OdooProperties properties;
	private final OdooClient odooClient;

	private static LabOrderPaymentService instance;

	public LabOrderPaymentService() {
		this(new OdooProperties());
	}

	public LabOrderPaymentService(OdooProperties properties) {
		this.properties = properties;
		this.odooClient = new OdooClient(properties);
	}

	public LabOrderPaymentService(OdooProperties properties, OdooClient odooClient) {
		this.properties = properties;
		this.odooClient = odooClient;
	}

	public static synchronized LabOrderPaymentService getInstance() {
		if (instance == null) {
			instance = new LabOrderPaymentService();
		}
		return instance;
	}

	/**
	 * When enforcement is off, collection is always allowed.
	 */
	public LabOrderPaymentStatus checkByEncounterUuid(String encounterUuid) {
		if (!properties.isEnforcementEnabled()) {
			return LabOrderPaymentStatus.allowed();
		}
		if (StringUtil.isNullorNill(encounterUuid)) {
			return LabOrderPaymentStatus.blocked(MSG_NO_BILLING);
		}
		if (!properties.isOdooConfigured()) {
			return LabOrderPaymentStatus.blocked(MSG_NOT_CONFIGURED);
		}
		try {
			return evaluateEncounter(encounterUuid.trim());
		} catch (OdooException e) {
			log.error("Odoo payment check failed for encounter " + encounterUuid + ": " + e.getMessage(), e);
			odooClient.resetSession();
			return LabOrderPaymentStatus.blocked(MSG_ODOO_UNAVAILABLE);
		} catch (RuntimeException e) {
			log.error("Unexpected payment check failure for encounter " + encounterUuid + ": " + e.getMessage(), e);
			odooClient.resetSession();
			return LabOrderPaymentStatus.blocked(MSG_ODOO_UNAVAILABLE);
		}
	}

	/**
	 * Batch-check many encounter UUIDs with as few Odoo round-trips as possible.
	 */
	public Map<String, LabOrderPaymentStatus> checkByEncounterUuids(List<String> encounterUuids) {
		Map<String, LabOrderPaymentStatus> results = new HashMap<String, LabOrderPaymentStatus>();
		if (!properties.isEnforcementEnabled()) {
			for (String uuid : encounterUuids) {
				if (!StringUtil.isNullorNill(uuid)) {
					results.put(uuid, LabOrderPaymentStatus.allowed());
				}
			}
			return results;
		}
		if (!properties.isOdooConfigured()) {
			LabOrderPaymentStatus blocked = LabOrderPaymentStatus.blocked(MSG_NOT_CONFIGURED);
			for (String uuid : encounterUuids) {
				if (!StringUtil.isNullorNill(uuid)) {
					results.put(uuid, blocked);
				}
			}
			return results;
		}

		Set<String> unique = new LinkedHashSet<String>();
		for (String uuid : encounterUuids) {
			if (!StringUtil.isNullorNill(uuid)) {
				unique.add(uuid.trim());
			}
		}
		if (unique.isEmpty()) {
			return results;
		}

		try {
			List<Object> domain = new ArrayList<Object>();
			domain.add(Arrays.asList("external_id", "in", new ArrayList<String>(unique)));
			List<Object> lineRows = odooClient.searchRead("sale.order.line", domain, LINE_FIELDS, null, 0);
			Map<String, List<Map<String, Object>>> linesByEncounter = groupLinesByEncounter(lineRows);

			Set<Integer> orderIds = collectOrderIds(lineRows);
			Map<Integer, Map<String, Object>> ordersById = loadLaboratoryOrders(orderIds);
			Map<Integer, Map<String, Object>> invoicesById = loadInvoices(ordersById);

			for (String uuid : unique) {
				List<Map<String, Object>> lines = linesByEncounter.get(uuid);
				results.put(uuid, evaluateLines(lines, ordersById, invoicesById));
			}
		} catch (OdooException e) {
			log.error("Odoo batch payment check failed: " + e.getMessage(), e);
			odooClient.resetSession();
			LabOrderPaymentStatus blocked = LabOrderPaymentStatus.blocked(MSG_ODOO_UNAVAILABLE);
			for (String uuid : unique) {
				results.put(uuid, blocked);
			}
		} catch (RuntimeException e) {
			log.error("Unexpected batch payment check failure: " + e.getMessage(), e);
			odooClient.resetSession();
			LabOrderPaymentStatus blocked = LabOrderPaymentStatus.blocked(MSG_ODOO_UNAVAILABLE);
			for (String uuid : unique) {
				results.put(uuid, blocked);
			}
		}
		return results;
	}

	private LabOrderPaymentStatus evaluateEncounter(String encounterUuid) {
		List<Object> domain = new ArrayList<Object>();
		domain.add(Arrays.asList("external_id", "=", encounterUuid));
		List<Object> lineRows = odooClient.searchRead("sale.order.line", domain, LINE_FIELDS, null, 0);
		Map<String, List<Map<String, Object>>> grouped = groupLinesByEncounter(lineRows);
		List<Map<String, Object>> lines = grouped.get(encounterUuid);
		Set<Integer> orderIds = collectOrderIds(lineRows);
		Map<Integer, Map<String, Object>> ordersById = loadLaboratoryOrders(orderIds);
		Map<Integer, Map<String, Object>> invoicesById = loadInvoices(ordersById);
		return evaluateLines(lines, ordersById, invoicesById);
	}

	private LabOrderPaymentStatus evaluateLines(List<Map<String, Object>> lines,
												Map<Integer, Map<String, Object>> ordersById,
												Map<Integer, Map<String, Object>> invoicesById) {
		if (lines == null || lines.isEmpty()) {
			return LabOrderPaymentStatus.blocked(MSG_NO_BILLING);
		}

		List<Map<String, Object>> labLines = new ArrayList<Map<String, Object>>();
		for (Map<String, Object> line : lines) {
			Integer orderId = many2oneId(line.get("order_id"));
			if (orderId != null && ordersById.containsKey(orderId)) {
				labLines.add(line);
			}
		}
		if (labLines.isEmpty()) {
			return LabOrderPaymentStatus.blocked(MSG_NO_BILLING);
		}

		int unpaid = 0;
		List<String> unpaidNames = new ArrayList<String>();
		for (Map<String, Object> line : labLines) {
			Integer orderId = many2oneId(line.get("order_id"));
			Map<String, Object> order = ordersById.get(orderId);
			if (!isLinePaid(order, invoicesById)) {
				unpaid++;
				String name = asString(line.get("name"));
				if (!StringUtil.isNullorNill(name)) {
					unpaidNames.add(name);
				}
			}
		}

		if (unpaid == 0) {
			return new LabOrderPaymentStatus(true, null, labLines.size(), 0);
		}

		String detail = unpaidNames.isEmpty()
				? MSG_UNPAID + " (" + unpaid + " of " + labLines.size() + " unpaid)"
				: MSG_UNPAID + ": " + joinNames(unpaidNames, 3) + (unpaidNames.size() > 3 ? "…" : "");
		return LabOrderPaymentStatus.blocked(detail, labLines.size(), unpaid);
	}

	/**
	 * Cash/Free require paid invoice; other methods require open or paid invoice.
	 */
	boolean isLinePaid(Map<String, Object> order, Map<Integer, Map<String, Object>> invoicesById) {
		if (order == null) {
			return false;
		}
		List<Integer> invoiceIds = asIdList(order.get("invoice_ids"));
		if (invoiceIds.isEmpty()) {
			return false;
		}

		String orderPaymentMethod = asString(order.get("payment_method"));
		for (Integer invoiceId : invoiceIds) {
			Map<String, Object> invoice = invoicesById.get(invoiceId);
			if (invoice == null) {
				continue;
			}
			String state = asString(invoice.get("state"));
			if ("cancel".equalsIgnoreCase(state)) {
				continue;
			}
			String paymentMethod = asString(invoice.get("payment_method"));
			if (StringUtil.isNullorNill(paymentMethod)) {
				paymentMethod = orderPaymentMethod;
			}
			if (isInvoiceAllowed(paymentMethod, state)) {
				return true;
			}
		}
		return false;
	}

	boolean isInvoiceAllowed(String paymentMethod, String state) {
		if (StringUtil.isNullorNill(state)) {
			return false;
		}
		String method = paymentMethod == null ? "" : paymentMethod.trim();
		String normalizedState = state.trim().toLowerCase();
		if ("cash".equalsIgnoreCase(method) || "free".equalsIgnoreCase(method)) {
			return "paid".equals(normalizedState);
		}
		// Credit and other methods: open (or paid) invoice is acceptable
		return "open".equals(normalizedState) || "paid".equals(normalizedState);
	}

	@SuppressWarnings("unchecked")
	private Map<String, List<Map<String, Object>>> groupLinesByEncounter(List<Object> lineRows) {
		Map<String, List<Map<String, Object>>> grouped = new HashMap<String, List<Map<String, Object>>>();
		if (lineRows == null) {
			return grouped;
		}
		for (Object row : lineRows) {
			if (!(row instanceof Map)) {
				continue;
			}
			Map<String, Object> map = (Map<String, Object>) row;
			String externalId = asString(map.get("external_id"));
			if (StringUtil.isNullorNill(externalId)) {
				continue;
			}
			List<Map<String, Object>> list = grouped.get(externalId);
			if (list == null) {
				list = new ArrayList<Map<String, Object>>();
				grouped.put(externalId, list);
			}
			list.add(map);
		}
		return grouped;
	}

	@SuppressWarnings("unchecked")
	private Set<Integer> collectOrderIds(List<Object> lineRows) {
		Set<Integer> ids = new HashSet<Integer>();
		if (lineRows == null) {
			return ids;
		}
		for (Object row : lineRows) {
			if (!(row instanceof Map)) {
				continue;
			}
			Integer orderId = many2oneId(((Map<String, Object>) row).get("order_id"));
			if (orderId != null) {
				ids.add(orderId);
			}
		}
		return ids;
	}

	@SuppressWarnings("unchecked")
	private Map<Integer, Map<String, Object>> loadLaboratoryOrders(Set<Integer> orderIds) {
		Map<Integer, Map<String, Object>> ordersById = new HashMap<Integer, Map<String, Object>>();
		if (orderIds == null || orderIds.isEmpty()) {
			return ordersById;
		}
		List<Object> domain = new ArrayList<Object>();
		domain.add(Arrays.asList("id", "in", new ArrayList<Integer>(orderIds)));
		domain.add(Arrays.asList("shop_id.name", "=", properties.getLabShopName()));
		List<Object> rows = odooClient.searchRead("sale.order", domain, ORDER_FIELDS, null, 0);
		if (rows == null) {
			return ordersById;
		}
		for (Object row : rows) {
			if (!(row instanceof Map)) {
				continue;
			}
			Map<String, Object> map = (Map<String, Object>) row;
			Integer id = asInteger(map.get("id"));
			if (id != null) {
				ordersById.put(id, map);
			}
		}
		return ordersById;
	}

	@SuppressWarnings("unchecked")
	private Map<Integer, Map<String, Object>> loadInvoices(Map<Integer, Map<String, Object>> ordersById) {
		Map<Integer, Map<String, Object>> invoicesById = new HashMap<Integer, Map<String, Object>>();
		Set<Integer> invoiceIds = new HashSet<Integer>();
		for (Map<String, Object> order : ordersById.values()) {
			invoiceIds.addAll(asIdList(order.get("invoice_ids")));
		}
		if (invoiceIds.isEmpty()) {
			return invoicesById;
		}
		List<Object> domain = new ArrayList<Object>();
		domain.add(Arrays.asList("id", "in", new ArrayList<Integer>(invoiceIds)));
		List<Object> rows = odooClient.searchRead("account.invoice", domain, INVOICE_FIELDS, null, 0);
		if (rows == null) {
			return invoicesById;
		}
		for (Object row : rows) {
			if (!(row instanceof Map)) {
				continue;
			}
			Map<String, Object> map = (Map<String, Object>) row;
			Integer id = asInteger(map.get("id"));
			if (id != null) {
				invoicesById.put(id, map);
			}
		}
		return invoicesById;
	}

	@SuppressWarnings("unchecked")
	private static List<Integer> asIdList(Object value) {
		List<Integer> ids = new ArrayList<Integer>();
		if (value == null || Boolean.FALSE.equals(value)) {
			return ids;
		}
		if (value instanceof Object[]) {
			for (Object item : (Object[]) value) {
				Integer id = asInteger(item);
				if (id != null) {
					ids.add(id);
				}
			}
			return ids;
		}
		if (value instanceof List) {
			for (Object item : (List<Object>) value) {
				Integer id = asInteger(item);
				if (id != null) {
					ids.add(id);
				}
			}
		}
		return ids;
	}

	private static Integer many2oneId(Object value) {
		if (value instanceof Object[]) {
			Object[] pair = (Object[]) value;
			if (pair.length > 0) {
				return asInteger(pair[0]);
			}
			return null;
		}
		if (value instanceof List) {
			List<?> list = (List<?>) value;
			if (!list.isEmpty()) {
				return asInteger(list.get(0));
			}
			return null;
		}
		return asInteger(value);
	}

	private static Integer asInteger(Object value) {
		if (value instanceof Integer) {
			return (Integer) value;
		}
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		return null;
	}

	private static String asString(Object value) {
		if (value == null || Boolean.FALSE.equals(value)) {
			return null;
		}
		return String.valueOf(value).trim();
	}

	private static String joinNames(List<String> names, int limit) {
		StringBuilder sb = new StringBuilder();
		int count = Math.min(limit, names.size());
		for (int i = 0; i < count; i++) {
			if (i > 0) {
				sb.append("; ");
			}
			sb.append(names.get(i));
		}
		return sb.toString();
	}
}
