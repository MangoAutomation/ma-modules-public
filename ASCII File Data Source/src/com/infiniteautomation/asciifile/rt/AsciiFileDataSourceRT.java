package com.infiniteautomation.asciifile.rt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.infiniteautomation.asciifile.vo.AsciiFileDataSourceVO;
import com.infiniteautomation.asciifile.vo.AsciiFilePointLocatorVO;
import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.SetPointSource;
import com.serotonin.m2m2.rt.dataSource.PollingDataSource;

/**
 * @author Phillip Dunlap
 */

public class AsciiFileDataSourceRT extends PollingDataSource implements FileAlterationListener{
	private final Log LOG = LogFactory.getLog(AsciiFileDataSourceRT.class);
	
    public static final int POINT_READ_EXCEPTION_EVENT = 1;
    public static final int POINT_WRITE_EXCEPTION_EVENT = 2;
    public static final int DATA_SOURCE_EXCEPTION_EVENT = 3;
    public static final int POINT_READ_PATTERN_MISMATCH_EVENT = 4;
    public static final int POLL_ABORTED_EVENT = 5;
    
	private File file; //File
	private FileAlterationObserver fobs;
	
	
	public AsciiFileDataSourceRT(AsciiFileDataSourceVO vo) {
		super(vo);
		setPollingPeriod(vo.getUpdatePeriodType(), vo.getUpdatePeriods(), false);
	}


	/**
	 * Load a file path
	 * @throws Exception 
	 */
	public boolean connect () throws Exception{
		AsciiFileDataSourceVO vo = (AsciiFileDataSourceVO) this.getVo();
		
		
		
        this.file = new File( vo.getFilePath() );
		if ( !file.exists() ) {
			raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("file.event.fileNotFound",vo.getFilePath()));
			return false;
		}else if ( !file.canRead() ){
			raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("file.event.readFailed",vo.getFilePath()));
			return false;
        }else{
			this.fobs = new FileAlterationObserver(this.file);
			this.fobs.initialize();
			this.fobs.addListener(this);
			
			return true;
        }

    }
	
    @Override
    public void initialize() {
    	boolean connected = false;
    	try{
    		connected = this.connect();
    	}catch(Exception e){
    		LOG.debug("Error while initializing data source", e);
    		String msg = e.getMessage();
    		if(msg == null){
    			msg = "Unknown";
    		}
			raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("file.event.readFailed",msg));
			
    	}
    	
    	if(connected){
    		returnToNormal(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis());
    	}
        super.initialize();
    	
    }
    @Override
    public void terminate() {
        super.terminate();
        if(this.file != null) {
			try {
				this.fobs.destroy();
			} catch (Exception e) {
				LOG.debug("Error destroying file observer");
				raiseEvent(DATA_SOURCE_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("file.event.obsDestroy", e.getMessage()));
			}
			this.file = null;
		}

    }
    
	@Override
	public void setPointValue(DataPointRT dataPoint, PointValueTime valueTime,
			SetPointSource source) {
		//TODO: Enable Regex replace
		//no-op
	}
	
	@Override
	public void onDirectoryChange(File dir) {
		fileEvent();
	}
	
	@Override
	public void onDirectoryCreate(File dir) {
		//no-op
	}
	
	@Override
	public void onDirectoryDelete(File dir) {
		//no-op
	}
	
	@Override
	public void onFileCreate(File f) {
		//no-op
	}
	
	@Override
	public void onFileDelete(File f) {
		//no-op
	}
	
	@Override
	public void onFileChange(File f) {
		fileEvent();
	}
	
	@Override
	public void onStart(FileAlterationObserver obs) {
		fileEvent();
	}
	
	@Override
	public void onStop(FileAlterationObserver obs) {
		//no-op
	}

	private void fileEvent() {
		//Should never happen
		if(this.file == null) {
			raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("file.event.readFailedFileNotSetup"));
			return;
		}
		
		
		//The file is modified or we've just started, so read it.
		try{
			BufferedReader reader = new BufferedReader(new FileReader(this.file));

			String msg;
            if(!this.dataPoints.isEmpty()) {
					
            	//TODO optimize to be better than numLines*numPoints
            	while( (msg = reader.readLine()) != null) {
					//Give all points the chance to find their data
					for(DataPointRT dp: this.dataPoints){
						AsciiFilePointLocatorRT pl = dp.getPointLocator();
						AsciiFilePointLocatorVO plVo = pl.getVo();
						Pattern pointValuePattern = pl.getValuePattern();
						Matcher pointValueMatcher = pointValuePattern.matcher(msg); //Use the index from the above message
						if(pointValueMatcher.find()){
							if(plVo.getPointIdentifierIndex() > pointValueMatcher.groupCount() || plVo.getValueIndex() > pointValueMatcher.groupCount()) {
								raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), false, new TranslatableMessage("file.event.insufficientGroups", dp.getVO().getExtendedName()));
							}
							else if(plVo.getPointIdentifier().equals(pointValueMatcher.group(plVo.getPointIdentifierIndex()))) {
								String value = pointValueMatcher.group(plVo.getValueIndex());                	
								PointValueTime newValue;
								Date dt;
								if(plVo.getHasTimestamp() && !plVo.getTimestampFormat().equals(".")) {
									SimpleDateFormat fmt = new SimpleDateFormat(plVo.getTimestampFormat());
									dt = fmt.parse(pointValueMatcher.group(plVo.getTimestampIndex()));
								}
								else if(plVo.getHasTimestamp()) {
									dt = new Date(Long.parseLong(pointValueMatcher.group(plVo.getTimestampIndex())));
								}
								else {
									dt = new Date();
								}
								
								//Switch on the type
								switch(plVo.getDataType()){
								case DataTypes.ALPHANUMERIC:
									newValue = new PointValueTime(value, dt.getTime());
									break;
								case DataTypes.NUMERIC:
									newValue = new PointValueTime(Double.parseDouble(value), dt.getTime());
									break;
								case DataTypes.MULTISTATE:
									newValue = new PointValueTime(Integer.parseInt(value), dt.getTime());
									break;
								case DataTypes.BINARY:
									newValue = new PointValueTime(Boolean.parseBoolean(value), dt.getTime());
									break;
								default:
									throw new ShouldNeverHappenException("Uknown Data type for point");
								}
								
								if(!plVo.getHasTimestamp())
									dp.updatePointValue(newValue);
								else
									dp.savePointValueDirectToCache(newValue, null, true, true);
							}
						}
					}
            	}
			}
            reader.close();
            returnToNormal(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis());
        }catch ( FileNotFoundException e ){
			raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("file.event.fileNotFound",e.getMessage()));
        } catch (IOException e) {
        	raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("file.event.readFailed",e.getMessage()));
		} catch (NumberFormatException e) {
			raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("file.event.notNumber",e.getMessage()));
		} catch (ParseException e) {
			raiseEvent(POINT_READ_EXCEPTION_EVENT, System.currentTimeMillis(), true, new TranslatableMessage("file.event.dateParseFailed",e.getMessage()));
		}
		
	}

	@Override
	protected void doPoll(long time) {
		if(fobs != null)
			fobs.checkAndNotify();

	}

	
	
	
	
}
