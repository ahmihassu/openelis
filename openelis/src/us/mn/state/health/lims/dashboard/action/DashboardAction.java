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
* Copyright (C) The Minnesota Department of Health.  All Rights Reserved.
*/

package us.mn.state.health.lims.dashboard.action;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.DynaActionForm;
import org.bahmni.feed.openelis.ObjectMapperRepository;
import org.bahmni.feed.openelis.odoo.LabOrderPaymentService;
import org.bahmni.feed.openelis.odoo.LabOrderPaymentStatus;
import us.mn.state.health.lims.common.action.BaseAction;
import us.mn.state.health.lims.dashboard.dao.OrderListDAO;
import us.mn.state.health.lims.dashboard.daoimpl.OrderListDAOImpl;
import us.mn.state.health.lims.dashboard.valueholder.Order;
import us.mn.state.health.lims.siteinformation.daoimpl.SiteInformationDAOImpl;
import us.mn.state.health.lims.siteinformation.valueholder.SiteInformation;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DashboardAction extends BaseAction {
    private static final String GROUP_BY_SAMPLE = "groupBySample";
    public static final String ACCESSION_STRATEGY = "accessionStrategy";

    public DashboardAction() {
    }

    @Override
    protected ActionForward performAction(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        DynaActionForm dynaForm = (DynaActionForm) form;

        SiteInformation siteInformation = new SiteInformationDAOImpl().getSiteInformationByName(ACCESSION_STRATEGY);
        String accessionStrategy = siteInformation != null ? siteInformation.getValue() : "";
        Boolean isGroupBySample = GROUP_BY_SAMPLE.equals(accessionStrategy);
        OrderListDAO orderListDAO = new OrderListDAOImpl(isGroupBySample);

        List<Order> todaySampleNotCollected = orderListDAO.getAllSampleNotCollectedToday();
        List<Order> backlogSampleNotCollected = orderListDAO.getAllSampleNotCollectedPendingBeforeToday();
        enrichPaymentStatus(todaySampleNotCollected, backlogSampleNotCollected);

        String escapedTodayOrderListJson = asJson(orderListDAO.getAllToday());
        String escapedTodaySampleNotCollectedListJson = asJson(todaySampleNotCollected);
        String escapedBacklogSampleNotCollectedListJson = asJson(backlogSampleNotCollected);
        String escapedBacklogOrderListJson = asJson(orderListDAO.getAllPendingBeforeToday());

        dynaForm.set("todayOrderList", escapedTodayOrderListJson);
        dynaForm.set("todaySampleNotCollectedList", escapedTodaySampleNotCollectedListJson);
        dynaForm.set("backlogSampleNotCollectedList", escapedBacklogSampleNotCollectedListJson);
        dynaForm.set("backlogOrderList", escapedBacklogOrderListJson);
        dynaForm.set("isGroupBySample", isGroupBySample);

        return mapping.findForward("success");
    }

    private void enrichPaymentStatus(List<Order>... sampleNotCollectedLists) {
        List<String> uuids = new ArrayList<String>();
        for (List<Order> orders : sampleNotCollectedLists) {
            if (orders == null) {
                continue;
            }
            for (Order order : orders) {
                if (order.getUuid() != null) {
                    uuids.add(order.getUuid());
                }
            }
        }
        if (uuids.isEmpty()) {
            return;
        }
        Map<String, LabOrderPaymentStatus> statusByUuid = LabOrderPaymentService.getInstance().checkByEncounterUuids(uuids);
        for (List<Order> orders : sampleNotCollectedLists) {
            if (orders == null) {
                continue;
            }
            for (Order order : orders) {
                LabOrderPaymentStatus status = statusByUuid.get(order.getUuid());
                if (status == null) {
                    continue;
                }
                order.setPaymentCollectAllowed(status.isCollectAllowed());
                order.setPaymentMessage(status.getMessage());
            }
        }
    }

    @Override
    protected String getPageTitleKey() {
        return "Dashboard";
    }

    @Override
    protected String getPageSubtitleKey() {
        return "Dashboard";
    }

    private String asJson(Object o) throws IOException {
        String listJson = ObjectMapperRepository.objectMapper.writeValueAsString(o);
        return StringEscapeUtils.escapeEcmaScript(listJson);
    }
}
