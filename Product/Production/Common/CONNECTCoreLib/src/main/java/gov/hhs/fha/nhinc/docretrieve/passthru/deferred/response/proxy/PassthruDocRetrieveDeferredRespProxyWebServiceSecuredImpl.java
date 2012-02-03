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
package gov.hhs.fha.nhinc.docretrieve.passthru.deferred.response.proxy;

import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetSystemType;
import gov.hhs.fha.nhinc.common.nhinccommonproxy.RespondingGatewayCrossGatewayRetrieveSecuredResponseType;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.nhincproxydocretrievesecuredresponse.NhincProxyDocRetrieveDeferredResponseSecuredPortType;
import gov.hhs.fha.nhinc.webserviceproxy.WebServiceProxyHelper;
import gov.hhs.healthit.nhin.DocRetrieveAcknowledgementType;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

/**
 *
 * @author Sai Valluripalli
 */
public class PassthruDocRetrieveDeferredRespProxyWebServiceSecuredImpl implements PassthruDocRetrieveDeferredRespProxy {

    private Log log = null;
    private boolean debugEnabled = false;
    private WebServiceProxyHelper oProxyHelper = null;
    private static Service cachedService = null;
    private static final String NAMESPACE_URI = "urn:gov:hhs:fha:nhinc:nhincproxydocretrievesecuredresponse";
    private static final String SERVICE_LOCAL_PART = "NhincProxyDocRetrieveDeferredResponseSecured";
    private static final String PORT_LOCAL_PART = "NhincProxyDocRetrieveDeferredResponseSecuredPortSoap";
    private static final String WSDL_FILE = "NhincProxyDocRetrieveDeferredRespSecured.wsdl";
    private static final String WS_ADDRESSING_ACTION = "urn:gov:hhs:fha:nhinc:nhincproxydocretrievesecuredresponse:CrossGatewayRetrieveResponseMessage";

    /**
     * default constructor
     */
    public PassthruDocRetrieveDeferredRespProxyWebServiceSecuredImpl() {
        log = createLogger();
        debugEnabled = log.isDebugEnabled();
        oProxyHelper = createWebServiceProxyHelper();
    }

    /**
     *
     * @return WebServiceProxyHelper
     */
    protected WebServiceProxyHelper createWebServiceProxyHelper() {
        return new WebServiceProxyHelper();
    }

    /**
     * Creates logger instance
     * @return Log
     */
    protected Log createLogger() {
        return (log != null) ? log : LogFactory.getLog(this.getClass());
    }

    /**
     *
     * @param retrieveDocumentSetResponse
     * @param assertion
     * @param target
     * @return DocRetrieveAcknowledgementType
     */
    public DocRetrieveAcknowledgementType crossGatewayRetrieveResponse(RetrieveDocumentSetRequestType request, RetrieveDocumentSetResponseType retrieveDocumentSetResponse, AssertionType assertion, NhinTargetSystemType target) {
        if (debugEnabled) {
            log.debug("-- Begin NhincProxyDocRetrieveDeferredRespSecuredWebServiceImpl.crossGatewayRetrieveResponse() --");
        }
        DocRetrieveAcknowledgementType ack = null;
        String url = null;
        try {
            url = oProxyHelper.getUrlLocalHomeCommunity(NhincConstants.NHINCPROXY_DOCRETRIEVE_DEFERRED_SECURED_RESPONSE);
            NhincProxyDocRetrieveDeferredResponseSecuredPortType port = getPort(url, NhincConstants.DOCRETRIEVE_DEFERRED_ACTION, WS_ADDRESSING_ACTION, assertion);
            RespondingGatewayCrossGatewayRetrieveSecuredResponseType resp = new RespondingGatewayCrossGatewayRetrieveSecuredResponseType();
            resp.setNhinTargetSystem(target);
            resp.setRetrieveDocumentSetResponse(retrieveDocumentSetResponse);
            ack = (DocRetrieveAcknowledgementType) oProxyHelper.invokePort(port, NhincProxyDocRetrieveDeferredResponseSecuredPortType.class, "crossGatewayRetrieveResponse", resp);
        } catch (Exception ex) {
            log.error(ex);
        }
        if (debugEnabled) {
            log.debug("-- End NhincProxyDocRetrieveDeferredRespSecuredWebServiceImpl.crossGatewayRetrieveResponse() --");
        }
        return ack;
    }

    /**
     * 
     * @param url
     * @param serviceAction
     * @param wsAddressingAction
     * @param assertion
     * @return NhincProxyDocRetrieveDeferredResponseSecuredPortType
     */
    private NhincProxyDocRetrieveDeferredResponseSecuredPortType getPort(String url, String serviceAction, String wsAddressingAction, AssertionType assertion) {
        NhincProxyDocRetrieveDeferredResponseSecuredPortType port = null;
        Service service = getService();
        if (service != null) {
            if (debugEnabled) {
                log.debug("Obtained service - creating port.");
            }
            port = service.getPort(new QName(NAMESPACE_URI, PORT_LOCAL_PART), NhincProxyDocRetrieveDeferredResponseSecuredPortType.class);
            oProxyHelper.initializeSecurePort((javax.xml.ws.BindingProvider) port, url, serviceAction, wsAddressingAction, assertion);
        } else {
            log.error("Unable to obtain serivce - no port created.");
        }

        return port;
    }

    /**
     * Retrieve the service class for this web service.
     *
     * @return The service class for this web service.
     */
    protected Service getService() {
        if (cachedService == null) {
            try {
                cachedService = oProxyHelper.createService(WSDL_FILE, NAMESPACE_URI, SERVICE_LOCAL_PART);
            } catch (Throwable t) {
                log.error("Error creating service: " + t.getMessage(), t);
            }
        }
        return cachedService;
    }
}
