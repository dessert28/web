package com.zjsru.mapper;

import com.zjsru.domain.Users;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {
    //查询 所有用户信息（mybatis 库的登录表 tb_users）

    @Select("SELECT * FROM tb_users")
    List<Users> selectAllUsers();

    // 按用户名查询用户（登录校验使用）
    @Select("SELECT * FROM tb_users WHERE username = #{username}")
    Users selectByUsername(@Param("username") String username);

    // 按主键查询用户
    @Select("SELECT * FROM tb_users WHERE id = #{id}")
    Users selectUserById(@Param("id") Integer id);

    // 按用户名和密码查询用户（用户名 + 明文密码，课程演示用）
    @Select("SELECT * FROM tb_users WHERE username = #{username} AND password = #{password}")
    Users selectUserByNameAndPassword(@Param("username") String username, @Param("password") String password);
}
