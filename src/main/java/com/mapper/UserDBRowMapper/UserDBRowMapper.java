package com.mapper.UserDBRowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.model.User;

public class UserDBRowMapper implements RowMapper<User>{

	 @Override
	    public User mapRow(ResultSet resultSet, int rowNum) throws SQLException {
	        User user = new User();
	        
	        user.setId(resultSet.getInt("id"));
	        user.setDept(resultSet.getString("dept"));
	        user.setName(resultSet.getString("name"));
	        user.setSalary(resultSet.getInt("salary"));
	        user.setTime(resultSet.getTime("time"));
	        
	        return user;
	    }
}
