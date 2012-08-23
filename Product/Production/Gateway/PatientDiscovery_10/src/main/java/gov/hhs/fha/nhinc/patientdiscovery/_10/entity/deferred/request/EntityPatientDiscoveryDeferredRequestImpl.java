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
package gov.hhs.fha.nhinc.patientdiscovery._10.entity.deferred.request;

import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetCommunitiesType;
import gov.hhs.fha.nhinc.cxf.extraction.SAML2AssertionExtractor;
import gov.hhs.fha.nhinc.generic.GenericFactory;
import gov.hhs.fha.nhinc.patientdiscovery.entity.deferred.request.EntityPatientDiscoveryDeferredRequestOrch;
import gov.hhs.fha.nhinc.patientdiscovery.entity.deferred.request.EntityPatientDiscoveryDeferredRequestOrchFactory;

import javax.xml.ws.WebServiceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.v3.MCCIIN000002UV01;
import org.hl7.v3.PRPAIN201305UV02;
import org.hl7.v3.RespondingGatewayPRPAIN201305UV02RequestType;
import org.hl7.v3.RespondingGatewayPRPAIN201305UV02SecuredRequestType;

public class EntityPatientDiscoveryDeferredRequestImpl {

    private Log log = null;
    private GenericFactory<EntityPatientDiscoveryDeferredRequestOrch> orchestrationFactory;

    public EntityPatientDiscoveryDeferredRequestImpl() {
        log = createLogger();
        orchestrationFactory = new EntityPatientDiscoveryDeferredRequestOrchFactory();
        
    }

    protected Log createLogger() {
        return ((log != null) ? log : LogFactory.getLog(getClass()));
    }

    
    public MCCIIN000002UV01 processPatientDiscoveryAsyncRequestSecured(
            RespondingGatewayPRPAIN201305UV02SecuredRequestType request, WebServiceContext context) {
        log.info("Begin processPatientDiscoveryAsyncRequestSecured(RespondingGatewayPRPAIN201305UV02SecuredRequestType, WebServiceContext)");
        EntityPatientDiscoveryDeferredRequestOrch orchestration = orchestrationFactory.create();
        MCCIIN000002UV01 response = null;

        try {
            if (request != null) {
                PRPAIN201305UV02 msg = request.getPRPAIN201305UV02();
                NhinTargetCommunitiesType targets = request.getNhinTargetCommunities();
                response = orchestration.processPatientDiscoveryAsyncReq(msg, extractAssertion(context), targets);
          } else {
                log.error("Failed to call the web orchestration (" + orchestration.getClass()
                        + ".processPatientDiscoveryAsyncReq).  The input parameter is null.");
            }
        } catch (Exception e) {
            log.error(
                    "Failed to call the web orchestration (" + orchestration.getClass()
                            + ".processPatientDiscoveryAsyncReq).  An unexpected exception occurred.  " + "Exception: "
                            + e.getMessage(), e);
        }
        return response;
    }

    public MCCIIN000002UV01 processPatientDiscoveryAsyncRequestUnsecured(
            RespondingGatewayPRPAIN201305UV02RequestType request, WebServiceContext context) {
        log.info("Begin processPatientDiscoveryAsyncRequestUnsecured(RespondingGatewayPRPAIN201305UV02RequestType, WebServiceContext)");
        EntityPatientDiscoveryDeferredRequestOrch orchestration = orchestrationFactory.create();
        MCCIIN000002UV01 response = null;

        try {
            if (request != null) {
                PRPAIN201305UV02 msg = request.getPRPAIN201305UV02();
                NhinTargetCommunitiesType targets = request.getNhinTargetCommunities();
                AssertionType assertIn = request.getAssertion();
                response = orchestration.processPatientDiscoveryAsyncReq(msg, assertIn, targets);
                        
            } else {
                log.error("Failed to call the web orchestration (" + orchestration.getClass()
                        + ".processPatientDiscoveryAsyncReq).  The input parameter is null.");
            }
        } catch (Exception e) {
            log.error(
                    "Failed to call the web orchestration (" + orchestration.getClass()
                            + ".processPatientDiscoveryAsyncReq).  An unexpected exception occurred.  " + "Exception: "
                            + e.getMessage(), e);
        }
        return response;
    }
    
    protected AssertionType extractAssertion(WebServiceContext context) {
        return new SAML2AssertionExtractor().extractSamlAssertion(context);
    }
}
