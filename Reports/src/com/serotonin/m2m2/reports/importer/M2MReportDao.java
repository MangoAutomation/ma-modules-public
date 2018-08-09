/**
 * Copyright (C) 2015 Infinite Automation Software. All rights reserved.
 * @author Terry Packer
 */
package com.serotonin.m2m2.reports.importer;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.RowMapper;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.db.MappedRowCallback;
import com.serotonin.m2m2.db.DatabaseProxy.DatabaseType;
import com.serotonin.m2m2.reports.ReportDao;

/**
 * @author Terry Packer
 *
 */
public class M2MReportDao {
	
	private static final Log LOG = LogFactory.getLog(M2MReportDao.class);
	
	private static final String REPORT_SELECT = "select data, id, userId, name from reports ";
	private static final String DATA_POINT_XID_SELECT = "select xid from dataPoints ";
	private static final String USER_NAME_SELECT = "select username from users ";
    private static final String MAILING_LIST_XID_SELECT = "select xid from mailingLists ";

	private Connection connection;
	private DatabaseType databaseType;
	
	private final Map<String,String> mappedClasses = new HashMap<String,String>();
	private final ReportDao mangoDao;
	
	public M2MReportDao(Connection connection, DatabaseType databaseType){
		this.connection = connection;
		this.databaseType = databaseType;
		
		mappedClasses.put("com.serotonin.mango.vo.report.ReportVO", M2MReportVO.class.getCanonicalName());
		mappedClasses.put("com.serotonin.mango.vo.report.ReportPointVO", M2MReportPointVO.class.getCanonicalName());
		mappedClasses.put("com.serotonin.mango.web.dwr.beans.RecipientListEntryBean", M2MRecipientListEntryBean.class.getCanonicalName());
		
		this.mangoDao = ReportDao.getInstance();
	}
	
	public void close() throws SQLException{
		this.connection.commit();
		this.connection.close();
	}

	
	public List<M2MReportVO> getReports(){
		return query(REPORT_SELECT, new M2MReportRowMapper());
	}
	
	/**
	 * @return
	 * @throws SQLException 
	 */
	public Statement createStatement() throws SQLException {
		Statement stmt = this.connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		stmt.setQueryTimeout(100000000);			

		//For MySQL Only!!!
		switch(databaseType){
			case MYSQL:
				//Coax the connection to stream data
				stmt.setFetchSize(Integer.MIN_VALUE);
			break;
			default:
			break;
		}
		return stmt;
	}


    /**
	 * @param string
	 * @param objects
	 * @param pointValueRowMapper
	 * @param callback
	 */
	protected void query(String sql,
			M2MReportRowMapper rowMapper,
			MappedRowCallback<M2MReportVO> callback) {
		
		Statement stmt = null;
		ResultSet rs = null;
		
		try {
			stmt = createStatement();
			
			rs = stmt.executeQuery(sql);
			int i = 0;
			while(rs.next()){
				M2MReportVO pvt = rowMapper.mapRow(rs, i);
				callback.row(pvt, i);
				i++;
			}
		} catch (SQLException e) {
			LOG.error(e.getMessage(), e);
			throw new ShouldNeverHappenException(e);
		} finally{
			if(rs != null){
				try {
					rs.close();
				} catch (SQLException e) {
					LOG.error(e.getMessage(), e);
				}
			}
			if(stmt != null){
				try {
					stmt.close();
				} catch (SQLException e1) {
					LOG.error(e1.getMessage(), e1);
				}
			}
		}
			
	}
    
	
	private List<M2MReportVO> query(String sql,
			M2MReportRowMapper rowMapper) {
		
		Statement stmt = null;
		ResultSet rs = null;
		List<M2MReportVO> reports = new ArrayList<M2MReportVO>();
		try {
			stmt = createStatement();
			
			rs = stmt.executeQuery(sql);
			int i=0;
			while(rs.next()){
				M2MReportVO report = rowMapper.mapRow(rs, i);
				reports.add(report);
				i++;
			}
		} catch (SQLException e) {
			LOG.error(e.getMessage(), e);
			throw new ShouldNeverHappenException(e);
		} finally{
			if(rs != null){
				try {
					rs.close();
				} catch (SQLException e) {
					LOG.error(e.getMessage(), e);
				}
			}
			if(stmt != null){
				try {
					stmt.close();
				} catch (SQLException e1) {
					LOG.error(e1.getMessage(), e1);
				}
			}
		}
		
		return reports;
	}

	class M2MReportRowMapper implements RowMapper<M2MReportVO>{
		@Override
		public M2MReportVO mapRow(ResultSet rs, int rowNum) throws SQLException{
            int i = 0;
            try {
				M2MConversionInputStream is = new M2MConversionInputStream(rs.getBinaryStream(++i), mappedClasses);
	            M2MReportVO report = (M2MReportVO) is.readObject(); //SerializationHelper.readObject(rs.getBlob(++i).getBinaryStream());
	            is.close();
	            report.setId(rs.getInt(++i));
	            report.setUserId(rs.getInt(++i));
	            report.setName(rs.getString(++i));
	            return report;
			} catch (IOException | ClassNotFoundException e) {
				throw new SQLException(e);
			}

		}
	}
	

    
    protected String createDelimitedList(Collection<?> values, String delimeter, String quote) {
        StringBuilder sb = new StringBuilder();
        Iterator<?> iterator = values.iterator();
        boolean first = true;
        while (iterator.hasNext()) {
            if (first)
                first = false;
            else
                sb.append(delimeter);

            if (quote != null)
                sb.append(quote);

            sb.append(iterator.next());

            if (quote != null)
                sb.append(quote);
        }
        return sb.toString();
    }

	/**
	 * 
	 */
	public Connection getConnection() {
		return this.connection;
	}

	/**
	 * @param pointId
	 * @return
	 */
	public String getDataPointXid(int pointId) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
        stmt = this.connection.prepareStatement(DATA_POINT_XID_SELECT + " where id = ?");
        stmt.setInt(1, pointId);
        stmt.execute();
        rs = stmt.getResultSet();
        
        if(rs.next()){
        	return rs.getString(1);
        }else
        	return null;
        
        } catch (SQLException e) {
        	LOG.error(e.getMessage(), e);
			throw new ShouldNeverHappenException(e);
		} finally{
			if(rs != null){
				try {
					rs.close();
				} catch (SQLException e) {
					LOG.error(e.getMessage(), e);
				}
			}
			if(stmt != null){
				try {
					stmt.close();
				} catch (SQLException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * @param userId
	 * @return
	 */
	public String getUsername(int userId) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
        stmt = this.connection.prepareStatement(USER_NAME_SELECT + " where id = ?");
        stmt.setInt(1, userId);
        stmt.execute();
        rs = stmt.getResultSet();
        
        if(rs.next()){
        	return rs.getString(1);
        }else
        	return null;
        
        } catch (SQLException e) {
        	LOG.error(e.getMessage(), e);
			throw new ShouldNeverHappenException(e);
		} finally{
			if(rs != null){
				try {
					rs.close();
				} catch (SQLException e) {
					LOG.error(e.getMessage(), e);
				}
			}
			if(stmt != null){
				try {
					stmt.close();
				} catch (SQLException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
	}

	/**
	 * @param pointId
	 * @return
	 */
	public String getMailingListXid(int listId) {
	    PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
        stmt = this.connection.prepareStatement(MAILING_LIST_XID_SELECT + " where id = ?");
        stmt.setInt(1, listId);
        stmt.execute();
        rs = stmt.getResultSet();
        
        if(rs.next()){
        	return rs.getString(1);
        }else
        	return null;
        
        } catch (SQLException e) {
        	LOG.error(e.getMessage(), e);
			throw new ShouldNeverHappenException(e);
		} finally{
			if(rs != null){
				try {
					rs.close();
				} catch (SQLException e) {
					LOG.error(e.getMessage(), e);
				}
			}
			if(stmt != null){
				try {
					stmt.close();
				} catch (SQLException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
	}

	//These are unique for only the life of this instance
	private List<String> usedXids = new ArrayList<String>();
	/**
	 * @return
	 */
	public String generateUniqueXid() {
		
		String xid = this.mangoDao.generateUniqueXid();
		while(usedXids.contains(xid))
			xid = this.mangoDao.generateUniqueXid();
		
		usedXids.add(xid);
		return xid;
		
	}
	
}
