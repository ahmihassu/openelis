package org.bahmni.feed.openelis.odoo;

/**
 * Result of checking Odoo lab-order payment for a sample / encounter.
 */
public class LabOrderPaymentStatus {

	private final boolean collectAllowed;
	private final String message;
	private final int totalLines;
	private final int unpaidLines;

	public LabOrderPaymentStatus(boolean collectAllowed, String message, int totalLines, int unpaidLines) {
		this.collectAllowed = collectAllowed;
		this.message = message;
		this.totalLines = totalLines;
		this.unpaidLines = unpaidLines;
	}

	public static LabOrderPaymentStatus allowed() {
		return new LabOrderPaymentStatus(true, null, 0, 0);
	}

	public static LabOrderPaymentStatus blocked(String message) {
		return new LabOrderPaymentStatus(false, message, 0, 0);
	}

	public static LabOrderPaymentStatus blocked(String message, int totalLines, int unpaidLines) {
		return new LabOrderPaymentStatus(false, message, totalLines, unpaidLines);
	}

	public boolean isCollectAllowed() {
		return collectAllowed;
	}

	public String getMessage() {
		return message;
	}

	public int getTotalLines() {
		return totalLines;
	}

	public int getUnpaidLines() {
		return unpaidLines;
	}
}
