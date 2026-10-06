package us.mn.state.health.lims.analyzerimport.analyzerreaders;

import org.hibernate.Transaction;
import org.json.JSONException;
import org.json.JSONObject;
import us.mn.state.health.lims.analyzerimport.util.AnalyzerTestNameCache;
import us.mn.state.health.lims.analyzerimport.util.MappedTestName;
import us.mn.state.health.lims.analyzerresults.valueholder.AnalyzerResults;
import us.mn.state.health.lims.common.exception.LIMSRuntimeException;
import us.mn.state.health.lims.common.util.HibernateProxy;
import us.mn.state.health.lims.sample.dao.SampleDAO;
import us.mn.state.health.lims.sample.daoimpl.SampleDAOImpl;
import us.mn.state.health.lims.sample.valueholder.Sample;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Dxc700Reader extends AnalyzerLineInserter{
    private static final String DATE_PATTERN = "yy-MM-dd HH:mm";
    private static final String CONTROL_ACCESSION_PREFIX = "QC-";
    private SampleDAO sampleDAO = new SampleDAOImpl();

    public boolean insertResult(JSONObject json)  {

        boolean successful = true;

        List<AnalyzerResults> results = new ArrayList<AnalyzerResults>();

        try {
            addAnalyzerResults(results, json);
        } catch (JSONException | ParseException e) {
            throw new RuntimeException(e);
        }


        if (results.size() > 0) {

            Transaction tx = HibernateProxy.beginTransaction();

            try {

                persistResults(results, "1");

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

    private void addAnalyzerResults(List<AnalyzerResults> results, JSONObject json) throws ParseException, JSONException {
        DateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
        JSONObject testUnits = getTestUnits();
        AnalyzerReaderUtil readerUtil = new AnalyzerReaderUtil();

        //JSONObject json = (JSONObject) json;
        String testName = json.getString("parameterName").toUpperCase();
        //Date dateTime = dateFormat.parse(json.getString("dateTime").replace("T", " "));
        //Timestamp timestamp = new Timestamp(dateTime.getTime());
        Timestamp timestamp = new Timestamp(new Date().getTime());
        String analyzerAccessionNumber = json.getString("sampleId");
        Sample sample = sampleDAO.getSampleByAccessionNumber(analyzerAccessionNumber);
        if( sample == null)
            return;
        String result = json.getString("result");
        AnalyzerResults analyzerResults = new AnalyzerResults();

        MappedTestName mappedName = AnalyzerTestNameCache.instance().getMappedTest(AnalyzerTestNameCache.AnalyzerType.Dxc_700, testName);
        if (mappedName == null) {
            mappedName = AnalyzerTestNameCache.instance().getEmptyMappedTestName(AnalyzerTestNameCache.AnalyzerType.Dxc_700, testName);
        }

        analyzerResults.setAnalyzerId(mappedName.getAnalyzerId());
        if( result.equals("")) {
            return;
        }
        try{
            Double resultNumber = Double.parseDouble(result);
        }catch( NumberFormatException nfe){
            //no-op -- defaults to NAN
            return;
        }
        analyzerResults.setResult(result);
        try {
            analyzerResults.setUnits(testUnits.getString(testName));
        } catch (JSONException e) {
            return;
        }
        analyzerResults.setCompleteDate(timestamp);
        analyzerResults.setTestId(mappedName.getTestId());
        analyzerResults.setAccessionNumber(analyzerAccessionNumber);
        analyzerResults.setTestName(mappedName.getOpenElisTestName());
        analyzerResults.setReadOnly(false);

        if (analyzerAccessionNumber != null) {
            analyzerResults.setIsControl(analyzerAccessionNumber.startsWith(CONTROL_ACCESSION_PREFIX));
        } else {
            analyzerResults.setIsControl(false);
        }

        results.add(analyzerResults);

        AnalyzerResults resultFromDB = readerUtil.createAnalyzerResultFromDB(analyzerResults);
        if (resultFromDB != null) {
            results.add(resultFromDB);
        }

    }

    public JSONObject getTestUnits() throws JSONException {
        JSONObject testUnits = new JSONObject();
        testUnits.put("GLU1N", "mg/dl");
        testUnits.put("UREA", "mg/dl");
        testUnits.put("CRE2N", "mg/dl");
        testUnits.put("AST1N", "U/L");
        testUnits.put("ALT1N", "U/L");
        testUnits.put("ALP1N", "U/L");
        testUnits.put("GGT2N", "U/L");
        testUnits.put("LDH3N", "U/L");
        testUnits.put("TBC1N", "mg/dl");
        testUnits.put("DBC1N", "mg/dl");
        testUnits.put("TP-1N", "g/dl");
        testUnits.put("ALB1N", "g/dl");
        testUnits.put("UA-1N", "mg/dl");
        testUnits.put("CKN1N", "U/L");
        testUnits.put("CKM1N", "ng/ml");
        testUnits.put("AMY2N", "U/L");
        testUnits.put("LIP1N", "U/L");
        testUnits.put("CHO2N", "mg/dL");
        testUnits.put("TRG1N", "mg/dl");
        testUnits.put("LDL1N", "mg/dL");
        testUnits.put("HDL1N", "mg/dL");
        testUnits.put("FE-1N", "mg/dL");
        testUnits.put("UBC1N", "mg/dL");
        testUnits.put("PHO1N", "mg/dl");
        testUnits.put("NA", "mmol/L");
        testUnits.put("K", "mmol/L");
        testUnits.put("CL", "mmol/L");
        testUnits.put("CAZ1N", "mmol/L");
        testUnits.put("MG-1N", "mmol/L");
        testUnits.put("THB1G", "%");
        return testUnits;
    }

    @Override
    public boolean insert(List<String> lines, String currentUserId) {
        return false;
    }

    @Override
    public String getError() {
        return null;
    }
}