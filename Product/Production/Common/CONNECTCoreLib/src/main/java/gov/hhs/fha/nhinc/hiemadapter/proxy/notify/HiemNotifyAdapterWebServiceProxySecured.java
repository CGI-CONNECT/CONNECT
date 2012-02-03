/*
 * Copyright (c) 2012, United States Government, as represented by the Secretary of Health and Human Services. 
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
package gov.hhs.fha.nhinc.hiemadapter.proxy.notify;

import com.sun.xml.ws.developer.WSBindingProvider;
import gov.hhs.fha.nhinc.adapternotificationconsumersecured.AdapterNotificationConsumerSecured;
import gov.hhs.fha.nhinc.adapternotificationconsumersecured.AdapterNotificationConsumerPortSecureType;
import gov.hhs.fha.nhinc.common.nhinccommon.AcknowledgementType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetSystemType;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.NotifyRequestType;
import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerCache;
import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerException;
import gov.hhs.fha.nhinc.hiem.consumerreference.ReferenceParametersElements;
import gov.hhs.fha.nhinc.hiem.dte.SoapUtil;
import gov.hhs.fha.nhinc.hiem.dte.marshallers.NhincCommonAcknowledgementMarshaller;

import gov.hhs.fha.nhinc.hiem.dte.marshallers.WsntSubscribeMarshaller;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.w3c.dom.*;
import gov.hhs.fha.nhinc.saml.extraction.SamlTokenCreator;
import java.util.Map;
import javax.xml.ws.BindingProvider;


/**
 *
 * @author Jon Hoppesch
 */
public class HiemNotifyAdapterWebServiceProxySecured implements HiemNotifyAdapterProxy {

    private static Log log = LogFactory.getLog(HiemNotifyAdapterWebServiceProxy.class);
    static AdapterNotificationConsumerSecured adapterNotifyService = new AdapterNotificationConsumerSecured();

    public Element notify(Element notifyElement, ReferenceParametersElements referenceParametersElements, AssertionType assertion, NhinTargetSystemType target) throws Exception {
        Element responseElement = null;
        AcknowledgementType response = null;

        log.debug("start secured notify");

        String url = getUrl();
        AdapterNotificationConsumerPortSecureType port = getPort(url);

        WsntSubscribeMarshaller subscribeMarshaller = new WsntSubscribeMarshaller();
        Notify notify = subscribeMarshaller.unmarshalNotifyRequest(notifyElement);

        SamlTokenCreator tokenCreator = new SamlTokenCreator();
        Map requestContext = tokenCreator.CreateRequestContext(assertion, url, NhincConstants.HIEM_NOTIFY_ENTITY_SERVICE_NAME_SECURED);
        ((BindingProvider) port).getRequestContext().putAll(requestContext);

        log.debug("attaching reference parameter headers");
        SoapUtil soapUtil = new SoapUtil();
        soapUtil.attachReferenceParameterElements((WSBindingProvider) port, referenceParametersElements);

        response = port.notify(notify);

        NhincCommonAcknowledgementMarshaller acknowledgementMarshaller = new NhincCommonAcknowledgementMarshaller();
        responseElement = acknowledgementMarshaller.marshal(response);

        log.debug("end secured notify");

        return responseElement;
    }

    public Element notifySubscribersOfDocument(Element docNotify, AssertionType assertion, NhinTargetSystemType target) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Element notifySubscribersOfCdcBioPackage(Element cdcNotify, AssertionType assertion, NhinTargetSystemType target) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private AdapterNotificationConsumerPortSecureType getPort(String url) {

        AdapterNotificationConsumerPortSecureType port = adapterNotifyService.getAdapterNotificationConsumerPortSecureType();

        log.info("Setting endpoint address to Adapter Hiem Notify Service Secured to " + url);
        ((javax.xml.ws.BindingProvider) port).getRequestContext().put(javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);

        return port;
    }
    private String getUrl()
    {
        String url = "";
        try
        {
            url = ConnectionManagerCache.getInstance().getLocalEndpointURLByServiceName(NhincConstants.HIEM_NOTIFY_ADAPTER_SERVICE_NAME);
        } 
        catch (ConnectionManagerException ex)
        {
            log.error("Error: Failed to retrieve url for service: " + NhincConstants.HIEM_NOTIFY_ADAPTER_SERVICE_NAME + " for local home community");
            log.error(ex.getMessage());
        }

        return url;
    }
}
