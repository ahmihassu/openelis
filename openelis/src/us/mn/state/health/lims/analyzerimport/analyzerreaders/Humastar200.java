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

public class Humastar200 extends AnalyzerLineInserter{
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

        MappedTestName mappedName = AnalyzerTestNameCache.instance().getMappedTest(AnalyzerTestNameCache.AnalyzerType.Humastar200, testName);
        if (mappedName == null) {
            mappedName = AnalyzerTestNameCache.instance().getEmptyMappedTestName(AnalyzerTestNameCache.AnalyzerType.Humastar200, testName);
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
        testUnits.put("ACPT", "U/L");
        testUnits.put("ALB", "g/dL");
        testUnits.put("AMY", "U/L");
        testUnits.put("AMYP", "U/L");
        testUnits.put("APAMP", "U/L");
        testUnits.put("APDEA", "U/L");
        testUnits.put("BILDA", "mg/dL");
        testUnits.put("BILTA", "mg/dL");
        testUnits.put("CA", "g/dl");
        testUnits.put("CHOL", "mg/dL");
        testUnits.put("CK", "mg/dL");
        testUnits.put("CK-MB", "ng/ml");
        testUnits.put("CL", "mmol/L");
        testUnits.put("CREA-2", "g/dL");
        testUnits.put("CREAA", "g/dL");
        testUnits.put("FE", "mcg/dL");
        testUnits.put("FET", "ng/mL");
        testUnits.put("GGT", "U/L");
        testUnits.put("GLU", "mg/dL");
        testUnits.put("GLU-UV", "");
        testUnits.put("GOT", "U/L");
        testUnits.put("GPT", "U/L");
        testUnits.put("LIP", "U/L");
        testUnits.put("MG", "mg/dL");
        testUnits.put("APOA1", "mg/dL");
        testUnits.put("APOB", "mg/dL");
        testUnits.put("ASO", "U/mL");
        testUnits.put("C3", "mg/dL)");
        testUnits.put("C4", "mg/dL)");
        testUnits.put("CRP", "mg/dL");
        testUnits.put("CRPHS", "mg/dL");
        testUnits.put("CYSTC", "mg/L");
        testUnits.put("HBA1C", "mmol/mol");
        testUnits.put("HBA1C2", "%");
        testUnits.put("IGA", "g/L");
        testUnits.put("IGG", "g/L");
        testUnits.put("IGM", "g/L");
        testUnits.put("MALB", "mg/24 hr");
        testUnits.put("RF", "U/mL");
        testUnits.put("TRF", "mcg/dL");
        testUnits.put("HCY", "mcmol/L");
        testUnits.put("HDL", "mg/dL");
        testUnits.put("LDH", "U/L");
        testUnits.put("LDL", "mg/dL");
        testUnits.put("LIP2", "U/L");
        testUnits.put("PHOS", "mg/dL");
        testUnits.put("POTEZ", "mmol/L");
        testUnits.put("PROT", "g/dL");
        testUnits.put("SODEZ", "mmol/L");
        testUnits.put("TRIG", "mg/dL");
        testUnits.put("UA", "mg/dL");
        testUnits.put("UAP", "mg/dL");
        testUnits.put("UREAUV", "mg/dL");

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