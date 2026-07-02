package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.entity.Favorite;
import com.blog.vo.ArticleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    @Select("SELECT a.*, u.username, u.avatar, u.nickname, u.bio, " +
            "(SELECT GROUP_CONCAT(c.name SEPARATOR '、') FROM category c WHERE FIND_IN_SET(c.id, a.category_ids)) AS categoryName " +
            "FROM favorite f " +
            "LEFT JOIN article a ON f.article_id = a.id " +
            "LEFT JOIN user u ON a.user_id = u.id " +
            "WHERE f.user_id = #{userId} " +
            "AND a.id IS NOT NULL " +
            "AND (a.status = 1 OR a.user_id = #{userId}) " +
            "ORDER BY f.create_time DESC")
    IPage<ArticleVO> getFavoriteArticlePage(Page<ArticleVO> page, @Param("userId") Long userId);

}
