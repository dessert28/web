package com.zjsru.mapper;

import com.zjsru.domain.Users;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
/*
* mapper接口仅限于数据访问层，不做业务逻辑处理*/
@Mapper
public interface UserMapper {
    //查询所有用户信息
    @Select("SELECT * FROM tb_users")
    List<Users> selectAllUsers();

    //根据用户ID查询用户信息
    @Select("select * from tb_users where id = #{id}")
    Users selectUserById(Integer id);

    //根据用户名和密码查询用户信息
    @Select("select * from tb_users where username = #{uname} and password = #{pwd}")
    Users selectUserByNameAndPassword(String uname,String pwd);

    //添加一个用户信息
    @Insert("insert into tb_users(username, password, email) values(#{username}, #{password}, #{email})")
    void insertUser(Users user);
}
