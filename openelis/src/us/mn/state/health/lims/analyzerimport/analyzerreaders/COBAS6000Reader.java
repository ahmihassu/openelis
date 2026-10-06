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

public class COBAS6000Reader extends AnalyzerLineInserter{
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

        MappedTestName mappedName = AnalyzerTestNameCache.instance().getMappedTest(AnalyzerTestNameCache.AnalyzerType.COBAS6000, testName);
        if (mappedName == null) {
            mappedName = AnalyzerTestNameCache.instance().getEmptyMappedTestName(AnalyzerTestNameCache.AnalyzerType.COBAS6000, testName);
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
        testUnits.put("T3", " ");
        testUnits.put("T4", " ");
        testUnits.put("TESTO", " ");
        testUnits.put("TNT-HSST", " ");
        testUnits.put("TP2", " ");

        testUnits.put("TP C3", " ");
        testUnits.put("TPSA", " ");
        testUnits.put("TPU3", " ");
        testUnits.put("TRIGL", " ");
        testUnits.put("TSH", " ");

        testUnits.put("UA2", " ");
        testUnits.put("UREAL", " ");
        testUnits.put("PHOS2", " ");
        testUnits.put("PRL", " ");
        testUnits.put("PROG III", " ");

        testUnits.put("RHD3", " ");
      
        testUnits.put("RWD3", " ");
        testUnits.put("T3", " ");
        testUnits.put("T4", " ");
        testUnits.put("TESTO", " ");

        testUnits.put("TNT-HSST", " ");
        testUnits.put("TP2", " ");

        testUnits.put("TPC3", " ");
        testUnits.put("TPSA", " ");
        testUnits.put("HB-W3", " ");
        testUnits.put("HCG-BETA", " ");
        testUnits.put("HDLC4", " ");
        testUnits.put("IRON2", " ");
        testUnits.put("K", " ");
        testUnits.put("L", " ");
        testUnits.put("LDHI2", " ");
        testUnits.put("LDLC3", " ");
        testUnits.put("LH", " ");
        testUnits.put("LIP", " ");
        testUnits.put("MG-2", " ");
        testUnits.put("Na", " ");
        testUnits.put("E2 III", " ");
        testUnits.put("FERR", " ");

        testUnits.put("FOL III", " ");
        testUnits.put("FPSA", " ");
        testUnits.put("FSH", " ");

        testUnits.put("FT3 3", " ");
        testUnits.put("FT3 III", " ");
        testUnits.put("FT4 3", " ");
        testUnits.put("GGTI2", " ");
        testUnits.put("GLUC2", " ");
        testUnits.put("GLUC3 L", " ");
        testUnits.put("HB-H3", " ");
        testUnits.put("B12 II", " ");
        testUnits.put("BILT3", " ");
        testUnits.put("CA125", " ");
        testUnits.put("CA2", " ");
        testUnits.put("CEA", " ");
        testUnits.put("CHO21", " ");
        testUnits.put("CK2", " ");
        testUnits.put("CKMBSTAT", " ");
        testUnits.put("CORT", " ");
        testUnits.put("CREJ2", " ");
        testUnits.put("CRPHS", " ");
        testUnits.put("CI", " ");
        testUnits.put("DBIL2", " ");
        testUnits.put("A1-H3", " ");
        testUnits.put("A1-W3", " ");
        testUnits.put("AAGP2", " ");
        testUnits.put("ACTH", " ");
        testUnits.put("AFP", " ");
        testUnits.put("ALB2", " ");
        testUnits.put("ALBP", " ");
        testUnits.put("ALP2L", " ");
        testUnits.put("ALP2S", " ");
        testUnits.put("ALTL", " ");
        testUnits.put("AMY-P", " ");
        testUnits.put("AMYL2", " ");
        testUnits.put("ASTL", " ");


         
       
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
