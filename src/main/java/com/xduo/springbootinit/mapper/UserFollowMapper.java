package com.xduo.springbootinit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xduo.springbootinit.model.entity.UserFollow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户关注关系 Mapper
 */
@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {

    /**
     * 统计某个用户的有效粉丝数
     */
    @Select("""
            select count(1)
            from user_follow uf
            join `user` u on u.id = uf.userId
            where uf.followUserId = #{userId}
              and u.isDelete = 0
              and u.userRole <> 'ban'
            """)
    long countVisibleFollower(@Param("userId") long userId);

    /**
     * 统计某个用户的有效关注数
     */
    @Select("""
            select count(1)
            from user_follow uf
            join `user` u on u.id = uf.followUserId
            where uf.userId = #{userId}
              and u.isDelete = 0
              and u.userRole <> 'ban'
            """)
    long countVisibleFollowing(@Param("userId") long userId);

    /**
     * 分页获取有效粉丝 id
     */
    @Select("""
            select uf.userId
            from user_follow uf
            join `user` u on u.id = uf.userId
            where uf.followUserId = #{userId}
              and u.isDelete = 0
              and u.userRole <> 'ban'
            order by uf.createTime desc
            limit #{offset}, #{size}
            """)
    List<Long> listVisibleFollowerUserIds(@Param("userId") long userId,
                                          @Param("offset") long offset,
                                          @Param("size") long size);

    /**
     * 分页获取有效关注用户 id
     */
    @Select("""
            select uf.followUserId
            from user_follow uf
            join `user` u on u.id = uf.followUserId
            where uf.userId = #{userId}
              and u.isDelete = 0
              and u.userRole <> 'ban'
            order by uf.createTime desc
            limit #{offset}, #{size}
            """)
    List<Long> listVisibleFollowingUserIds(@Param("userId") long userId,
                                           @Param("offset") long offset,
                                           @Param("size") long size);
}
