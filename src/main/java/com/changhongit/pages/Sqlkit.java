package com.changhongit.pages;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry5.EventConstants;
import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.OnEvent;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;

import com.changhongit.bean.Bean;
import com.changhongit.bean.LBean;
import com.changhongit.components.Pages;
import com.changhongit.tool.log.LogTool;
import com.changhongit.tool.sql.C3P0Pool;
import com.changhongit.tool.string.StringUtil;

public class Sqlkit {
   
	private static final String UPDATE = "update";

	private static final String QUERY = "query";

	/**
	 * 变量描述：查询的行结果集
	 */
	@Property
	@Persist
	private List<LBean> list;
	
	/**
	 * 变量描述：每一行的结果集
	 */
	@Property
	@Persist
	private LBean lbean;
	
	/**
	 * 变量描述：每一行
	 */
	@Property
	@Persist
	private Bean bean;
	
	/**
	 * 变量描述：查询Form
	 */
	@Component
	private Form sqlForm;
	
	/**
	 * 变量描述：查询时列名称
	 */
	@Property
	@Persist
	private String column;
	
	/**
	 * 变量描述：查询时所有列集合
	 */
	@Property
	@Persist
	private List<String> columns;
	
	/**
	 * 变量描述：查询sql语句
	 */
	@Property
	@Persist
	private String sql;
	
	/**
	 * 变量描述：数据库连接标识符
	 */
	@Property
	@Persist
	private String dbFlag;
	
	/**
	 * 变量描述：（分页用）当前页
	 */
	@Persist
	@Property
	private int curpage;

	/**
	 * 变量描述：（分页用）总记录数
	 */
	@Property
	@Persist
	private int totalRow;
	
	/**
	 * 变量描述：操作类型
	 */
	@Property
	@Persist
	private String operation;
	
	/**
	 * 变量描述：更新时影响记录数
	 */
	@Property
	@Persist
	private int reflectCount;

	/**
	 * 变量描述：（分页用）分页组件
	 */
	@Component(parameters = { "rowsPerPage=pagenum", "totalRows=totalRow", "currentPage=curpage" })
	private Pages pager;
	
	@OnEvent(value = EventConstants.SUBMIT, component = "sqlForm")
	public Object submit(){
		curpage = 1;
		if(StringUtil.isBlank(sql) || sql.trim().length() == 0){
			sqlForm.recordError("sql is blank");
		} else {
			String _sql = sql.trim().toLowerCase();
			if(_sql.startsWith("select")){
				select();
				operation = QUERY;
			} else if(_sql.startsWith("update") || _sql.startsWith("insert")){
				reflectCount = update();
				operation = UPDATE;
			}
		}
		return null;
	}
	
	/**
	 * 执行更新 或者 插入操作
	 */
	private int update() {
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = C3P0Pool.getInstance().getConnection(dbFlag);
			stmt = conn.createStatement();
			return stmt.executeUpdate(sql);
		} catch(SQLException e){
			LogTool.error(e, getClass());
			e.printStackTrace();
		} finally {
			close(stmt);
			close(conn);
		}
		return 0;
	}

	/**
	 * 执行sql查询，返回结果集
	 */
	private ResultSet getResultSet(Connection conn, Statement stmt, String sql){
		ResultSet rs = null;
		try {
			rs = stmt.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}
	
	/**
	 * 获取结果集数据源
	 */
	private ResultSetMetaData getMetaData(ResultSet rs){
		ResultSetMetaData rsmd = null;
		try {
			rsmd = rs.getMetaData();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rsmd;
	}
	
	/**
	 * 执行查询操作
	 */
	private void select(){
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = C3P0Pool.getInstance().getConnection(dbFlag);
			stmt = conn.createStatement();
			
			setTotal(); 
			
			// 构建分页sql语句
			String _sql = new String(sql);
			_sql += " limit " + (curpage - 1) * getPagenum() + ", " + getPagenum() + " ";
			
			LogTool.info("查询sql:" + _sql, getClass());
			
			rs = getResultSet(conn, stmt, _sql);
			ResultSetMetaData rsmd = getMetaData(rs);
			
			columns = new ArrayList<String>();
			// 获取所有字段列
			int columnCount = rsmd.getColumnCount();
			for (int i = 1; i <= columnCount; i++) {
				String columnName = rsmd.getColumnName(i);
				columns.add(columnName);
			}
			
			list = new ArrayList<LBean>();
			// 获取字段值
			while(rs.next()){
				LBean lb = new LBean();
				List<Bean> beans = new ArrayList<Bean>();
				for(String column : columns){
					Object columnValue = rs.getObject(column);
					Bean bean = new Bean(column, columnValue);
					beans.add(bean);
				}
				lb.setBeans(beans);
				list.add(lb);
			}
		} catch (SQLException e) {
			LogTool.error(e, getClass());
			e.printStackTrace();
		} finally {
			close(rs);
			close(stmt);
			close(conn);
		}
	}
	
	/**
	 * 获取总记录数
	 */
	private void setTotal(){
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn = C3P0Pool.getInstance().getConnection(dbFlag);
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			rs.last();
			totalRow = rs.getRow();
		} catch(SQLException e){
			LogTool.error(e, getClass());
			e.printStackTrace();
		} finally {
			close(rs);
			close(stmt);
			close(conn);
		}
	}
	
	// 分页连接
	@OnEvent(value = EventConstants.ACTION, component = "pager")
	void changePager(int pages) {
		curpage = pages;
		select();
	}
	
	/**
	 * 每页大小
	 */
	public int getPagenum(){
		return 5;
	}
	
	/**
	 * 关闭Connection
	 */
	private void close(Connection conn){
		try {
			if(conn != null){
				conn.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 关闭Statement
	 */
	private void close(Statement stmt){
		try {
			if(stmt != null){
				stmt.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 关闭ResultSet
	 */
	private void close(ResultSet rs){
		try {
			if(rs != null){
				rs.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}