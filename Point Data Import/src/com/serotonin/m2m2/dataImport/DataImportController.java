package com.serotonin.m2m2.dataImport;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.View;

import au.com.bytecode.opencsv.CSVReader;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.i18n.TranslatableException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.export.ExportCsvStreamer;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.UrlHandler;
import com.serotonin.m2m2.web.mvc.controller.ControllerUtils;

public class DataImportController implements UrlHandler {
    @Override
    @SuppressWarnings("unchecked")
    public View handleRequest(HttpServletRequest request, HttpServletResponse response, final Map<String, Object> model)
            throws Exception {
        Permissions.ensureAdmin(request);

        if (ServletFileUpload.isMultipartContent(request)) {
            Translations translations = ControllerUtils.getTranslations(request);

            ServletFileUpload upload = new ServletFileUpload(new DiskFileItemFactory(100, null));
            List<FileItem> items;
            try {
                items = upload.parseRequest(request);

                for (FileItem item : items) {
                    if ("uploadFile".equals(item.getFieldName())) {
                    	CSVReader csvReader = new CSVReader(new InputStreamReader(item.getInputStream()));
                        try {
                            int count = importCsv(csvReader);
                            model.put("result", new TranslatableMessage("dataImport.import.imported", count)
                                    .translate(translations));
                        }
                        catch (TranslatableException e) {
                            model.put("error", e.getTranslatableMessage().translate(translations));
                        }finally{
                        	csvReader.close();
                        }
                    }
                }
            }
            catch (Exception e) {
                model.put("error",
                        new TranslatableMessage("dataImport.upload.exception", e.getMessage()).translate(translations));
            }
        }

        return null;
    }

    /**
     * The file needs to be in the format:
     * 
     * Data Point XID, Device Name, Point name, Time, Value, Rendered, Annotation, Modify(Not used yet)
     * 
     * 
     * @param item
     * @return
     * @throws IOException
     * @throws TranslatableException
     */
    private int importCsv(CSVReader csvReader) throws IOException, TranslatableException {
        
        DataPointDao dataPointDao = new DataPointDao();
        PointValueDao pointValueDao = Common.databaseProxy.newPointValueDao();

        // Basic validation of header
        String[] nextLine = csvReader.readNext();
        if (nextLine == null)
            throw new TranslatableException(new TranslatableMessage("dataImport.import.noData"));
        if (nextLine.length != ExportCsvStreamer.columns)
            throw new TranslatableException(new TranslatableMessage("dataImport.import.invalidHeaders", nextLine.length, ExportCsvStreamer.columns));

        //Map of XIDs to non-running data points
        Map<String, DataPointVO> voMap = new HashMap<String, DataPointVO>();
        //Map of XIDs to running data points
        Map<String, DataPointRT> rtMap = new HashMap<String, DataPointRT>();
        
        //Read in all the rows
        int row = 1;
        String xid;
        DataPointVO vo;
        DataPointRT rt;
        long time;
        DataValue value;
        PointValueTime pvt;

        while ((nextLine = csvReader.readNext()) != null) {
        	//Check XID
        	xid = nextLine[0];
        	if (StringUtils.isBlank(xid))
                throw new TranslatableException(new TranslatableMessage("dataImport.import.badXid", xid, row));
        	
        	//First Check to see if we already have a point
        	vo = voMap.get(xid);
        	rt = rtMap.get(xid);
        	
        	//We will always have the vo in the map but the RT may be null if the point isn't running
        	if(vo == null){
        		vo = dataPointDao.getDataPoint(xid);
	            if (vo == null)
	                throw new TranslatableException(new TranslatableMessage("dataImport.import.xidNotFound", xid, row));
	        	rt = Common.runtimeManager.getDataPoint(vo.getId());
	
        		rtMap.put(xid, rt);
        		voMap.put(xid, vo);
        	}
        	
        	//Going to insert some data
            time = ExportCsvStreamer.dtf.parseDateTime(nextLine[3]).getMillis();
            value = DataValue.stringToValue(nextLine[4], vo.getPointLocator().getDataTypeId());
            pvt = new PointValueTime(value, time);
        	
        	if(rt == null){
        		//Insert Via DAO
        		pointValueDao.savePointValueAsync(vo.getId(), pvt, null);
        	}else{
        		//Insert Via RT
        		rt.savePointValueDirectToCache(pvt, null, true, true);
        	}

        	row++;
        }
 
        return row - 2; //Header plus one for loop droput after count
    }
}
