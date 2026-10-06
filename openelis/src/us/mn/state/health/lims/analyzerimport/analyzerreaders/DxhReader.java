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
 *
 * Contributor(s): CIRG, University of Washington, Seattle WA.
 */
package us.mn.state.health.lims.analyzerimport.analyzerreaders;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.validator.GenericValidator;
import org.hibernate.Transaction;

import us.mn.state.health.lims.analyzerimport.util.AnalyzerTestNameCache;
import us.mn.state.health.lims.analyzerimport.util.MappedTestName;
import us.mn.state.health.lims.analyzerimport.util.AnalyzerTestNameCache.AnalyzerType;
import us.mn.state.health.lims.analyzerresults.dao.AnalyzerResultsDAO;
import us.mn.state.health.lims.analyzerresults.valueholder.AnalyzerResults;
import us.mn.state.health.lims.common.exception.LIMSRuntimeException;
import us.mn.state.health.lims.common.util.DAOImplFactory;
import us.mn.state.health.lims.common.util.DateUtil;
import us.mn.state.health.lims.common.util.HibernateProxy;

import us.mn.state.health.lims.sample.dao.SampleDAO;
import us.mn.state.health.lims.sample.daoimpl.SampleDAOImpl;
import us.mn.state.health.lims.sample.valueholder.Sample;

@SuppressWarnings("unused")
public class DxhReader extends AnalyzerLineInserter {

    private static final String CONTROL_ACCESSION_PREFIX = "QC-";

    private static int index = 0;
    private static final int ID_INSTRUMENT = index++;
    private static final int DATE = index++;
    private static final int TIME = index++;
    private static final int ACCESSION = index++;
    private static final int WBC = index++;

    private static final int LYMPH = index++;
    private static final int RBC = index++;
    private static final int PLT = index++;
    private static final int HGB = index++;
    private static final int MON_PER = index++;
    private static final int NEU = index++;
    private static final int BAS_PER = index++;
    private static final int MCV = index++;
    private static final int MCH = index++;
    private static final int MCHC = index++;
    private static final int LYM = index++;
    private static final int EO_PER = index++;
    private static final int BAS = index++;
    private static final int HCT = index++;
    private static final int EO = index++;


    private static final int columns = index++;

    private static final String DELIMITER = ",";

    private static final String DATE_PATTERN = "dd/MM/yyyy HH:mm:ss";
    private static String[] testNameIndex = new String[columns];
    private static String[] unitsIndex = new String[columns];
    private static Boolean[] readOnlyIndex = new Boolean[columns];
    private static int[] scaleIndex = new int[columns];
    private static int[] orderedTestIndexs = new int[19];

    {

        testNameIndex[WBC] = "WBC";
        testNameIndex[LYMPH] = "LYMPH";
        testNameIndex[RBC] = "RBC";
        testNameIndex[PLT] = "PLT";
        testNameIndex[HGB] = "HGB";
        testNameIndex[MON_PER] = "MON%";
        testNameIndex[NEU] = "NEU";
        testNameIndex[BAS_PER] = "BAS%";
        testNameIndex[MCV] = "MCV";
        testNameIndex[MCH] = "MCH";
        testNameIndex[MCHC] = "MCHC";
        testNameIndex[LYM] = "LYM";
        testNameIndex[EO_PER] = "EO%";
        testNameIndex[BAS] = "BAS";
        testNameIndex[HCT] = "HCT";
        testNameIndex[EO] = "EO";


        unitsIndex[WBC] = "10^3/ul";
        unitsIndex[RBC] = "10^6/ul";
        unitsIndex[LYMPH] = "10^6/ul";
        unitsIndex[PLT] = "10^3/ul";
        unitsIndex[HGB] = "g/dl";
        unitsIndex[MON_PER] = "10^3/ul";
        unitsIndex[NEU] = "10^3/ul";
        unitsIndex[BAS_PER] = "%";
        unitsIndex[MCV] = "fl";
        unitsIndex[MCH] = "pg";
        unitsIndex[MCHC] = "g/dl";
        unitsIndex[LYM] = "10^3/ul";
        unitsIndex[EO_PER] = "%";
        unitsIndex[BAS] = "%";
        unitsIndex[HCT] = "%";
        unitsIndex[EO] = "10^3/ul";


/*		unitsIndex[PDWsd] = " ";
        unitsIndex[PDWcv] = " ";
        unitsIndex[MPV] = " ";
        unitsIndex[PCT] = " ";
        unitsIndex[PLCR] = " ";
        unitsIndex[PLCC] = " "; */

        for( int i = 0; i < readOnlyIndex.length; i++){
            readOnlyIndex[i] = Boolean.FALSE;
        }
/*
		readOnlyIndex[NEUT_COUNT_10_uL] = Boolean.TRUE;
		readOnlyIndex[MONO_COUNT_10_uL] = Boolean.TRUE;
		readOnlyIndex[BASO_COUNT_10_uL] = Boolean.TRUE;
		readOnlyIndex[LYMPH_COUNT_10_uL] = Boolean.TRUE;
		readOnlyIndex[EO_COUNT_10_uL] = Boolean.TRUE;
*/
        scaleIndex[WBC] = 1;
        scaleIndex[LYMPH] = 1;
        scaleIndex[RBC] = 1;
        scaleIndex[PLT] = 1;
        scaleIndex[HGB] = 1;
        scaleIndex[MON_PER] = 1;
        scaleIndex[NEU] = 1;
        scaleIndex[BAS_PER] = 1;
        scaleIndex[MCV] = 1;
        scaleIndex[MCH] = 1;
        scaleIndex[MCHC] = 1;
        scaleIndex[LYM] = 1;
        scaleIndex[EO_PER] = 1;
        scaleIndex[BAS] = 1;
        scaleIndex[HCT] = 1;
        scaleIndex[EO] = 1;

		/*scaleIndex[PDWsd] = 1;
        scaleIndex[PDWcv] = 1;
        scaleIndex[MPV] = 1;
        scaleIndex[PCT] = 1;
        scaleIndex[PLCR] = 1;
        scaleIndex[PLCC] = 1; */



		/*
		GB_10_uL              WBC   100
		GR_100000_uL          RBC   100
		NEUT_PER_10_NEG_1_PER NE%    10
		HBG_g_L               HGB    10
		LYMPH_PER_10_NEG_1_PER LY%   10
		HCT_10_NEG_1_PER      HCT    10
		MONO_PER_10_NEG_1_PER MO%    10
		VGM_10_NEG_1_fL       MVC    10
		EO_PER_10_NEG_1_PER   EO%    10
		TCMH_10_NEG_1_pg      MCH    10
		BASO_PER_10_NEG_1_PER BA%    10
		CCMH_g_L              MCHC   10
		PLQ_10_3_uL           PLT     1

		NEUT_COUNT_10_uL      NE#   100
		MONO_COUNT_10_uL      MO#   100
		BASO_COUNT_10_uL      BA#    10
		LYMPH_COUNT_10_uL     LY#   100
		EO_COUNT_10_uL        EO#   100
		IDR_SD_10_NEG_1_FL    Platelet Count
		IDR_CV_10_NEG_1_PER	  ESR
		 */
    }

    private SampleDAO sampleDAO = new SampleDAOImpl();

    public boolean insert(List<String> lines, String currentUserId) {
        boolean successful = true;

        List<AnalyzerResults> results = new ArrayList<AnalyzerResults>();

        for (int i = 1; i < lines.size(); i++) {
            addAnalyzerResultFromLine(results, lines);
        }

        if (results.size() > 0) {

            Transaction tx = HibernateProxy.beginTransaction();

            try {

                persistResults(results, currentUserId);

                tx.commit();

            } catch (LIMSRuntimeException lre) {
                tx.rollback();
                successful = false;
            } finally {
                HibernateProxy.closeSession();
            }
        }

        return successful;
    }

    private void addAnalyzerResultFromLine(List<AnalyzerResults> results, List<String> lines) {
        String[] header = lines.get(0).split(DELIMITER);
        String[] fields = lines.get(1).split(DELIMITER);
        String analyzerAccessionNumber = fields[ACCESSION];

        // Check if sample already exists
        Sample sample = sampleDAO.getSampleByAccessionNumber(analyzerAccessionNumber);
        if( sample == null)
            return;

        AnalyzerReaderUtil readerUtil = new AnalyzerReaderUtil();

        Timestamp timestamp = DateUtil.convertStringDateToTimestampWithPattern(fields[DATE] + " " + fields[TIME], DATE_PATTERN);

        List<AnalyzerResults> readOnlyResults = new ArrayList<AnalyzerResults>();

        //the reason for the indirection is to get the order of tests correct
        for (int i = 4; i < header.length; i++) {
            String testName = header[i];
            String testResult = fields[i];
            if (!GenericValidator.isBlankOrNull(header[i])) {
                MappedTestName mappedName = AnalyzerTestNameCache.instance().getMappedTest(AnalyzerType.Dxh_800, testName);

                if( mappedName == null){
                    mappedName = AnalyzerTestNameCache.instance().getEmptyMappedTestName(AnalyzerType.Dxh_800, testName);
                }

                AnalyzerResults analyzerResults = new AnalyzerResults();

                analyzerResults.setAnalyzerId(mappedName.getAnalyzerId());

                double result = Double.NaN;

                try{
                    result = Double.parseDouble(testResult)/ scaleIndex[ArrayUtils.indexOf(testNameIndex, "WBC")];
                }catch( NumberFormatException nfe){
                    //no-op -- defaults to NAN
                }

                analyzerResults.setResult(String.valueOf(result));
                analyzerResults.setUnits(unitsIndex[ArrayUtils.indexOf(testNameIndex, "WBC")]);
                analyzerResults.setCompleteDate(timestamp);
                analyzerResults.setTestId(mappedName.getTestId());
                analyzerResults.setAccessionNumber(analyzerAccessionNumber);
                analyzerResults.setTestName(mappedName.getOpenElisTestName());
                analyzerResults.setReadOnly(readOnlyIndex[ArrayUtils.indexOf(testNameIndex, "WBC")]);

                if (analyzerAccessionNumber != null) {
                    analyzerResults.setIsControl(analyzerAccessionNumber.startsWith(CONTROL_ACCESSION_PREFIX));
                } else {
                    analyzerResults.setIsControl(false);
                }

                if( analyzerResults.isReadOnly()){
                    readOnlyResults.add(analyzerResults);
                }else{
                    results.add(analyzerResults);
                }

                AnalyzerResults resultFromDB = readerUtil.createAnalyzerResultFromDB(analyzerResults);
                if( resultFromDB != null){
                    results.add(resultFromDB);
                }
            }
        }

        results.addAll(readOnlyResults);
    }

    @Override
    public String getError() {
        return "Sysmex analyzer unable to write to database";
    }

}
