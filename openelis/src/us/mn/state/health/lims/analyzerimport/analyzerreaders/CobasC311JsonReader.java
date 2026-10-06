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

public class CobasC311JsonReader extends AnalyzerLineInserter{
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
        Date dateTime = dateFormat.parse(json.getString("dateTime").replace("T", " "));
        Timestamp timestamp = new Timestamp(dateTime.getTime());
        //Timestamp timestamp = new Timestamp(new Date().getTime());
        String analyzerAccessionNumber = json.getString("sampleId");
        Sample sample = sampleDAO.getSampleByAccessionNumber(analyzerAccessionNumber);
        if( sample == null)
            return;
        String result = json.getString("result");
        AnalyzerResults analyzerResults = new AnalyzerResults();

        MappedTestName mappedName = AnalyzerTestNameCache.instance().getMappedTest(AnalyzerTestNameCache.AnalyzerType.COBASC311, testName);
        if (mappedName == null) {
            mappedName = AnalyzerTestNameCache.instance().getEmptyMappedTestName(AnalyzerTestNameCache.AnalyzerType.COBASC311, testName);
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
        testUnits.put("ASTL", "U/L");
        testUnits.put("ALTL", "U/L");
        testUnits.put("ALP2S", "U/L");
        testUnits.put("DBIL2", "mg/dL");
        testUnits.put("BILT3", "mg/dL");
        testUnits.put("CREJ2", "g/dl");
        testUnits.put("ALB2", "g/dL");
        testUnits.put("TP2", "g/dL");
        testUnits.put("GLUC3", "mg/dL");
        testUnits.put("CHO2I", "mg/dL");
        testUnits.put("TRIGL", "mg/dL");
        testUnits.put("HDLC4", "mg/dL");
        testUnits.put("LDLC3", "mg/dL");
        testUnits.put("UA2", "mg/dL");
        testUnits.put("CK2", "mg/dL");
        testUnits.put("CKMB2", "ng/ml");
        testUnits.put("CA2", "g/dl");
        testUnits.put("PHOS2", "mg/dL");
        testUnits.put("MG-2", "mmol/L");
        testUnits.put("UREAL", "mg/dl");
        testUnits.put("AMYL2", "U/L");
        testUnits.put("AMY-P", "U/L");
        testUnits.put("LIP", "U/L");
        testUnits.put("LDH2", "U/L");
        testUnits.put("GLUC2", "mg/dL");
        testUnits.put("ALBP", "g/dL");
        testUnits.put("NA", "mmol/L");
        testUnits.put("K", "mmol/L");
        testUnits.put("CL", "mmol/L");
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