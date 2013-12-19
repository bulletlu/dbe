package cn.com.qimingx.dbe;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.com.qimingx.utils.MyUtils;
import cn.com.qimingx.utils.SQLTypeUtils;

public class QueryResultSetCache {
	
	private Map<String,List<Map<String, Object>>> cache;
	
	private Map<String,List<TableColumnInfo>> rsColums;
	
	public QueryResultSetCache(){
		cache = new HashMap<String,List<Map<String, Object>>>();
		rsColums = new HashMap<String,List<TableColumnInfo>>();
	}
	
	public boolean isNeedToCacheIn(String sql, int start){
		List<Map<String, Object>> rows = this.cache.get(sql);
		if(rows == null || start == 0){
			return true;
		}
		return false;
	}
	
	public List<TableColumnInfo> getColumns(String sql){
		return rsColums.get(sql);
	}
	
	
	//
	public void cache(String sql, ResultSet rs,List<TableColumnInfo> columns) throws SQLException{
		this.rsColums.put(sql, columns);
		List<Map<String, Object>> rows = this.cache.get(sql);
		if(rows == null){
			rows = new ArrayList<Map<String, Object>>();
			this.cache.put(sql, rows);
		}else{
			rows.clear();
		}
		// read
		while (rs.next()) {
			Map<String, Object> row = new HashMap<String, Object>();
			for (TableColumnInfo column : columns) {
				Object value = readFieldValue(rs, column);
				row.put(column.getName(), value);
			}
			rows.add(row);
		}
	}
	
	public TableDataInfo getTableDataInfoByCache(String sql,int start, int limit)	{
		// init..
		List<Map<String, Object>> rows = this.cache.get(sql);
		List<Map<String, Object>> page = new ArrayList<Map<String, Object>>(limit + 10);
		
		if(rows == null){
			new TableDataInfo(0, page);
		}
		//log.debug("sql :"+sql+ " all rows:"+rows+"  count:"+rows.size());
		// read
		for(int i=0;i<rows.size() && i<start+limit;i++){
			if(i<start){
				continue;
			}
			page.add(rows.get(i));
		}
		// return
		return new TableDataInfo(rows.size(), page);
	}
	
	private Object readFieldValue(ResultSet rs, TableColumnInfo column)
			throws SQLException {
		int type = column.getType();
		String field = column.getName();
		// 读取字段值
		Object value = null;
		if (SQLTypeUtils.isDateType(type)) {
			value = rs.getTimestamp(field);
		} else if (SQLTypeUtils.isNumberType(type)) {
			value = rs.getString(field);
		} else if (SQLTypeUtils.isStringType(type)) {
			value = rs.getString(field);
		} else if (SQLTypeUtils.isBlobType(type)) {
			value = "[LONGVARBINARY]";
		} else if (SQLTypeUtils.isClobType(type)) {
			value = "[LONGVARCHAR]";
		} else {
			value = "[" + SQLTypeUtils.getJdbcTypeName(type) + "]";
		}
		// 处理 NULL 值
		if (rs.wasNull() && !SQLTypeUtils.isNumberType(type)
				&& !SQLTypeUtils.isDateType(type)) {
			value = "[NULL]";
		} else {
			if (SQLTypeUtils.isStringType(type)) {
				// 检查是否是html内容...
				if (MyUtils.isHTMLContent(value.toString())) {
					value = "[HTML]";
				}
			}
		}
		return value;
	}

}
