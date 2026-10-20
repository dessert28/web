package com.zjsru.mapper;

import com.zjsru.domain.Users;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {
    //查询 所有用户信息
    
    @Select("SELECT * FROM users")
    List<Users> selectAllUsers();
}
