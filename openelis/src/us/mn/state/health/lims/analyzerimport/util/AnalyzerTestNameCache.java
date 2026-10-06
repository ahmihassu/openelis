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
package us.mn.state.health.lims.analyzerimport.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import us.mn.state.health.lims.analyzer.dao.AnalyzerDAO;
import us.mn.state.health.lims.analyzer.daoimpl.AnalyzerDAOImpl;
import us.mn.state.health.lims.analyzer.valueholder.Analyzer;
import us.mn.state.health.lims.analyzerimport.dao.AnalyzerTestMappingDAO;
import us.mn.state.health.lims.analyzerimport.daoimpl.AnalyzerTestMappingDAOImpl;
import us.mn.state.health.lims.analyzerimport.valueholder.AnalyzerTestMapping;
import us.mn.state.health.lims.test.dao.TestDAO;
import us.mn.state.health.lims.test.daoimpl.TestDAOImpl;
import us.mn.state.health.lims.test.valueholder.Test;

public class AnalyzerTestNameCache {

	private static AnalyzerTestNameCache instance;
	
	private static final String COBAS_INTEGRA400_NAME = "Cobas Integra";
	private static final String SYSMEX_XT2000_NAME = "Sysmex XT 2000";
	private static final String Mindray_3452_NAME = "Mindray 3452";
	private static final String Dxh_800_NAME = "DxH 800";
	private static final String Dxc_700_NAME = "DxC 700";

	private static final String ADV_NAME = "ADVIA 560";
	private static final String Abx_Micros_NAME = "ABX Micros ES";
	private static final String Dxi_800_NAME = "DxI 800";
	private static final String Sysmex_XN_NAME = "SysmexXN550";
	private static final String Sysmex_KX_NAME = "Sysmex KX-21N";
	private static final String Humastar200_NAME = "HumaStar 200";
	private static final String BS200E_NAME = "BS-200E";
	private static final String Amha1806_NAME = "Amha1806";

	private static final String DYMINDDH36_NAME = "DYMIND DH36";
	private static final String MN5150_NAME = "MINDRAY 5150";
	private static final String BC30_NAME = "MINDRAY BC30";
	private static final String DF5_NAME = "DYMIND DF5";
	private static final String BK_NAME = "BEKMAN";
    private static final String BS230_NAME = "MINDRAY BS230";
    private static final String COBAS6000_NAME = "COBAS 6000";
    private static final String FN_NAME = "Finecare";
    private static final String CL_NAME = "Mindray CL";

	private static final String MindrayBC780_NAME = "Mindray BC-780";
	private static final String COBASE402_NAME = "Cobas e402";
	private static final String MindrayCL980i_NAME = "Mindray CL-980i";
	private static final String BC3000_NAME = "BC3000";
	private static final String COBASC311_NAME = "Cobas C 311";
	private static final String EXL200_NAME = "EXL 200";
	private static final String GENEXPERT_NAME = "GeneXpert";
	private static final String FACSCALIBUR = "Facscalibur";
	private static final String FACSCANTO = "FacsCanto";
	private static final String EVOLIS = "Evolis";
	private static final String COBAS_TAQMAN = "Cobas Taqman";
	private static final String COBAS_DBS = "CobasDBS";
	private static final String COBAS_C311 = "Cobas C311";

	public static enum AnalyzerType {
		COBAS_INTEGRA400,
		SYSMEX_XT_2000,
		Mindray_3452,
		Dxh_800,
		Amha1806,
		Dxc_700,
		ADV,
		Abx_Micros,
		Dxi_800,
		SYSMEX_XN,
		Humastar200,
		BS200E,
		DYMINDDH36,
		MN5150,
		BC30,
		DF5,
		BK,
        BS230,
        COBAS6000,
        FN,
        CL,
		COBASE402,
		MindrayBC780,
		MindrayCL980I,
		SYSMEX_KX,
		BC3000,
		COBASC311,
		EXL200,
		GENEXPERT,
		FACSCALIBUR,
		EVOLIS,
		COBAS_TAQMAN,
		FACSCANTO,
		COBAS_DBS,
		COBAS_C311
	}

	private static Map<String, MappedTestName> Amha1806_NAMEMAP;

	private static Map<String, MappedTestName> CobasIntegraTestNameMap;
	private static Map<String, MappedTestName> CobasTaqmanTestNameMap;
	private static Map<String, MappedTestName> SysmexXT2000TestNameMap;
	private static Map<String, MappedTestName> Mindray3452TestNameMap;
	private static Map<String, MappedTestName> Dxh800NameMap;
	private static Map<String, MappedTestName> Dxc700NameMap;
	private static Map<String, MappedTestName> Advia560NameMap;
	private static Map<String, MappedTestName> AbxMicrosNameMap;
	private static Map<String, MappedTestName> Dxi800NameMap;
	private static Map<String, MappedTestName> SysmexXN550NameMap;
	private static Map<String, MappedTestName> SysmexKX21NNameMap;
	private static Map<String, MappedTestName> Humastar200NameMap;
	private static Map<String, MappedTestName> BS200E_NAMETestNameMap;
	private static Map<String, MappedTestName> DYMINDDH36_NAMETestNameMap;
	private static Map<String, MappedTestName> MN5150_NAMETestNameMap;
	private static Map<String, MappedTestName> BC30_NAMETestNameMap;
    private static Map<String, MappedTestName> BC3000_NAMETestNameMap;
	private static Map<String, MappedTestName> DF5_NAMETestNameMap;
	private static Map<String, MappedTestName> BK_NAMETestNameMap;
    private static Map<String, MappedTestName> ADV_NAMETestNameMap;
    private static Map<String, MappedTestName> BS230_NAMETestNameMap;
    private static Map<String, MappedTestName> COBAS6000_NAMETestNameMap;
    private static Map<String, MappedTestName> FN_NAMETestNameMap;
    private static Map<String, MappedTestName> CL_NAMETestNameMap;

	private static Map<String, MappedTestName> MindrayBC780_NAMETestNameMap;
	private static Map<String, MappedTestName> COBASE402_NameTestNameMap;
	private static Map<String, MappedTestName> MindrayCL980i_NAMETestNameMap;

	private static Map<String, MappedTestName> COBASC311NameMap;
	private static Map<String, MappedTestName> EXL200NameMap;
	private static Map<String, MappedTestName> GENEXPERTTestNameMap;
	private static Map<String, MappedTestName> FacscaliburTestNameMap;
	private static Map<String, MappedTestName> EvolisTestNameMap;
	private static Map<String, MappedTestName> FacscantoTestNameMap;
	private static Map<String, MappedTestName> CobasDBStoTestNameMap;
	private static Map<String, MappedTestName> CobasC311toTestNameMap;

	private static Map<String, String> analyzerNameToIdMap;
	private static Map<AnalyzerType, String> analyzerIDMap;
	private static boolean isMapped = false;

	public static AnalyzerTestNameCache instance(){
		if( instance == null){
			instance = new AnalyzerTestNameCache();
		}
		
		return instance;
	}
	
	public MappedTestName getMappedTest( AnalyzerType analyzerType, String analyzerTestName){
		insureMapsLoaded();

		switch (analyzerType){
		case COBAS_INTEGRA400:
			return CobasIntegraTestNameMap.get(analyzerTestName);
		case SYSMEX_XT_2000:
			return SysmexXT2000TestNameMap.get(analyzerTestName);
		case Mindray_3452:
			return Mindray3452TestNameMap.get(analyzerTestName);
		case Amha1806:
				return Amha1806_NAMEMAP.get(analyzerTestName);
		case Dxh_800:
			return Dxh800NameMap.get(analyzerTestName);
		case Dxc_700:
			return Dxc700NameMap.get(analyzerTestName);
		case ADV:
			return ADV_NAMETestNameMap.get(analyzerTestName);
		case Abx_Micros:
			return AbxMicrosNameMap.get(analyzerTestName);
		case Dxi_800:
			return Dxi800NameMap.get(analyzerTestName);
		case SYSMEX_XN:
			return SysmexXN550NameMap.get(analyzerTestName);
		case SYSMEX_KX:
			return SysmexKX21NNameMap.get(analyzerTestName);
		case Humastar200:
			return Humastar200NameMap.get(analyzerTestName);
		case BS200E:
			return BS200E_NAMETestNameMap.get(analyzerTestName);
		case DYMINDDH36:
			return DYMINDDH36_NAMETestNameMap.get(analyzerTestName);
		case MN5150:
			return MN5150_NAMETestNameMap.get(analyzerTestName);
		case BC30:
			return BC30_NAMETestNameMap.get(analyzerTestName);
		case DF5:
			return DF5_NAMETestNameMap.get(analyzerTestName);
		case BK:
			return BK_NAMETestNameMap.get(analyzerTestName);
        case BS230:
                return BS230_NAMETestNameMap.get(analyzerTestName);
        case COBAS6000:
                return COBAS6000_NAMETestNameMap.get(analyzerTestName);

        case FN:
                return FN_NAMETestNameMap.get(analyzerTestName);

        case CL:
                return CL_NAMETestNameMap.get(analyzerTestName);
		case MindrayBC780:
			return MindrayBC780_NAMETestNameMap.get(analyzerTestName);
		case MindrayCL980I:
			return MindrayCL980i_NAMETestNameMap.get(analyzerTestName);
		case COBASE402:
				return COBASE402_NameTestNameMap.get(analyzerTestName);
		case BC3000:
			return BC3000_NAMETestNameMap.get(analyzerTestName);
		case COBASC311:
			return COBASC311NameMap.get(analyzerTestName);	
		case EXL200:
			return EXL200NameMap.get(analyzerTestName);
		case GENEXPERT:
			return GENEXPERTTestNameMap.get(analyzerTestName);
		case FACSCALIBUR:
			return FacscaliburTestNameMap.get(analyzerTestName);
		case EVOLIS:
			return EvolisTestNameMap.get(analyzerTestName);
		case COBAS_TAQMAN:
			return CobasTaqmanTestNameMap.get(analyzerTestName);
		case FACSCANTO:
			return FacscantoTestNameMap.get(analyzerTestName);
		case COBAS_DBS:
			return CobasDBStoTestNameMap.get(analyzerTestName);	
		case COBAS_C311:
			return CobasC311toTestNameMap.get(analyzerTestName);		
		default:
			return null;
		}
	}

	private synchronized void insureMapsLoaded() {
		if( !isMapped){
			loadMaps();
			isMapped = true;
		}
	}

	public Map<String, MappedTestName> getMappedTestsForAnalyzer( AnalyzerType analyzerType){
		insureMapsLoaded();

		switch (analyzerType){
		case COBAS_INTEGRA400:
			return CobasIntegraTestNameMap;
		case SYSMEX_XT_2000:
			return SysmexXT2000TestNameMap;
		case Mindray_3452:
			return Mindray3452TestNameMap;
		case Amha1806:
				return Amha1806_NAMEMAP;
		case Dxh_800:
			return Dxh800NameMap;
		case Abx_Micros:
			return AbxMicrosNameMap;
		case ADV:
			return ADV_NAMETestNameMap;
		case EXL200:
			return EXL200NameMap;
		case GENEXPERT:
			return GENEXPERTTestNameMap;
		case SYSMEX_XN:
			return SysmexXN550NameMap;
		case Humastar200:
			return Humastar200NameMap;
		case BS200E:
			return BS200E_NAMETestNameMap;
		case DYMINDDH36:
			return DYMINDDH36_NAMETestNameMap;
		case MN5150:
			return MN5150_NAMETestNameMap;
		case BC30:
			return BC30_NAMETestNameMap;
		case DF5:
			return DF5_NAMETestNameMap;
		case BK:
			return BK_NAMETestNameMap;
        case BS230:
            return BS230_NAMETestNameMap;

        case COBAS6000:
            return COBAS6000_NAMETestNameMap;
        case FN:
            return FN_NAMETestNameMap;

        case CL:
             return CL_NAMETestNameMap;
		case MindrayCL980I:
			return MindrayCL980i_NAMETestNameMap;

		case COBASE402:
			return COBASE402_NameTestNameMap;
		case SYSMEX_KX:
			return SysmexKX21NNameMap;
		case BC3000:
			return BC3000_NAMETestNameMap;
		case COBASC311:
			return COBASC311NameMap;
		case Dxi_800:
			return Dxi800NameMap;
		case FACSCALIBUR:
			return FacscaliburTestNameMap;
		case EVOLIS:
			return EvolisTestNameMap;
		case COBAS_TAQMAN:
			return CobasTaqmanTestNameMap;
		case FACSCANTO:
			return FacscantoTestNameMap;
		case COBAS_DBS:
			return CobasDBStoTestNameMap;	
		case COBAS_C311:
			return CobasC311toTestNameMap;		
		default:
			return null;
		}
	}

	public synchronized void reloadCache(){
		isMapped = false;
	}

	private void loadMaps() {
		AnalyzerDAO analyzerDAO = new AnalyzerDAOImpl();
		List<Analyzer> analyzerList = analyzerDAO.getAllAnalyzers();
		Map<String, Map<String,MappedTestName>> analyzerMapList = new HashMap<String, Map<String,MappedTestName>>();

		CobasIntegraTestNameMap = new HashMap<String,MappedTestName>();
		SysmexXT2000TestNameMap = new HashMap<String,MappedTestName>();
		Mindray3452TestNameMap = new HashMap<String,MappedTestName>();
		Dxh800NameMap = new HashMap<String,MappedTestName>();
		Dxc700NameMap = new HashMap<String,MappedTestName>();
        ADV_NAMETestNameMap = new HashMap<String,MappedTestName>();
		AbxMicrosNameMap = new HashMap<String,MappedTestName>();
		Dxi800NameMap = new HashMap<String,MappedTestName>();
		SysmexXN550NameMap = new HashMap<String,MappedTestName>();
		SysmexKX21NNameMap = new HashMap<String,MappedTestName>();
		Humastar200NameMap = new HashMap<String,MappedTestName>();
		BS200E_NAMETestNameMap = new HashMap<String,MappedTestName>();
		DYMINDDH36_NAMETestNameMap = new HashMap<String,MappedTestName>();
		MN5150_NAMETestNameMap=new HashMap<String,MappedTestName>();

		BC30_NAMETestNameMap=new HashMap<String,MappedTestName>();
		DF5_NAMETestNameMap=new HashMap<String,MappedTestName>();

		BK_NAMETestNameMap=new HashMap<String,MappedTestName>();
        BS230_NAMETestNameMap=new HashMap<String,MappedTestName>();
        COBAS6000_NAMETestNameMap=new HashMap<String,MappedTestName>();
        FN_NAMETestNameMap=new HashMap<String,MappedTestName>();
        CL_NAMETestNameMap=new HashMap<String,MappedTestName>();

		MindrayBC780_NAMETestNameMap = new HashMap<String,MappedTestName>();
		COBASE402_NameTestNameMap = new HashMap<String,MappedTestName>();
		MindrayCL980i_NAMETestNameMap = new HashMap<String,MappedTestName>();
        BC3000_NAMETestNameMap = new HashMap<String,MappedTestName>();
		COBASC311NameMap = new HashMap<String,MappedTestName>();
		EXL200NameMap = new HashMap<String,MappedTestName>();
		GENEXPERTTestNameMap = new HashMap<String,MappedTestName>();
		FacscaliburTestNameMap = new HashMap<String,MappedTestName>();
		EvolisTestNameMap = new HashMap<String,MappedTestName>();
		CobasTaqmanTestNameMap = new HashMap<String, MappedTestName>();
		FacscantoTestNameMap = new HashMap<String, MappedTestName>();
		CobasDBStoTestNameMap = new HashMap<String, MappedTestName>();
		CobasC311toTestNameMap = new HashMap<String, MappedTestName>();
		Amha1806_NAMEMAP = new HashMap<String, MappedTestName>();

		analyzerIDMap = new HashMap<AnalyzerType, String>();
		analyzerNameToIdMap = new HashMap<String, String>();

		for( Analyzer analyzer : analyzerList){
			analyzerNameToIdMap.put(analyzer.getName(), analyzer.getId());

			if( COBAS_INTEGRA400_NAME.equals( analyzer.getName())){
				analyzerMapList.put(analyzer.getId(), CobasIntegraTestNameMap );
				analyzerIDMap.put(AnalyzerType.COBAS_INTEGRA400, analyzer.getId());
			}else if( SYSMEX_XT2000_NAME.equals(analyzer.getName())){
				analyzerMapList.put(analyzer.getId(), SysmexXT2000TestNameMap);
				analyzerIDMap.put(AnalyzerType.SYSMEX_XT_2000, analyzer.getId());
			}else if( Mindray_3452_NAME.equals(analyzer.getName())){
				analyzerMapList.put(analyzer.getId(), Mindray3452TestNameMap);
				analyzerIDMap.put(AnalyzerType.Mindray_3452, analyzer.getId());
			}else if( Dxh_800_NAME.equals(analyzer.getName())){
				analyzerMapList.put(analyzer.getId(), Dxh800NameMap);
				analyzerIDMap.put(AnalyzerType.Dxh_800, analyzer.getId());
			}else if( Dxc_700_NAME.equals(analyzer.getName())){
				analyzerMapList.put(analyzer.getId(), Dxc700NameMap);
				analyzerIDMap.put(AnalyzerType.Dxc_700, analyzer.getId());
			}else if( ADV_NAME.equals(analyzer.getName())){
				analyzerMapList.put(analyzer.getId(), ADV_NAMETestNameMap);
				analyzerIDMap.put(AnalyzerType.ADV, analyzer.getId());
			}else if( Abx_Micros_NAME.equals(analyzer.getName())){
				analyzerMapList.put(analyzer.getId(), AbxMicrosNameMap);
				analyzerIDMap.put(AnalyzerType.Abx_Micros, analyzer.getId());
			}else if( Dxi_800_NAME.equals(analyzer.getName())){
				analyzerMapList.put(analyzer.getId(), Dxi800NameMap);
				analyzerIDMap.put(AnalyzerType.Dxi_800, analyzer.getId());
			}else if( Sysmex_XN_NAME.equals(analyzer.getName())){
				analyzerMapList.put(analyzer.getId(), SysmexXN550NameMap);
				analyzerIDMap.put(AnalyzerType.SYSMEX_XN, analyzer.getId());
			}else if( Sysmex_KX_NAME.equals(analyzer.getName())){
				analyzerMapList.put(analyzer.getId(), SysmexKX21NNameMap);
				analyzerIDMap.put(AnalyzerType.SYSMEX_KX, analyzer.getId());
			}else if( Humastar200_NAME.equals(analyzer.getName())){
				analyzerMapList.put(analyzer.getId(), Humastar200NameMap);
				analyzerIDMap.put(AnalyzerType.Humastar200, analyzer.getId());
			}else if( BS200E_NAME.equals(analyzer.getName())){
				analyzerMapList.put(analyzer.getId(), BS200E_NAMETestNameMap);
				analyzerIDMap.put(AnalyzerType.BS200E, analyzer.getId());
			}else if( DYMINDDH36_NAME.equals(analyzer.getName())){
				analyzerMapList.put(analyzer.getId(), DYMINDDH36_NAMETestNameMap);
				analyzerIDMap.put(AnalyzerType.DYMINDDH36, analyzer.getId());
			}else if(MN5150_NAME.equals(analyzer.getName())){
				analyzerMapList.put(analyzer.getId(), MN5150_NAMETestNameMap);
				analyzerIDMap.put(AnalyzerType.MN5150, analyzer.getId());

			}else if(BC30_NAME.equals(analyzer.getName())){
				analyzerMapList.put(analyzer.getId(), BC30_NAMETestNameMap);
				analyzerIDMap.put(AnalyzerType.BC30, analyzer.getId());

			}else if(DF5_NAME.equals(analyzer.getName())){
				analyzerMapList.put(analyzer.getId(), DF5_NAMETestNameMap);
				analyzerIDMap.put(AnalyzerType.DF5, analyzer.getId());

			}else if(BK_NAME.equals(analyzer.getName())){
			analyzerMapList.put(analyzer.getId(), BK_NAMETestNameMap);
			analyzerIDMap.put(AnalyzerType.BK, analyzer.getId());

            }else if(BS230_NAME.equals(analyzer.getName())){
                analyzerMapList.put(analyzer.getId(), BS230_NAMETestNameMap);
                analyzerIDMap.put(AnalyzerType.BS230, analyzer.getId());

            }else if(COBAS6000_NAME.equals(analyzer.getName())){
                analyzerMapList.put(analyzer.getId(), COBAS6000_NAMETestNameMap);
                analyzerIDMap.put(AnalyzerType.COBAS6000, analyzer.getId());

            }else if(FN_NAME.equals(analyzer.getName())){
                analyzerMapList.put(analyzer.getId(), FN_NAMETestNameMap);
                analyzerIDMap.put(AnalyzerType.FN, analyzer.getId());

            }else if(CL_NAME.equals(analyzer.getName())){
                analyzerMapList.put(analyzer.getId(), CL_NAMETestNameMap);
                analyzerIDMap.put(AnalyzerType.CL, analyzer.getId());

			}else if( MindrayBC780_NAME.equals(analyzer.getName())){
				analyzerMapList.put(analyzer.getId(), MindrayBC780_NAMETestNameMap);
				analyzerIDMap.put(AnalyzerType.MindrayBC780, analyzer.getId());
			}else if( COBASE402_NAME.equals(analyzer.getName())){
				analyzerMapList.put(analyzer.getId(), COBASE402_NameTestNameMap);
				analyzerIDMap.put(AnalyzerType.COBASE402, analyzer.getId());
			}

			else if( MindrayCL980i_NAME.equals(analyzer.getName())){
				analyzerMapList.put(analyzer.getId(), MindrayCL980i_NAMETestNameMap);
				analyzerIDMap.put(AnalyzerType.MindrayCL980I, analyzer.getId());
			}else if( BC3000_NAME.equals(analyzer.getName())){
				analyzerMapList.put(analyzer.getId(), BC3000_NAMETestNameMap);
				analyzerIDMap.put(AnalyzerType.BC3000, analyzer.getId());
			}else if( COBASC311_NAME.equals(analyzer.getName())){
				analyzerMapList.put(analyzer.getId(), COBASC311NameMap);
				analyzerIDMap.put(AnalyzerType.COBASC311, analyzer.getId());	
			}else if( EXL200_NAME.equals(analyzer.getName())){
				analyzerMapList.put(analyzer.getId(), EXL200NameMap);
				analyzerIDMap.put(AnalyzerType.EXL200, analyzer.getId());
			}else if( GENEXPERT_NAME.equals(analyzer.getName())){
				analyzerMapList.put(analyzer.getId(), GENEXPERTTestNameMap);
				analyzerIDMap.put(AnalyzerType.GENEXPERT, analyzer.getId());
			}else if( FACSCALIBUR.equals(analyzer.getName())){
				analyzerMapList.put(analyzer.getId(), FacscaliburTestNameMap);
				analyzerIDMap.put(AnalyzerType.FACSCALIBUR, analyzer.getId());
			}else if(EVOLIS.equals(analyzer.getName())){
				analyzerMapList.put(analyzer.getId(), EvolisTestNameMap);
				analyzerIDMap.put(AnalyzerType.EVOLIS, analyzer.getId());
			}else if(COBAS_TAQMAN.equals(analyzer.getName())){
				analyzerMapList.put(analyzer.getId(), CobasTaqmanTestNameMap);
				analyzerIDMap.put(AnalyzerType.COBAS_TAQMAN, analyzer.getId());
			}else if( FACSCANTO.equals(analyzer.getName())){
				analyzerMapList.put(analyzer.getId(), FacscantoTestNameMap);
				analyzerIDMap.put(AnalyzerType.FACSCANTO, analyzer.getId());
			}else if( COBAS_DBS.equals(analyzer.getName())){
				analyzerMapList.put(analyzer.getId(), CobasDBStoTestNameMap);
				analyzerIDMap.put(AnalyzerType.COBAS_DBS, analyzer.getId());
			}else if( COBAS_C311.equals(analyzer.getName())){
				analyzerMapList.put(analyzer.getId(), CobasC311toTestNameMap);
				analyzerIDMap.put(AnalyzerType.COBAS_C311, analyzer.getId());

		   }else if( Amha1806_NAME.equals(analyzer.getName())){
			analyzerMapList.put(analyzer.getId(), Amha1806_NAMEMAP);
			analyzerIDMap.put(AnalyzerType.COBAS_C311, analyzer.getId());
		  }
		}

		AnalyzerTestMappingDAO analyzerTestMappingDAO = new AnalyzerTestMappingDAOImpl();
		List<AnalyzerTestMapping> mappingList = analyzerTestMappingDAO.getAllAnalyzerTestMappings();

		TestDAO testDAO = new TestDAOImpl();
		for( AnalyzerTestMapping mapping : mappingList){
			MappedTestName mappedTestName = createMappedTestName( testDAO, mapping );

			Map<String,MappedTestName> testMap = analyzerMapList.get(mapping.getAnalyzerId());
			if( testMap != null){
				testMap.put(mapping.getAnalyzerTestName(), mappedTestName);
			}
		}

	}

	private MappedTestName createMappedTestName(TestDAO testDAO, AnalyzerTestMapping mapping) {
		Test test = new Test();
		test.setId(mapping.getTestId());
		testDAO.getData(test);


		MappedTestName mappedTest = new MappedTestName();
		mappedTest.setAnalyzerTestName(mapping.getAnalyzerTestName());
		mappedTest.setTestId(mapping.getTestId());
		mappedTest.setOpenElisTestName(test.getLocalizedName());
		mappedTest.setAnalyzerId(mapping.getAnalyzerId());

		return mappedTest;
	}

	public MappedTestName getEmptyMappedTestName(AnalyzerType analyzerType, String analyzerTestName) {
		MappedTestName mappedTest = new MappedTestName();
		mappedTest.setAnalyzerTestName(analyzerTestName);
		mappedTest.setTestId(null);
		mappedTest.setOpenElisTestName(analyzerTestName);
		mappedTest.setAnalyzerId(analyzerIDMap.get(analyzerType));

		return mappedTest;
	}

	public String getAnalyzerId(AnalyzerType analyzerType) {
		insureMapsLoaded();

		return analyzerIDMap.get(analyzerType);
	}

	public String getAnalyzerIdForName(String analyzerName) {
		insureMapsLoaded();

		return analyzerNameToIdMap.get(analyzerName);
	}
	
	public static void setTestInstance( AnalyzerTestNameCache cache){
		instance = cache;
	}
}
