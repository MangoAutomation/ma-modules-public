package com.serotonin.m2m2.dataImport;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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
                        try {
                            int count = importCsv(item);
                            model.put("result", new TranslatableMessage("dataImport.import.imported", count)
                                    .translate(translations));
                        }
                        catch (TranslatableException e) {
                            model.put("error", e.getTranslatableMessage().translate(translations));
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

    private int importCsv(FileItem item) throws IOException, TranslatableException {
        CSVReader csvReader = new CSVReader(new InputStreamReader(item.getInputStream()));
        DataPointDao dataPointDao = new DataPointDao();
        PointValueDao pointValueDao = Common.databaseProxy.newPointValueDao();
        DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss");

        // Basic validation
        String[] nextLine = csvReader.readNext();
        if (nextLine == null)
            throw new TranslatableException(new TranslatableMessage("dataImport.import.noData"));
        if (nextLine.length < 2)
            throw new TranslatableException(new TranslatableMessage("dataImport.import.noPoints"));

        // Find the points by XID
        DataPointVO[] vos = new DataPointVO[nextLine.length - 1];

        for (int i = 1; i < nextLine.length; i++) {
            if (StringUtils.isBlank(nextLine[i]))
                throw new TranslatableException(new TranslatableMessage("dataImport.import.badXid", i));

            DataPointVO vo = dataPointDao.getDataPoint(nextLine[i]);
            if (vo == null)
                throw new TranslatableException(new TranslatableMessage("dataImport.import.xidNotFound", nextLine[i]));

            vos[i - 1] = vo;
        }

        // Find the RTs for the points if they are enabled
        DataPointRT[] rts = new DataPointRT[vos.length];
        for (int i = 0; i < vos.length; i++)
            rts[i] = Common.runtimeManager.getDataPoint(vos[i].getId());

        // Import the data
        int count = 0;
        while ((nextLine = csvReader.readNext()) != null) {
            // The first value is always a date.
            long time = dtf.parseDateTime(nextLine[0]).getMillis();

            // The rest of the values are point samples.
            for (int i = 1; i < nextLine.length; i++) {
                DataValue value = DataValue.stringToValue(nextLine[i], vos[i - 1].getPointLocator().getDataTypeId());
                PointValueTime pvt = new PointValueTime(value, time);

                if (rts[i - 1] != null)
                    rts[i - 1].savePointValueDirectToCache(pvt, null, true, true);
                else
                    // Save directly to the database
                    pointValueDao.savePointValueAsync(vos[i - 1].getId(), pvt, null);
            }

            count++;
        }

        return count;
    }
}
