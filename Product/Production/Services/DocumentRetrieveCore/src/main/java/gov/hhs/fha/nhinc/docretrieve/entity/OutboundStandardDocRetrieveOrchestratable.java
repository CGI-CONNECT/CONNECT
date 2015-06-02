/*
 * Copyright (c) 2009-2015, United States Government, as represented by the Secretary of Health and Human Services.
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
package gov.hhs.fha.nhinc.docretrieve.entity;

import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetSystemType;
import gov.hhs.fha.nhinc.orchestration.AuditTransformer;
import gov.hhs.fha.nhinc.orchestration.NhinAggregator;
import gov.hhs.fha.nhinc.orchestration.OutboundDelegate;
import gov.hhs.fha.nhinc.orchestration.PolicyTransformer;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType;

/**
 *
 * @author mweaver
 */
public class OutboundStandardDocRetrieveOrchestratable extends OutboundDocRetrieveOrchestratable {

    /**
     * Constructor.
     */
    public OutboundStandardDocRetrieveOrchestratable() {

    }

    /**
     * Constructor.
     *
     * @param pt
     * @param at
     * @param nd
     * @param na
     */
    public OutboundStandardDocRetrieveOrchestratable(PolicyTransformer pt, AuditTransformer at, OutboundDelegate nd,
            NhinAggregator na) {
        super(pt, at, nd, na);
    }

    /**
     * Constructor.
     *
     * @param pt
     * @param at
     * @param nd
     * @param na
     * @param body
     * @param assertion
     * @param target
     */
    public OutboundStandardDocRetrieveOrchestratable(PolicyTransformer pt, AuditTransformer at, OutboundDelegate nd,
            NhinAggregator na, RetrieveDocumentSetRequestType body, AssertionType assertion, NhinTargetSystemType target) {
        super(pt, at, nd, na, body, assertion, target);
    }

    /*
     * (non-Javadoc)
     *
     * @see gov.hhs.fha.nhinc.orchestration.Orchestratable#isPassthru()
     */
    @Override
    final public boolean isPassthru() {
        return false;
    }

    /*
     * (non-Javadoc)
     *
     * @see gov.hhs.fha.nhinc.docretrieve.entity.OutboundDocRetrieveOrchestratable#create()
     */
    @Override
    public OutboundDocRetrieveOrchestratable create(PolicyTransformer pt, AuditTransformer at, OutboundDelegate nd,
            NhinAggregator na) {
        return new OutboundStandardDocRetrieveOrchestratable(pt, at, nd, na);
    }
}
