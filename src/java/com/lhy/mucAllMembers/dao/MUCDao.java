package com.lhy.mucAllMembers.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.database.DbConnectionManager;

/**
*
* 根据用户名查询出用户所在会议室的信息    
*/
public class MUCDao {
	
	public static List<Map<String, String>> getMUCInfo(String jid) {
		
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		StringBuilder sqlBuilder = new StringBuilder();
		
		sqlBuilder.append("select ofmucroom.serviceID, ofmucroom.name, ofmucroom.roomid ,ofmucmember.nickname from ");
		sqlBuilder.append(" ofmucroom join ofmucmember on ofmucroom.roomID = ofmucmember.roomID and ofmucmember.jid = ?");
		sqlBuilder.append(" union ");
		sqlBuilder.append(" select ofmucroom.serviceID, ofmucroom.name, ofmucroom.roomid ,null from ");
		sqlBuilder.append(" ofmucroom  join ofmucaffiliation on ofmucroom.roomID = ofmucaffiliation.roomID and ofmucaffiliation.jid = ?");
		
		Connection connection = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Map<String, String> map = null;
		try {
			connection = DbConnectionManager.getConnection();
			statement = connection.prepareStatement(sqlBuilder.toString());
			statement.setString(1, jid);
			statement.setString(2, jid);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				map = new HashMap<String, String>();
				map.put("serviceID", resultSet.getString(1));
				map.put("name", resultSet.getString(2));
				map.put("roomid", resultSet.getString(3));
				map.put("nickname", resultSet.getString(4));
				list.add(map);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		} finally {
			DbConnectionManager.closeConnection(resultSet, statement,
					connection);
		}
		return list;
	}
}
