/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations under
 * the License.
 *
 * The Original Code is OpenELIS code.
 *
 * Copyright (C) CIRG, University of Washington, Seattle WA.  All Rights Reserved.
 */
package us.mn.state.health.lims.common.provider.query;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.validator.GenericValidator;

import us.mn.state.health.lims.common.util.StringUtil;
import us.mn.state.health.lims.common.util.XMLUtil;
import us.mn.state.health.lims.dashboard.dao.OrderListDAO;
import us.mn.state.health.lims.dashboard.daoimpl.OrderListDAOImpl;
import us.mn.state.health.lims.dashboard.valueholder.OrderedTestsLabelData;

/**
 * Returns ordered test names (and patient demographics) for barcode label printing.
 */
public class OrderedTestsPopulateProvider extends BaseQueryProvider {

    private final OrderListDAO orderListDAO = new OrderListDAOImpl(false);

    @Override
    public void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accessionNumber = request.getParameter("accessionNumber");
        StringBuilder xml = new StringBuilder();
        String result;

        if (GenericValidator.isBlankOrNull(accessionNumber)) {
            result = INVALID;
            xml.append("empty");
        } else {
            OrderedTestsLabelData labelData = orderListDAO.getOrderedTestsLabelDataByAccession(accessionNumber.trim());
            if (labelData == null) {
                result = StringUtil.getMessageForKey("patient.message.patientNotFound");
                xml.append("empty");
            } else {
                appendEscaped(xml, "tests", labelData.getTests());
                appendEscaped(xml, "patientName", labelData.getPatientName());
                appendEscaped(xml, "gender", labelData.getGender());
                appendEscaped(xml, "birth_date", labelData.getBirthDate());
                result = VALID;
            }
        }

        ajaxServlet.sendData(xml.toString(), result, request, response);
    }

    private void appendEscaped(StringBuilder xml, String key, String value) {
        XMLUtil.appendKeyValue(key, value == null ? "" : StringEscapeUtils.escapeXml(value), xml);
    }
}
