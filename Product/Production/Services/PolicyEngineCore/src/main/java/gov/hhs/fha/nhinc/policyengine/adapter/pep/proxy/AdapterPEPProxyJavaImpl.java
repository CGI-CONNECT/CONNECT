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
package gov.hhs.fha.nhinc.policyengine.adapter.pep.proxy;

import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.CheckPolicyRequestType;
import gov.hhs.fha.nhinc.common.nhinccommonadapter.CheckPolicyResponseType;
import gov.hhs.fha.nhinc.policyengine.adapter.pep.AdapterPEPImpl;

import org.apache.log4j.Logger;

/**
 * This is the concrete implementation for the Java based call to the AdapterPEP.
 */
public class AdapterPEPProxyJavaImpl implements AdapterPEPProxy {
    private static final Logger LOG = Logger.getLogger(AdapterPEPProxyJavaImpl.class);

    /**
     * Given a request to check the access policy, this service will interface with the PDP to determine if access is to
     * be granted or denied.
     * 
     * @param checkPolicyRequest The xacml request to check defined policy
     * @return The xacml response which contains the access decision
     */
    public CheckPolicyResponseType checkPolicy(CheckPolicyRequestType checkPolicyRequest, AssertionType assertion) {
        LOG.trace("Begin AdapterPEPProxyJavaImpl.checkPolicy");
        CheckPolicyResponseType checkPolicyResponse = new CheckPolicyResponseType();

        AdapterPEPImpl pepImpl = new AdapterPEPImpl();

        try {
            checkPolicyResponse = pepImpl.checkPolicy(checkPolicyRequest, assertion);
        } catch (Exception ex) {
            String message = "Error occurred calling AdapterPEPProxyJavaImpl.checkPolicy.  Error: " + ex.getMessage();
            LOG.error(message, ex);
            throw new RuntimeException(message, ex);
        }

        LOG.trace("End AdapterPEPProxyJavaImpl.checkPolicy");
        return checkPolicyResponse;
    }
}