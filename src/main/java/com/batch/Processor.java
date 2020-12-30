package com.batch;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import com.model.User;
import com.model.User_Temp;

@Component
public class Processor implements ItemProcessor<User, User_Temp> {

    private static final Map<String, String> DEPT_NAMES =
            new HashMap<>();

    public Processor() {
        DEPT_NAMES.put("001", "Technology");
        DEPT_NAMES.put("002", "Operations");
        DEPT_NAMES.put("003", "Accounts");
    }

    @Override
    public User_Temp process(User user) throws Exception {
    //	System.out.println("inside processor 1");
    	User_Temp user1=new User_Temp();
        String deptCode = user.getDept();
        String dept = DEPT_NAMES.get(deptCode);
        user1.setDept(dept);
        user1.setTime(new Date());
        user1.setSalary(user.getSalary()+2);
        System.out.println(String.format("Converted from [%s] to [%s]", deptCode, dept));
        return user1;
    }
}
