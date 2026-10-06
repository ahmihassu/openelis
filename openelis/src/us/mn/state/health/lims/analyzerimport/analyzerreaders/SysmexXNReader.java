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

public class SysmexXNReader extends AnalyzerLineInserter{
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

        MappedTestName mappedName = AnalyzerTestNameCache.instance().getMappedTest(AnalyzerTestNameCache.AnalyzerType.SYSMEX_XN, testName);
        if (mappedName == null) {
            mappedName = AnalyzerTestNameCache.instance().getEmptyMappedTestName(AnalyzerTestNameCache.AnalyzerType.SYSMEX_XN, testName);
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
        testUnits.put("WBC", "10^3/uL");
        testUnits.put("RBC", "10^6/uL");
        testUnits.put("HGB", "g/dL");
        testUnits.put("HCT", "%");
        testUnits.put("MCV", "fL");
        testUnits.put("MCH", "pg");
        testUnits.put("MCHC", "g/dL");
        testUnits.put("PLT", "10^3/uL");
        testUnits.put("RDW-SD", "fL");
        testUnits.put("RDW-CV", "%");
        testUnits.put("PDW", "%");
        testUnits.put("MPV", "fL");
        testUnits.put("P-LCR", "%");
        testUnits.put("PCT", "%");

        testUnits.put("NEUT#", "10^3/uL");
        testUnits.put("LYMPH#", "10^3/uL");
        testUnits.put("MON#", "10^3/uL");
        testUnits.put("EO#", "10^3/uL");
        testUnits.put("BASO#", "10^3/uL");

        testUnits.put("NEUT%", "10^3/uL");
        testUnits.put("LYMPH%", "10^3/uL");
        testUnits.put("MON%", "10^3/uL");
        testUnits.put("EO%", "10^3/uL");
        testUnits.put("BASO%", "10^3/uL");

        testUnits.put("IG#", "10^3/uL");
        testUnits.put("IG%", "%");

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