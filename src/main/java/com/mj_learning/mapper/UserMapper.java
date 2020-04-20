package com.mj_learning.mapper;

import com.mj_learning.entities.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {
    @Select("select * from user")
    List<User> getUsers();

    @Insert("insert into user(account_id,name,token,gmt_created,gmt_modified) values(#{account_id},#{name},#{token},#{gmt_created},#{gmt_modified})")
    int insert(User user);

    @Select("select * from user where token=#{token}")
    User findUserByToken(@Param("token") String token);
}
