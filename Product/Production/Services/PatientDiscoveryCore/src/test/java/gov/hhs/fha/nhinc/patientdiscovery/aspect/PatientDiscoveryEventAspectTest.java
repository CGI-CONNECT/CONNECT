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
package gov.hhs.fha.nhinc.patientdiscovery.aspect;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import gov.hhs.fha.nhinc.event.EventFactory;
import gov.hhs.fha.nhinc.event.EventRecorder;
import gov.hhs.fha.nhinc.event.responder.BeginAdapterDelegationEvent;
import gov.hhs.fha.nhinc.event.responder.BeginInboundMessageEvent;
import gov.hhs.fha.nhinc.event.responder.BeginInboundProcessingEvent;
import gov.hhs.fha.nhinc.event.responder.EndAdapterDelegationEvent;
import gov.hhs.fha.nhinc.event.responder.EndInboundMessageEvent;
import gov.hhs.fha.nhinc.event.responder.EndInboundProcessingEvent;

import org.apache.cxf.jaxws.context.WebServiceContextImpl;
import org.apache.cxf.jaxws.context.WrappedMessageContext;
import org.apache.cxf.message.MessageImpl;
import org.hl7.v3.PRPAIN201305UV02;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test {@link PatientDiscoveryEventAspect}
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/EventFactoryConfig.xml" })
public class PatientDiscoveryEventAspectTest {

    @Autowired
    private EventFactory eventFactory;


    @Before
    public void mockWebserviceConext() {
        // Mock up message context
        MessageImpl msg = new MessageImpl();
        WrappedMessageContext msgCtx = new WrappedMessageContext(msg);
        WebServiceContextImpl.setMessageContext(msgCtx);
    }

    @Test
    public void inboundMessageEvent() {
        EventRecorder mockEventRecorder = mock(EventRecorder.class);
        
        InboundMessageEventAspect aspect = new InboundMessageEventAspect();
        aspect.setEventFactory(eventFactory);
        aspect.setEventRecorder(mockEventRecorder);

        PRPAIN201305UV02 mockPRPAIN201305UV02 = mock(PRPAIN201305UV02.class);
        InOrder order = inOrder(mockEventRecorder);
        
        aspect.beginEvent(mockPRPAIN201305UV02);
        aspect.endEvent(mockPRPAIN201305UV02);

        order.verify(mockEventRecorder).recordEvent(isA(BeginInboundMessageEvent.class));
        order.verify(mockEventRecorder).recordEvent(isA(EndInboundMessageEvent.class));

    }
    
    @Test
    public void adapterDelegationEvent() {
        AdapterDelegationEventAspect aspect = new AdapterDelegationEventAspect();
        
        EventRecorder mockEventRecorder = mock(EventRecorder.class);
        
        aspect.setEventFactory(eventFactory);
        aspect.setEventRecorder(mockEventRecorder);
        
        PRPAIN201305UV02 pRPAIN201305UV02 = mock(PRPAIN201305UV02.class);
        
        aspect.beginEvent(pRPAIN201305UV02);
        aspect.endEvent(pRPAIN201305UV02);
        
        InOrder order = inOrder(mockEventRecorder);
        
        order.verify(mockEventRecorder).recordEvent(isA(BeginAdapterDelegationEvent.class));
        order.verify(mockEventRecorder).recordEvent(isA(EndAdapterDelegationEvent.class));
        
    }
    
    @Test
    public void inboundProcessingEvent() {
        InboundProcessingEventAspect aspect = new InboundProcessingEventAspect();
        
        EventRecorder mockEventRecorder = mock(EventRecorder.class);
        
        aspect.setEventFactory(eventFactory);
        aspect.setEventRecorder(mockEventRecorder);
        
        PRPAIN201305UV02 pRPAIN201305UV02 = mock(PRPAIN201305UV02.class);
        
        InOrder order = inOrder(mockEventRecorder);
        aspect.beginEvent(pRPAIN201305UV02);
        aspect.endEvent(pRPAIN201305UV02);
        
        order.verify(mockEventRecorder).recordEvent(isA(BeginInboundProcessingEvent.class));
        order.verify(mockEventRecorder).recordEvent(isA(EndInboundProcessingEvent.class));
    }


}
