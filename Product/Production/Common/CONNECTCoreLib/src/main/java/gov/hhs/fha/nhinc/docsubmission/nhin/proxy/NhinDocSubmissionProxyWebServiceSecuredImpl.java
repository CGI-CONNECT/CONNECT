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
package gov.hhs.fha.nhinc.docsubmission.nhin.proxy;

import java.util.HashMap;
import java.util.Map;

import gov.hhs.fha.nhinc.callback.cxf.CXFPasswordCallbackHandler;
import gov.hhs.fha.nhinc.callback.cxf.CXFSAMLCallbackHandler;
import gov.hhs.fha.nhinc.callback.cxf.CryptoManager;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetSystemType;
import oasis.names.tc.ebxml_regrep.xsd.rs._3.RegistryResponseType;
import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.webserviceproxy.WebServiceProxyHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ihe.iti.xdr._2007.DocumentRepositoryXDRPortType;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

/**
 * 
 * @author dunnek
 */
public class NhinDocSubmissionProxyWebServiceSecuredImpl implements NhinDocSubmissionProxy {
    private Log log = null;
    private static HashMap<String, Service> cachedServiceMap = new HashMap<String, Service>();
    private static final String NAMESPACE_URI = "urn:ihe:iti:xdr:2007";
    private static final String SERVICE_LOCAL_PART = "DocumentRepositoryXDR_Service";
    private static final String PORT_LOCAL_PART = "DocumentRepositoryXDR_Port_Soap";
    private static final String WSDL_FILE_G0 = "NhinXDR.wsdl";
    private static final String WSDL_FILE_G1 = "NhinXDR20.wsdl";
    private static final String WS_ADDRESSING_ACTION_G0 = "urn:ihe:iti:xdr:2007:ProvideAndRegisterDocumentSet-b";
    private static final String WS_ADDRESSING_ACTION_G1 = "urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-b";
    private WebServiceProxyHelper proxyHelper = null;

    public NhinDocSubmissionProxyWebServiceSecuredImpl() {
        log = createLogger();
        proxyHelper = createWebServiceProxyHelper();
    }

    protected Log createLogger() {
        return LogFactory.getLog(getClass());
    }

    protected WebServiceProxyHelper createWebServiceProxyHelper() {
        return new WebServiceProxyHelper();
    }

    protected void initializeSecurePort(DocumentRepositoryXDRPortType port, String url, String wsAddressingAction,
            AssertionType assertion) {
        proxyHelper.initializeSecurePort((javax.xml.ws.BindingProvider) port, url, NhincConstants.XDR_ACTION,
                wsAddressingAction, assertion);
    }

    /**
     * This method retrieves and initializes the port.
     * 
     * @param url The URL for the web service.
     * @return The port object for the web service.
     */
    protected DocumentRepositoryXDRPortType getPort(String url, AssertionType assertion,
            NhincConstants.GATEWAY_API_LEVEL apiLevel) {
        DocumentRepositoryXDRPortType port = null;
        Service service;
        String wsAddressingAction;
        switch (apiLevel) {
        case LEVEL_g0:
            service = getService(WSDL_FILE_G0);
            wsAddressingAction = WS_ADDRESSING_ACTION_G0;
            break;
        case LEVEL_g1:
            service = getService(WSDL_FILE_G1);
            wsAddressingAction = WS_ADDRESSING_ACTION_G1;
            break;
        default:
            service = null;
            wsAddressingAction = null;
        }

        if (service != null) {
            log.debug("Obtained service - creating port.");

            // CXF stuff
            /*
             * this didn't work Map<String, Object> m = new HashMap<String, Object>(); m.put("samlPropFile",
             * "saml.properties"); org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor interceptor = new
             * org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor(m ); org.apache.cxf.endpoint.Client cxfClient =
             * org.apache.cxf.frontend.ClientProxy.getClient(service); cxfClient.getInInterceptors().add(interceptor);
            */ 
            
            //port = service.getPort(new QName(NAMESPACE_URI, PORT_LOCAL_PART), DocumentRepositoryXDRPortType.class);
            
            //org.apache.cxf.endpoint.Client cxfClient = org.apache.cxf.frontend.ClientProxy.getClient(port);
            //log.debug("there are " + cxfClient.getInInterceptors().size() + " interceptors.");
                       
            ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[] { "DocumentSubmission_20-client-beans.xml" });
            port = (DocumentRepositoryXDRPortType)context.getBean("documentSubmissionPortType");
            HTTPConduit httpConduit = (HTTPConduit) ClientProxy.getClient(port).getConduit();
            
            TLSClientParameters tlsCP = new TLSClientParameters();
            //The following is not recommended and would not be done in a prodcution environment,
            //this is just for illustrative purpose
            tlsCP.setDisableCNCheck(true);
     
            httpConduit.setTlsClientParameters(tlsCP);
            Map<String, Object> requestContext = ((BindingProvider) port).getRequestContext();
            //requestContext.put("ws-security.saml-callback-handler", new CXFSAMLCallbackHandler()); 
            //requestContext.put("ws-security.signature.crypto", CryptoManager.class);
            //requestContext.put("ws-security.callback-handler", CXFPasswordCallbackHandler.class);
            /*requestContext.put("ws-security.signature.properties", "keystore.properties");
            requestContext.put(WSHandlerConstants.ACTION, WSHandlerConstants.SAML_TOKEN_SIGNED + " " + WSHandlerConstants.TIMESTAMP);
            requestContext.put(WSHandlerConstants.USER, "gateway");
            requestContext.put(WSHandlerConstants.TTL_TIMESTAMP, "3600");
            requestContext.put(WSHandlerConstants.PASSWORD_TYPE, "PasswordDigest");
            requestContext.put(WSHandlerConstants.PW_CALLBACK_REF, new CXFPasswordCallbackHandler());
            requestContext.put(WSHandlerConstants.SIG_PROP_FILE, "keystore.properties");
            requestContext.put(WSHandlerConstants.SIG_ALGO, "http://www.w3.org/2000/09/xmldsig#rsa-sha1");
            requestContext.put(WSHandlerConstants.SIG_DIGEST_ALGO, "http://www.w3.org/2000/09/xmldsig#sha1");
            requestContext.put(WSHandlerConstants.SIGNATURE_PARTS, "{Element}{http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd}Timestamp;");*/
            initializeSecurePort(port, url, wsAddressingAction, assertion);
        } else {
            log.error("Unable to obtain service - no port created.");
        }
        return port;
    }

    /**
     * Retrieve the service class for this web service.
     * 
     * @return The service class for this web service.
     */
    protected Service getService(String wsdl) {
        Service cachedService = cachedServiceMap.get(wsdl);
        if (cachedService == null) {
            try {
                cachedService = proxyHelper.createService(wsdl, NAMESPACE_URI, SERVICE_LOCAL_PART);
                cachedServiceMap.put(wsdl, cachedService);
            } catch (Throwable t) {
                log.error("Error creating service: " + t.getMessage(), t);
            }
        }
        return cachedService;
    }

    public RegistryResponseType provideAndRegisterDocumentSetB(ProvideAndRegisterDocumentSetRequestType request,
            AssertionType assertion, NhinTargetSystemType targetSystem, NhincConstants.GATEWAY_API_LEVEL apiLevel) {
        log.debug("Begin provideAndRegisterDocumentSetB");
        RegistryResponseType response = new RegistryResponseType();

        try {
            String url = proxyHelper.getUrlFromTargetSystemByGatewayAPILevel(targetSystem,
                    NhincConstants.NHINC_XDR_SERVICE_NAME, apiLevel);
            DocumentRepositoryXDRPortType port = getPort(url, assertion, apiLevel);

            if (request == null) {
                log.error("Message was null");
            } else if (port == null) {
                log.error("port was null");
            } else {
                response = (RegistryResponseType) proxyHelper.invokePort(port, DocumentRepositoryXDRPortType.class,
                        "documentRepositoryProvideAndRegisterDocumentSetB", request);
            }
        } catch (Exception ex) {
            log.error("Error calling documentRepositoryProvideAndRegisterDocumentSetB: " + ex.getMessage(), ex);
        }

        log.debug("End provideAndRegisterDocumentSetB");
        return response;

    }

}
