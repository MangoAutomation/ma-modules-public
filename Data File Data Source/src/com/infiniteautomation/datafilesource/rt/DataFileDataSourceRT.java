package com.infiniteautomation.datafilesource.rt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.infiniteautomation.datafilesource.contexts.AbstractCSVDataSource;
import com.infiniteautomation.datafilesource.contexts.AbstractSheetDataSource;
import com.infiniteautomation.datafilesource.contexts.AbstractXMLDataSource;
import com.infiniteautomation.datafilesource.contexts.CSVImporter;
import com.infiniteautomation.datafilesource.dataimage.AlphanumericImportPoint;
import com.infiniteautomation.datafilesource.dataimage.NumericImportPoint;
import com.infiniteautomation.datafilesource.dataimage.MultistateImportPoint;
import com.infiniteautomation.datafilesource.dataimage.BinaryImportPoint;
import com.infiniteautomation.datafilesource.dataimage.ImportPoint;
import com.infiniteautomation.datafilesource.rt.DataFileDataSourceRT;
import com.infiniteautomation.datafilesource.vo.DataFileDataSourceVO;
import com.infiniteautomation.datafilesource.vo.DataFilePointLocatorVO;

import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.emport.SpreadsheetEmporter;
import com.serotonin.m2m2.vo.event.PointEventDetectorVO;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.SetPointSource;
import com.serotonin.m2m2.rt.dataSource.PollingDataSource;

public class DataFileDataSourceRT extends PollingDataSource  {
	private final Log LOG = LogFactory.getLog(DataFileDataSourceRT.class);
    public static final int POINT_READ_EXCEPTION_EVENT = 1;
    public static final int POINT_WRITE_EXCEPTION_EVENT = 2;
    public static final int DATA_SOURCE_EXCEPTION_EVENT = 3;
    public static final int POINT_READ_PATTERN_MISMATCH_EVENT = 4;
    
    public static final int XML_TYPE = 0;
    public static final int CSV_TYPE = 1;
    public static final int EXCEL_TYPE = 2;
    
	private File file; //File
	private ClassLoader cl;
	
	public DataFileDataSourceRT(DataFileDataSourceVO vo) {
		super(vo);
		setPollingPeriod(vo.getUpdatePeriodType(), vo.getUpdatePeriods(), false);
	}


	/**
	 * Load a file path
	 * @throws Exception 
	 */
	public boolean connect () throws Exception{
		DataFileDataSourceVO vo = (DataFileDataSourceVO) this.getVo();
		
        this.file = new File( vo.getFilePath() );
		if ( !file.exists() ) {
			raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("jsonfile.event.fileNotFound",vo.getFilePath()));
			return false;
        }
			
		return true;

    }
	
    @Override
    public void initialize() {
    	DataFileDataSourceVO vo = (DataFileDataSourceVO) this.getVo();
    	boolean connected = false;
    	try{
    		connected = this.connect();
    	}catch(Exception e){
    		LOG.debug("Error while initializing data source", e);
    		String msg = e.getMessage();
    		if(msg == null){
    			msg = "Unknown";
    		}
			raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("datafile.event.fileNotFound",msg));
			
    	}
    	
    	if(connected){
    		returnToNormal(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis());
    	}
    	
    	//Create classloader for easy extensions hardcoded elements!
    	String loaderPath = Common.MA_HOME + ModuleRegistry.getModule("dataFile").getDirectoryPath() + "/web/templates/" + vo.getTypeName() + "/";
    	try {
    		File myF = new File(loaderPath);
    		LOG.debug(myF.toURL().toString());
    		
    		URL[] urls = new URL[]{myF.toURL()};
    		cl = new URLClassLoader(urls, DataFileDataSourceRT.class.getClassLoader());
    	} catch (MalformedURLException e) {
    		throw new ShouldNeverHappenException(e);
    	}
    	
        super.initialize();
    	
    }
    
    @Override
    public void terminate() {
    	cl = null;
        super.terminate();
    }
    
    public void performRename() {
    	DataFileDataSourceVO vo = (DataFileDataSourceVO) this.getVo();
    	File[] list;
    	if(file.isFile()) {
    		list = new File[]{file};
    	}
		else {
			list = file.listFiles();
		}

    	for(File f : list) {
    		if(vo.isDeleteAfterImport()) {
    			f.delete();
    		}
    		else {
	    		File rename = new File(f.getParent() + vo.getAddedPrefix() + f.getName());
	    		if(rename.exists()) {
	    			rename.delete();
	    		}
	    		try {
	    			FileUtils.moveFile(f, rename);
	    		} catch (IOException e) {
	    			raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), false, new TranslatableMessage("datafile.event.renameFailed",e.getMessage()));
	    		}
    		}
    	}
    }
	
    
    public String[] buildIdentifiers() {
    	//Build the maps to hand off to the importer
		int cnt = 0;
		String[] headers = new String[this.dataPoints.size()]; 
		for(DataPointRT dp : this.dataPoints) {
    		DataFilePointLocatorRT pl = dp.getPointLocator();
    		DataFilePointLocatorVO plVo = pl.getVo();
    		headers[cnt] = plVo.getFromIdentifier();
    		cnt+=1;
    	}
    	return headers;
    }
    
    //Loads all files within a particular directory, but doesn't recurse directory trees
	private List<ImportPoint> loadNewFiles() {
		List<ImportPoint> result = new ArrayList<ImportPoint>();
		DataFileDataSourceVO vo = (DataFileDataSourceVO) this.getVo();
		File[] contents = file.listFiles();
		String alreadyProcessed = vo.getAddedPrefix();
		
		if(alreadyProcessed == null) {
			alreadyProcessed = "\nUNLIKELY\n";
		}
		
		for (File f : contents) {
			if(f != null) {
				LOG.debug("examining file with name: " + f.getName());
				if(f.getName() != null && ! f.getName().startsWith(alreadyProcessed) && ! f.isDirectory()) {
					result.addAll(loadNewFile(f));
				}
			}
		}
		
		return result;
	}
	
	public List<ImportPoint> loadNewFile(File f) {
		DataFileDataSourceVO vo = (DataFileDataSourceVO) this.getVo();
		
		try {
			
			switch(vo.getFileType()) {
			
			case XML_TYPE : //Use a JAXB unmarshaller on an implementer of interface AbstractDataSourceXML
				Class<? extends AbstractXMLDataSource> xmlTemplate = cl.loadClass(vo.getTemplate()).asSubclass(AbstractXMLDataSource.class);
				JAXBContext context = JAXBContext.newInstance(xmlTemplate);
				Unmarshaller un = context.createUnmarshaller();
				AbstractXMLDataSource xmlData = (AbstractXMLDataSource) un.unmarshal(new FileInputStream(f.getAbsolutePath()));
				return xmlData.getParsedPoints();
				
			case CSV_TYPE :
				Class<? extends AbstractCSVDataSource> csvTemplate = cl.loadClass(vo.getTemplate()).asSubclass(AbstractCSVDataSource.class);
				AbstractCSVDataSource csvInterpreter = csvTemplate.newInstance();
				CSVImporter csvImporter = new CSVImporter();
				
				if(csvInterpreter.takesPointIdentifiers())
					csvInterpreter.setIdentifiers(buildIdentifiers());
				
				csvImporter.doImport(f, csvInterpreter);
				return csvInterpreter.getParsedPoints();
				
			case EXCEL_TYPE : //load the class that subclasses AbstractDataSourceSheet, pass it to Mango's Excel routines
				Class<? extends AbstractSheetDataSource> excelTemplate = cl.loadClass(vo.getTemplate()).asSubclass(AbstractSheetDataSource.class);
				AbstractSheetDataSource excelImporter = excelTemplate.newInstance();
				
				if(excelImporter.takesPointHeaders())
					excelImporter.setHeaders(buildIdentifiers()); //convert the configured points into headers
				
				SpreadsheetEmporter ssImporter = new SpreadsheetEmporter(f.getAbsolutePath());
		    	ssImporter.doImport(excelImporter);
		    	return excelImporter.getParsedPoints();
		    	
			default :
				return new ArrayList<ImportPoint>();
				
			}
			//TODO: Clean Exception handling
		} catch (ClassNotFoundException e) {
			throw new ShouldNeverHappenException(e);
		} catch (FileNotFoundException e) {
			throw new ShouldNeverHappenException(e);
		} catch (IllegalAccessException e) {
			throw new ShouldNeverHappenException(e);
		} catch (JAXBException e) {
			raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), false, new TranslatableMessage("datafile.event.jaxbException", e.getMessage()));
		} catch (InstantiationException e) {
			raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), false, new TranslatableMessage("datafile.event.instantiationException", e.getMessage()));
		} catch (IOException e) {
			raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), false, new TranslatableMessage("datafile.event.ioException", e.getMessage()));
		} 
		return new ArrayList<ImportPoint>();
	}

	@Override
	protected void doPoll(long time) {
		if(file != null && file.exists()) {
			List<ImportPoint> result = new ArrayList<ImportPoint>();
			if(file.isDirectory())
				result.addAll(loadNewFiles());
			else if(file.isFile())
				result.addAll(loadNewFile(file));
			
			if(result.size() > 0) {
				performImport(result);
				performRename();
			}
		}
	}
	
	private void performImport(List<ImportPoint> result) {
		DataFileDataSourceVO vo = (DataFileDataSourceVO) this.getVo();
		DataPointDao dpd = new DataPointDao();
		Map<String,DataPointRT> xidMap = new HashMap<String,DataPointRT>();
		Map<String,DataPointRT> identMap = new HashMap<String,DataPointRT>();
		Map<String,DataPointRT> createdMap = new HashMap<String,DataPointRT>();
		int type = DataTypes.UNKNOWN; //used for creating points
		for(DataPointRT rt : this.dataPoints) {
			DataFilePointLocatorVO plVo = (DataFilePointLocatorVO) rt.getVO().getPointLocator();
			if(plVo.getIsMappingPoint()) {
				DataPointRT external = Common.runtimeManager.getDataPoint(dpd.getByXid(plVo.getToXid()).getId());
				xidMap.put(rt.getVO().getXid(), external);
			}
			else {
				identMap.put(plVo.getFromIdentifier(), rt);
			}
		}
		
		for(ImportPoint pnt : result) {
			PointValueTime pvt = null;
			switch(pnt.getDataType()) {
			case ALPHANUMERIC :
				pvt = new PointValueTime(((AlphanumericImportPoint)pnt).value, pnt.getTime());
				type = DataTypes.ALPHANUMERIC;
				break;
			case NUMERIC :
				pvt = new PointValueTime(((NumericImportPoint)pnt).value, pnt.getTime());
				type = DataTypes.NUMERIC;
				break;
			case MULTISTATE :
				pvt = new PointValueTime(((MultistateImportPoint)pnt).value, pnt.getTime());
				type = DataTypes.MULTISTATE;
				break;
			case BINARY :
				pvt = new PointValueTime(((BinaryImportPoint)pnt).value, pnt.getTime());
				type = DataTypes.BINARY;
				break;
			}
			
			if(pvt != null) {
				if(xidMap.containsKey(pnt.getIdentifier()))
					xidMap.get(pnt.getIdentifier()).setPointValue(pvt, null);
				else if(identMap.containsKey(pnt.getIdentifier())) {
					identMap.get(pnt.getIdentifier()).setPointValue(pvt, null);
				}
				else if(createdMap.containsKey(pnt.getIdentifier())) {
					createdMap.get(pnt.getIdentifier()).setPointValue(pvt, null);
				}
				else if(vo.getCreatePoints()){
					DataPointVO dp = new DataPointVO();
					DataFileDataSourceVO ds = (DataFileDataSourceVO) this.getVo();
					DataFilePointLocatorVO pl = (DataFilePointLocatorVO) ds.createPointLocator();
					String newXid = pnt.getIdentifier();
					if(newXid.length() > 40)
						newXid = newXid.substring(0,39);
					
					//TODO speculate on enabled/disabled influences runtime
					//TODO ensure that just created points get their just imported values. Tested successfully under light load
					if(dpd.getByXid(pnt.getIdentifier()) == null) {
			        	dp.setId(Common.NEW_ID);       	
			        	dp.setXid(pnt.getIdentifier());
			        	dp.setName(pnt.getIdentifier());
			            dp.setDataSourceId(ds.getId());
			            dp.setDataSourceTypeName(ds.getDefinition().getDataSourceTypeName());
			            dp.setDeviceName(ds.getName());
			            dp.setEnabled(true);
			            pl.setDataTypeId(type);
			            dp.setPointLocator(pl);
			            dp.setEventDetectors(new ArrayList<PointEventDetectorVO>(0));
			            dp.defaultTextRenderer();
			            Common.runtimeManager.saveDataPoint(dp); //POTENTIALLY THREAD UNSAFE ACTIONS HERE::
			            createdMap.put(pnt.getIdentifier(), Common.runtimeManager.getDataPoint(dp.getId())); //Or at least this call will fail when it shouldn't.
			            createdMap.get(pnt.getIdentifier()).setPointValue(pvt, null);
					}
					else {
						raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), false, new TranslatableMessage("datafile.event.cantCreate",pnt.getIdentifier()));
					}
				}
			}
		}
	}


	@Override
	public void setPointValue(DataPointRT dataPoint, PointValueTime valueTime,
			SetPointSource source) {
		// no-op, no points
		
	}
}
