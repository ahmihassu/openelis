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

public class CLReader extends AnalyzerLineInserter{
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
        String testName = json.getString("parameterName");
        Date dateTime = dateFormat.parse(json.getString("dateTime").replace("T", " "));
        Timestamp timestamp = new Timestamp(dateTime.getTime());
        //Timestamp timestamp = new Timestamp(new Date().getTime());
        String analyzerAccessionNumber = json.getString("sampleId");
        Sample sample = sampleDAO.getSampleByAccessionNumber(analyzerAccessionNumber);
        if( sample == null)
            return;
        String result = json.getString("result");
        AnalyzerResults analyzerResults = new AnalyzerResults();

        MappedTestName mappedName = AnalyzerTestNameCache.instance().getMappedTest(AnalyzerTestNameCache.AnalyzerType.CL, testName);
        if (mappedName == null) {
            mappedName = AnalyzerTestNameCache.instance().getEmptyMappedTestName(AnalyzerTestNameCache.AnalyzerType.CL, testName);
        }

        analyzerResults.setAnalyzerId(mappedName.getAnalyzerId());
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
        testUnits.put("AFP", " ");
        testUnits.put("HCG", " ");
        testUnits.put("FT4", " ");
        testUnits.put("FT3", " ");
        testUnits.put("T4", " ");
        testUnits.put("T3", " ");
        testUnits.put("PSA", " ");
        testUnits.put("TSH", " ");
        testUnits.put("LH", " ");
        testUnits.put("CEA", " ");
        testUnits.put("FSH", " ");
        testUnits.put("CTni", " ");
        testUnits.put("CTnt", " ");
        testUnits.put("Prog", " ");
        testUnits.put("HbA1c", " ");
        testUnits.put("VB12", "pg/mL");
        testUnits.put("VD", "U/L");
        testUnits.put("FPSA", "ug/L");
        testUnits.put("T-PSA", "ug/L");
        testUnits.put("PRL", "ug/L");
        testUnits.put("TNL", "ug/L");
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
