/*
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations under
 * the License.
 *
 * The Original Code is OpenELIS code.
 *
 * Copyright (C) The Minnesota Department of Health.  All Rights Reserved.
 */
package us.mn.state.health.lims.dashboard.valueholder;

/**
 * Patient and ordered-test summary used when printing sample barcode labels.
 */
public class OrderedTestsLabelData {
    private final String tests;
    private final String patientName;
    private final String gender;
    private final String birthDate;

    public OrderedTestsLabelData(String tests, String patientName, String gender, String birthDate) {
        this.tests = tests;
        this.patientName = patientName;
        this.gender = gender;
        this.birthDate = birthDate;
    }

    public String getTests() {
        return tests;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getGender() {
        return gender;
    }

    public String getBirthDate() {
        return birthDate;
    }
}
