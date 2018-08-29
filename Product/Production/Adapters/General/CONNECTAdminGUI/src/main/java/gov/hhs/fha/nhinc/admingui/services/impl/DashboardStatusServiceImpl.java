/*
 * Copyright (c) 2009-2018, United States Government, as represented by the Secretary of Health and Human Services.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above
 *       copyright notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of the United States Government nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE UNITED STATES GOVERNMENT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gov.hhs.fha.nhinc.admingui.services.impl;

import static gov.hhs.fha.nhinc.admingui.util.HelperUtil.buildConfigAssertion;
import static gov.hhs.fha.nhinc.nhinclib.NhincConstants.ADMIN_GUI_MANAGEMENT_SERVICE_NAME;

import gov.hhs.fha.nhinc.admingui.model.StatusSnapshot;
import gov.hhs.fha.nhinc.adminguimanagement.AdminGUIManagementPortType;
import gov.hhs.fha.nhinc.common.adminguimanagement.AdminGUIRequestMessageType;
import gov.hhs.fha.nhinc.common.adminguimanagement.DashboardStatusMessageType;
import gov.hhs.fha.nhinc.common.adminguimanagement.EventLogMessageType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.configuration.AdminGUIManagementPortDescriptor;
import gov.hhs.fha.nhinc.event.model.EventCount;
import gov.hhs.fha.nhinc.exchangemgr.ExchangeManagerException;
import gov.hhs.fha.nhinc.messaging.client.CONNECTClient;
import gov.hhs.fha.nhinc.messaging.client.CONNECTClientFactory;
import gov.hhs.fha.nhinc.messaging.service.port.ServicePortDescriptor;
import gov.hhs.fha.nhinc.webserviceproxy.WebServiceProxyHelper;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DashboardStatusServiceImpl {
    private static final Logger LOG = LoggerFactory.getLogger(DashboardStatusServiceImpl.class);
    private static final WebServiceProxyHelper oProxyHelper = new WebServiceProxyHelper();
    private static CONNECTClient<AdminGUIManagementPortType> client = null;
    private static final String UNKNOWN = "Unknown";

    public StatusSnapshot getStatus() {
        return getStatus(true);
     }

    public StatusSnapshot getStatus(boolean refreshMessages) {

        AdminGUIRequestMessageType request = new AdminGUIRequestMessageType();
        request.setConfigAssertion(buildConfigAssertion());
        request.setIncludeEventMessages(refreshMessages);

        DashboardStatusMessageType response;
        try {
            response = (DashboardStatusMessageType) clientInvokePort("dashboardStatus", request);

        } catch (Exception e) {
            LOG.error("Unable to get dashboard status: {}", e.getLocalizedMessage(), e);
            response = new DashboardStatusMessageType();
            response.setMemory(UNKNOWN);
            response.setOS(UNKNOWN);
            response.setServer(UNKNOWN);
            response.setVersion(UNKNOWN);
        }
        return convertToSnapshot(response);
    }

    private static StatusSnapshot convertToSnapshot(DashboardStatusMessageType response) {
        StatusSnapshot snapshot = new StatusSnapshot();
        HashMap<String, EventCount> events = new HashMap<>();
        for (EventLogMessageType event : response.getEvent()) {
            events.put(event.getEvent(), new EventCount(event.getEvent(), event.getInbound(), event.getOutbound()));
        }
        snapshot.setOs(response.getOS());
        snapshot.setMemory(response.getMemory());
        snapshot.setJavaVersion(response.getVersion());
        snapshot.setServerVersion(response.getServer());
        snapshot.setEvents(events);
        return snapshot;
    }

    private static CONNECTClient<AdminGUIManagementPortType> getClient() throws ExchangeManagerException {
        if (null == client) {
            String url = oProxyHelper.getAdapterEndPointFromConnectionManager(ADMIN_GUI_MANAGEMENT_SERVICE_NAME);
            ServicePortDescriptor<AdminGUIManagementPortType> portDescriptor = new AdminGUIManagementPortDescriptor();
            client = CONNECTClientFactory.getInstance().getCONNECTClientUnsecured(portDescriptor, url, new AssertionType());
        }
        return client;
    }

    private static <T> Object clientInvokePort(String serviceName, T request) throws Exception {
        return getClient().invokePort(AdminGUIManagementPortType.class, serviceName, request);
    }
}
