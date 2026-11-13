package com.zjsru.hospital.mapper;

import com.zjsru.hospital.domain.Users;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 患者账号数据访问（tb_users）。
 * mapper 接口只做数据访问，不做业务逻辑。
 */
@Mapper
public interface UserMapper {

    // 查询所有用户信息
    @Select("SELECT * FROM tb_users")
    List<Users> selectAllUsers();

    // 根据用户ID查询用户信息
    @Select("select * from tb_users where id = #{id}")
    Users selectUserById(Integer id);

    // 根据用户名和密码查询用户信息
    @Select("select * from tb_users where username = #{uname} and password = #{pwd}")
    Users selectUserByNameAndPassword(@Param("uname") String uname, @Param("pwd") String pwd);

    // 添加一个用户信息
    @Insert("insert into tb_users(username, password, email) values(#{username}, #{password}, #{email})")
    void insertUser(Users user);
}
