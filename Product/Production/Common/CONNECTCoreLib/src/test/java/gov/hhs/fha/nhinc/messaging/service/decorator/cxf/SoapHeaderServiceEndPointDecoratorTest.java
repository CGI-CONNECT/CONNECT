/*
 * Copyright (c) 2009-2019, United States Government, as represented by the Secretary of Health and Human Services.
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
package gov.hhs.fha.nhinc.messaging.service.decorator.cxf;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import gov.hhs.fha.nhinc.messaging.service.ServiceEndpoint;
import java.util.Map;
import javax.xml.ws.BindingProvider;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author ttang
 *
 */
public class SoapHeaderServiceEndPointDecoratorTest {
    @Test
    public void testSoapHeaderEndpoint() {
        ServiceEndpoint<BindingProvider> mockService = Mockito.mock(ServiceEndpoint.class);
        BindingProvider mockBinding = Mockito.mock(BindingProvider.class);
        when(mockService.getPort()).thenReturn(mockBinding);
        Map<String, Object> mockMap = Mockito.mock(Map.class);
        when(mockBinding.getRequestContext()).thenReturn(mockMap);

        SoapHeaderServiceEndPointDecorator decorator = new SoapHeaderServiceEndPointDecorator<>(mockService, null,
            "http://deferredEndpoint/", true);
        decorator.configure();

        verify(mockMap, Mockito.times(1)).put(Mockito.any(String.class), Mockito.any(Object.class));
    }

}
