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
package gov.hhs.fha.nhinc.patientlocationquery.inbound;

import static java.lang.Boolean.FALSE;

import gov.hhs.fha.nhinc.aspect.InboundProcessingEvent;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.event.DefaultTargetEventDescriptionBuilder;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.patientlocationquery.adapter.proxy.AdapterPatientLocationQueryProxyObjectFactory;
import gov.hhs.fha.nhinc.patientlocationquery.audit.PatientLocationQueryAuditLogger;
import ihe.iti.xcpd._2009.PatientLocationQueryRequestType;
import ihe.iti.xcpd._2009.PatientLocationQueryResponseType;
import java.util.Properties;

/**
 *
 * @author tjafri
 */
public class PassthroughInboundPatientLocationQuery implements InboundPatientLocationQuery {

    PatientLocationQueryAuditLogger auditLogger = new PatientLocationQueryAuditLogger();

    @InboundProcessingEvent(beforeBuilder = DefaultTargetEventDescriptionBuilder.class,
        afterReturningBuilder = DefaultTargetEventDescriptionBuilder.class,
        serviceType = "Patient Location Query", version = "1.0")
    @Override
    public PatientLocationQueryResponseType processPatientLocationQuery(PatientLocationQueryRequestType request,
        AssertionType assertion, Properties webContextproperties) {

        PatientLocationQueryResponseType response =  sendToAdapter(request, assertion);
        auditResponse(request,response, assertion, webContextproperties);
        return response;
    }

    protected PatientLocationQueryResponseType sendToAdapter(PatientLocationQueryRequestType request,
        AssertionType assertion) {
        return new AdapterPatientLocationQueryProxyObjectFactory().getAdapterPatientLocationQueryProxy()
            .adapterPatientLocationQueryResponse(request, assertion);
    }

    protected void auditResponse(PatientLocationQueryRequestType request, PatientLocationQueryResponseType response,
        AssertionType assertion, Properties webContextProperties) {
        auditLogger.auditResponseMessage(request, response, assertion, null, NhincConstants.AUDIT_LOG_INBOUND_DIRECTION,
            NhincConstants.AUDIT_LOG_NHIN_INTERFACE, FALSE, webContextProperties, NhincConstants.PLQ_NHIN_SERVICE_NAME);
    }
}