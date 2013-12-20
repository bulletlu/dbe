package cn.com.qimingx.dbe.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author inc062805
 * 
 * 临时工作目录工具类
 */
public class QueryResultSetCache{
	// logger
	private static final Log log = LogFactory.getLog(QueryResultSetCache.class);

	// 当前工作目录的根目录
	private Map<String,ResultSet> cache;
	
	public QueryResultSetCache(){
		cache = new HashMap<String,ResultSet>();
	}
	
	public void put(String sql,ResultSet rs){
		ResultSet rss = cache.get(sql);
		try {
			if(rss != null && !rss.isClosed()){
				rss.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		cache.put(sql, rs);
	}
	
	public ResultSet get(String sql){
		return cache.get(sql);
	}
	
	public void destroy(){
		for(String sql : cache.keySet()){
			ResultSet rs = cache.get(sql);
			try {
				if(rs != null && !rs.isClosed()) rs.close();
				log.debug("close resultset of " + sql);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
}
