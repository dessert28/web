package com.zjsru.mapper;

import com.zjsru.domain.Users;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {
    //查询 所有用户信息
    
    @Select("SELECT * FROM users")
    List<Users> selectAllUsers();

    // 按用户名查询用户
    @Select("SELECT * FROM users WHERE username = #{username}")
    Users selectByUsername(@Param("username") String username);
}
