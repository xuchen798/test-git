package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Comment;
import com.blog.vo.CommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Select("SELECT c.*, u.username, u.avatar, " +
            "ru.username AS reply_username, ru.avatar AS reply_avatar " +
            "FROM comment c " +
            "LEFT JOIN user u ON c.user_id = u.id " +
            "LEFT JOIN user ru ON c.reply_user_id = ru.id " +
            "WHERE c.article_id = #{articleId} AND c.parent_id = 0 " +
            "ORDER BY c.create_time DESC")
    List<CommentVO> getTopLevelComments(@Param("articleId") Long articleId);

    @Select("SELECT c.*, u.username, u.avatar, " +
            "ru.username AS reply_username, ru.avatar AS reply_avatar " +
            "FROM comment c " +
            "LEFT JOIN user u ON c.user_id = u.id " +
            "LEFT JOIN user ru ON c.reply_user_id = ru.id " +
            "WHERE c.parent_id = #{parentId} " +
            "ORDER BY c.create_time ASC")
    List<CommentVO> getChildComments(@Param("parentId") Long parentId);

    @Select("SELECT c.*, u.username, u.avatar, " +
            "ru.username AS reply_username, ru.avatar AS reply_avatar " +
            "FROM comment c " +
            "LEFT JOIN user u ON c.user_id = u.id " +
            "LEFT JOIN user ru ON c.reply_user_id = ru.id " +
            "WHERE c.id = #{id}")
    CommentVO getCommentVOById(@Param("id") Long id);

}
