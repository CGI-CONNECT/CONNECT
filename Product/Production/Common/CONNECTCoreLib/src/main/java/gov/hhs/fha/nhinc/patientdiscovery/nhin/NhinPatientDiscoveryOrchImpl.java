/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *  
 * Copyright 2010(Year date of delivery) United States Government, as represented by the Secretary of Health and Human Services.  All rights reserved.
 *  
 */
package gov.hhs.fha.nhinc.patientdiscovery.nhin;

import gov.hhs.fha.nhinc.common.nhinccommon.AcknowledgementType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.patientdiscovery.PatientDiscovery201305Processor;
import gov.hhs.fha.nhinc.patientdiscovery.PatientDiscoveryAdapterSender;
import gov.hhs.fha.nhinc.patientdiscovery.PatientDiscoveryAuditor;
import gov.hhs.fha.nhinc.patientdiscovery.PatientDiscoveryProcessor;
import gov.hhs.fha.nhinc.patientdiscovery.adapter.proxy.AdapterPatientDiscoveryProxy;
import gov.hhs.fha.nhinc.patientdiscovery.adapter.proxy.AdapterPatientDiscoveryProxyObjectFactory;
import gov.hhs.fha.nhinc.perfrepo.PerformanceManager;
import gov.hhs.fha.nhinc.properties.ServicePropertyAccessor;
import gov.hhs.fha.nhinc.util.HomeCommunityMap;

import java.sql.Timestamp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.v3.PRPAIN201305UV02;
import org.hl7.v3.PRPAIN201306UV02;
import org.hl7.v3.RespondingGatewayPRPAIN201305UV02RequestType;

/**
 * 
 * @author westberg
 */
public class NhinPatientDiscoveryOrchImpl implements NhinPatientDiscoveryOrchestration {

	private static Log log = LogFactory
			.getLog(NhinPatientDiscoveryOrchImpl.class);

	private ServicePropertyAccessor servicePropertyAccessor;

	private PatientDiscoveryAuditor auditLogger;

	private PatientDiscoveryProcessor patientDiscoveryProcessor;

	private GenericFactory<AdapterPatientDiscoveryProxy> proxyFactory;
	
	NhinPatientDiscoveryOrchImpl(ServicePropertyAccessor servicePropertyAccessor, PatientDiscoveryAuditor auditLogger, PatientDiscoveryProcessor patientDiscoveryProcessor, GenericFactory<AdapterPatientDiscoveryProxy> proxyFactory) {
		this.servicePropertyAccessor = servicePropertyAccessor;
		this.auditLogger = auditLogger;
		this.patientDiscoveryProcessor = patientDiscoveryProcessor;
		this.proxyFactory = proxyFactory;
	}

	/* (non-Javadoc)
	 * @see gov.hhs.fha.nhinc.patientdiscovery.nhin.NhinPatientDiscoveryOrchestration#respondingGatewayPRPAIN201305UV02(org.hl7.v3.PRPAIN201305UV02, gov.hhs.fha.nhinc.common.nhinccommon.AssertionType)
	 */
	@Override
	public PRPAIN201306UV02 respondingGatewayPRPAIN201305UV02(
			PRPAIN201305UV02 body, AssertionType assertion) {
		log.debug("Entering NhinPatientDiscoveryImpl.respondingGatewayPRPAIN201305UV02");

		PRPAIN201306UV02 response = new PRPAIN201306UV02();
		AcknowledgementType ack = new AcknowledgementType();

		// Check if the Patient Discovery Service is enabled
		if (isServiceEnabled()) {

			response = auditAndProcess(body, assertion, auditLogger);
		}

		// Send response back to the initiating Gateway
		log.debug("Exiting NhinPatientDiscoveryImpl.respondingGatewayPRPAIN201305UV02");
		return response;
	}

	/**
	 * @param body
	 * @param assertion
	 * @param auditLogger
	 * @return
	 */
	protected PRPAIN201306UV02 auditAndProcess(PRPAIN201305UV02 body,
			AssertionType assertion, PatientDiscoveryAuditor auditLogger) {
		PRPAIN201306UV02 response;
		AcknowledgementType ack;
		// Audit the outgoing Adapter 201305 Message
		ack = auditLogger.auditAdapter201305(body, assertion,
				NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION);

		// Log the start of the adapter performance record
		String homeCommunityId = HomeCommunityMap.getLocalHomeCommunityId();
		Timestamp starttimeAdapter = new Timestamp(
				System.currentTimeMillis());
		Long logAdapterId = PerformanceManager
				.getPerformanceManagerInstance().logPerformanceStart(
						starttimeAdapter,
						NhincConstants.PATIENT_DISCOVERY_SERVICE_NAME,
						NhincConstants.AUDIT_LOG_ADAPTER_INTERFACE,
						NhincConstants.AUDIT_LOG_OUTBOUND_DIRECTION,
						homeCommunityId);

		response = process(body, assertion);

		// Log the end of the adapter performance record
		Timestamp stoptimeAdapter = new Timestamp(
				System.currentTimeMillis());
		PerformanceManager.getPerformanceManagerInstance()
				.logPerformanceStop(logAdapterId, starttimeAdapter,
						stoptimeAdapter);

		// Audit the incoming Adapter 201306 Message - response that came
		// back from the adapter.
		ack = auditLogger.auditAdapter201306(response, assertion,
				NhincConstants.AUDIT_LOG_INBOUND_DIRECTION);
		return response;
	}

	/**
	 * @param body
	 * @param assertion
	 * @return
	 */
	protected PRPAIN201306UV02 process(PRPAIN201305UV02 body,
			AssertionType assertion) {
		PRPAIN201306UV02 response;
		// Check if in Pass-Through Mode
		if (isInPassThroughMode()) {
			response = send201305ToAgency(body, assertion);
		} else {
			response = patientDiscoveryProcessor.process201305(body, assertion);
		}
		return response;
	}

	
	
	
	protected PRPAIN201306UV02 send201305ToAgency(PRPAIN201305UV02 request, AssertionType assertion) {
        AdapterPatientDiscoveryProxy proxy = proxyFactory.create();
        PRPAIN201306UV02 adapterResp = proxy.respondingGatewayPRPAIN201305UV02(request, assertion);
        return adapterResp;
    }
	
	/**
	 * Checks the gateway.properties file to see if the Patient Discovery
	 * Service is enabled.
	 * 
	 * @return Returns true if the servicePatientDiscovery is enabled in the
	 *         properties file.
	 */
	protected boolean isServiceEnabled() {
		return servicePropertyAccessor.isServiceEnabled();
	}

	/**
	 * Checks to see if the query should be handled internally or passed through
	 * to an adapter.
	 * 
	 * @return Returns true if the patientDiscoveryPassthrough property of the
	 *         gateway.properties file is true.
	 */
	protected boolean isInPassThroughMode() {
		return servicePropertyAccessor.isInPassThroughMode();
	}

}
