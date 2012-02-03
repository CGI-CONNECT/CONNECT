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
package gov.hhs.fha.nhinc.patientdiscovery.nhin.deferred.request;

import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.patientdiscovery.PatientDiscoveryAuditLogger;
import gov.hhs.fha.nhinc.patientdiscovery.PatientDiscoveryPolicyChecker;
import gov.hhs.fha.nhinc.patientdiscovery.adapter.deferred.request.error.proxy.AdapterPatientDiscoveryDeferredReqErrorProxyObjectFactory;
import gov.hhs.fha.nhinc.patientdiscovery.adapter.deferred.request.proxy.AdapterPatientDiscoveryDeferredReqProxyObjectFactory;
import gov.hhs.fha.nhinc.patientdiscovery.nhin.AbstractServicePropertyAccessor;
import gov.hhs.fha.nhinc.patientdiscovery.nhin.GenericFactory;

/**
 * @author bhumphrey
 * 
 */
public final class NhinPatientDiscoveryDeferredReqOrchFactory implements
		GenericFactory<NhinPatientDiscoveryDeferredReqOrch> {

	private static NhinPatientDiscoveryDeferredReqOrchFactory INSTANCE = new NhinPatientDiscoveryDeferredReqOrchFactory();

	NhinPatientDiscoveryDeferredReqOrchFactory() {

	}

	@Override
	public NhinPatientDiscoveryDeferredReqOrch create() {
		return new NhinPatientDiscoveryDeferredReqOrchImpl(
				new AbstractServicePropertyAccessor() {

					@Override
					protected String getServiceName() {
						return  NhincConstants.NHINC_PATIENT_DISCOVERY_ASYNC_REQ_SERVICE_NAME;
					}

					@Override
					protected String getPassThruName() {
						return  NhincConstants.PATIENT_DISCOVERY_SERVICE_ASYNC_REQ_PASSTHRU_PROPERTY;
					} },
				new PatientDiscoveryAuditLogger(),
		new AdapterPatientDiscoveryDeferredReqProxyObjectFactory(),
		new AdapterPatientDiscoveryDeferredReqErrorProxyObjectFactory(),
		PatientDiscoveryPolicyChecker.getInstance());
	}

	public static NhinPatientDiscoveryDeferredReqOrchFactory getInstance() {
		return INSTANCE;
	}

}
