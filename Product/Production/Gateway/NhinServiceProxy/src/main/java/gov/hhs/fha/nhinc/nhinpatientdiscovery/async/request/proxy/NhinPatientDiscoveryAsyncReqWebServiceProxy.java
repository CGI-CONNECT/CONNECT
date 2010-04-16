/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.hhs.fha.nhinc.nhinpatientdiscovery.async.request.proxy;

import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetSystemType;
import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerCache;
import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerException;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.saml.extraction.SamlTokenCreator;
import ihe.iti.xcpd._2009.RespondingGatewayAsyncReqPortType;
import ihe.iti.xcpd._2009.RespondingGatewayAsyncReqService;
import java.util.Map;
import javax.xml.ws.BindingProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.v3.MCCIIN000002UV01;
import org.hl7.v3.PRPAIN201305UV02;

/**
 *
 * @author Jon Hoppesch
 */
public class NhinPatientDiscoveryAsyncReqWebServiceProxy implements NhinPatientDiscoveryAsyncReqProxy {
    private static Log log = LogFactory.getLog(NhinPatientDiscoveryAsyncReqWebServiceProxy.class);
    static RespondingGatewayAsyncReqService nhinService = new RespondingGatewayAsyncReqService();

    public MCCIIN000002UV01 respondingGatewayPRPAIN201305UV02(PRPAIN201305UV02 body, AssertionType assertion, NhinTargetSystemType target) {
        String url = null;
        MCCIIN000002UV01 response = new MCCIIN000002UV01();

        // Get the URL to the Nhin Subject Discovery Service
        url = getUrl(target);

        if (NullChecker.isNotNullish(url)) {
            RespondingGatewayAsyncReqPortType port = getPort(url);

            SamlTokenCreator tokenCreator = new SamlTokenCreator();
            Map requestContext = tokenCreator.CreateRequestContext(assertion, url, NhincConstants.PATIENT_DISCOVERY_ACTION);

            ((BindingProvider) port).getRequestContext().putAll(requestContext);

            response = port.respondingGatewayPRPAIN201305UV02(body);

        } else {
            log.error("The URL for service: " + NhincConstants.PATIENT_DISCOVERY_ASYNC_REQ_SERVICE_NAME + " is null");
        }

        return response;
    }

    private RespondingGatewayAsyncReqPortType getPort(String url) {
        RespondingGatewayAsyncReqPortType port = nhinService.getRespondingGatewayAsyncReqPort();

        log.info("Setting endpoint address to Nhin Patient Discovery Service to " + url);
        ((BindingProvider) port).getRequestContext().put(javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY, url);

        return port;
    }

    private String getUrl(NhinTargetSystemType target) {
        String url = null;

        if (target != null) {
            try {
                url = ConnectionManagerCache.getEndpontURLFromNhinTarget(target, NhincConstants.PATIENT_DISCOVERY_ASYNC_REQ_SERVICE_NAME);
            } catch (ConnectionManagerException ex) {
                log.error("Error: Failed to retrieve url for service: " + NhincConstants.PATIENT_DISCOVERY_ASYNC_REQ_SERVICE_NAME);
                log.error(ex.getMessage());
            }
        } else {
            log.error("Target system passed into the proxy is null");
        }

        return url;
    }

}
