/*
    Copyright (C) 2014 Infinite Automation Systems Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.sqlConsole;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.View;
import org.springframework.web.util.WebUtils;

import com.serotonin.db.spring.ConnectionCallbackVoid;
import com.serotonin.db.spring.ExtendedJdbcTemplate;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.web.mvc.UrlHandler;
import com.serotonin.m2m2.web.mvc.controller.ControllerUtils;
import com.serotonin.util.SerializationHelper;

public class SqlController implements UrlHandler {
    private static final Log LOG = LogFactory.getLog(SqlController.class);

    @Override
    public View handleRequest(HttpServletRequest request, HttpServletResponse response, final Map<String, Object> model)
            throws Exception {
        Permissions.ensureAdmin(request);
        model.put("updateResult", -1);

        try {
            final String serializedDataMsg = ControllerUtils.translate(request, "sql.serializedData");
            final String sqlString = request.getParameter("sqlString");

            model.put("sqlString", sqlString);
            model.put("updateResult", -1);

            if (WebUtils.hasSubmitParameter(request, "query"))
                query(sqlString, serializedDataMsg, model);
            else if (WebUtils.hasSubmitParameter(request, "update")) {
                ExtendedJdbcTemplate ejt = new ExtendedJdbcTemplate();
                ejt.setDataSource(Common.databaseProxy.getDataSource());
                int result = ejt.update(sqlString);
                model.put("updateResult", result);
            }
            else if (WebUtils.hasSubmitParameter(request, "tables"))
                query(Common.databaseProxy.getTableListQuery(), serializedDataMsg, model);
        }
        catch (RuntimeException e) {
            model.put("error", e.getMessage());
            LOG.warn("", e);
        }

        return null;
    }

    private void query(final String sqlString, final String serializedDataMsg, final Map<String, Object> model) {
        Common.databaseProxy.doInConnection(new ConnectionCallbackVoid() {
            @Override
            public void doInConnection(Connection conn) throws SQLException {
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sqlString);

                ResultSetMetaData meta = rs.getMetaData();
                int columns = meta.getColumnCount();
                List<String> headers = new ArrayList<String>(columns);
                for (int i = 0; i < columns; i++)
                    headers.add(meta.getColumnLabel(i + 1));

                List<List<Object>> data = new LinkedList<List<Object>>();
                List<Object> row;
                while (rs.next()) {
                    row = new ArrayList<Object>(columns);
                    data.add(row);
                    for (int i = 0; i < columns; i++) {
                        if (meta.getColumnType(i + 1) == Types.CLOB)
                            row.add(rs.getString(i + 1));
                        else if (meta.getColumnType(i + 1) == Types.LONGVARBINARY
                                || meta.getColumnType(i + 1) == Types.BLOB) {
                            Blob blob = rs.getBlob(i + 1);
                            Object o;
                            if (blob == null)
                                o = null;
                            else
                                o = SerializationHelper.readObjectInContext(blob.getBinaryStream());
                            row.add(serializedDataMsg + "(" + o + ")");
                        }
                        else
                            row.add(rs.getObject(i + 1));
                    }
                }

                model.put("headers", headers);
                model.put("data", data);
            }
        });
    }
}
