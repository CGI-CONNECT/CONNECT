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
package gov.hhs.fha.nhinc.adapter.commondatalayer;

import gov.hhs.fha.nhinc.adapter.commondatalayer.mappers.*;
import org.hl7.v3.PatientDemographicsPRPAIN201307UV02RequestType;
import org.hl7.v3.PatientDemographicsPRPAMT201303UV02ResponseType;

/**
 *
 * @author kim
 */
public class AdapterCommonDataLayerImpl {

   private static AdapterCommonDataLayerImpl instance = null;

   public static AdapterCommonDataLayerImpl getInstance() {
      synchronized (AdapterCommonDataLayerImpl.class) {
         if (instance == null) {
            instance = new AdapterCommonDataLayerImpl();
         }
      }

      return instance;
   }

   public PatientDemographicsPRPAMT201303UV02ResponseType getPatienInfo(PatientDemographicsPRPAIN201307UV02RequestType request) {
      return StaticPatientDemographicsQuery.createPatientDemographicsResponse(request);
   }

   public org.hl7.v3.CareRecordQUPCIN043200UV01ResponseType getMedications(org.hl7.v3.CareRecordQUPCIN043100UV01RequestType param0) {
      return StaticMedicationsQuery.createMedicationsResponse(param0);
   }

   public org.hl7.v3.CareRecordQUPCIN043200UV01ResponseType getTestResults(org.hl7.v3.CareRecordQUPCIN043100UV01RequestType param0) {
      return StaticTestResultsQuery.createTestResultsResponse(param0);
   }

   public org.hl7.v3.CareRecordQUPCIN043200UV01ResponseType getAllergies(org.hl7.v3.CareRecordQUPCIN043100UV01RequestType param0) {
      return StaticAllergiesQuery.createAllergiesResponse(param0);
   }

   public org.hl7.v3.CareRecordQUPCIN043200UV01ResponseType getProblems(org.hl7.v3.CareRecordQUPCIN043100UV01RequestType param0) {
      return StaticProblemsQuery.createProblemsResponse(param0);
   }

   public org.hl7.v3.FindPatientsPRPAMT201310UV02ResponseType findPatients(org.hl7.v3.FindPatientsPRPAIN201305UV02RequestType param0) {
      return StaticFindPatientsQuery.createFindPatientsResponse(param0);
   }
}
