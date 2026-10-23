package com.zjsru.mapper;

import com.zjsru.domain.Users;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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

    // 按用户名和密码查询用户（用户名 + 明文密码）
    @Select("SELECT * FROM tb_users WHERE username = #{username} AND password = #{password}")
    Users selectUserByNameAndPassword(@Param("username") String username, @Param("password") String password);

    // 新增用户（id 自增不显式写入），返回受影响行数
    @Insert("INSERT INTO tb_users(username, password, rolename, status, phone) " +
            "VALUES(#{username}, #{password}, #{rolename}, #{status}, #{phone})")
    int insertUser(Users user);

    // 按主键更新用户全部字段，返回受影响行数
    @Update("UPDATE tb_users SET username = #{username}, password = #{password}, " +
            "rolename = #{rolename}, status = #{status}, phone = #{phone} WHERE id = #{id}")
    int updateUser(Users user);

    // 按主键删除用户，返回受影响行数
    @Delete("DELETE FROM tb_users WHERE id = #{id}")
    int deleteUserById(@Param("id") Long id);
}
