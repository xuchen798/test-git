package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.entity.Article;
import com.blog.vo.ArticleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    @Select("SELECT a.*, u.username, u.nickname, u.avatar, u.bio, " +
            "(SELECT GROUP_CONCAT(c.name SEPARATOR '、') FROM category c WHERE FIND_IN_SET(c.id, a.category_ids)) AS categoryName " +
            "FROM article a " +
            "LEFT JOIN user u ON a.user_id = u.id " +
            "WHERE a.id = #{id}")
    ArticleVO getArticleVOById(@Param("id") Long id);

    @Select("<script>" +
            "SELECT a.*, u.username, u.nickname, u.avatar, u.bio, " +
            "(SELECT GROUP_CONCAT(c.name SEPARATOR '、') FROM category c WHERE FIND_IN_SET(c.id, a.category_ids)) AS categoryName " +
            "FROM article a " +
            "LEFT JOIN user u ON a.user_id = u.id " +
            "<where>" +
            "   a.status = 1" +
            "   <if test='categoryId != null'>" +
            "       AND FIND_IN_SET(#{categoryId}, a.category_ids)" +
            "   </if>" +
            "   <if test='keyword != null and keyword != \"\"'>" +
            "       AND (a.title LIKE CONCAT('%', #{keyword}, '%') OR a.summary LIKE CONCAT('%', #{keyword}, '%'))" +
            "   </if>" +
            "</where>" +
            "ORDER BY a.create_time DESC" +
            "</script>")
    IPage<ArticleVO> getArticleVOPage(Page<ArticleVO> page,
                                      @Param("categoryId") Long categoryId,
                                      @Param("keyword") String keyword);

    @Select("SELECT a.*, u.username, u.nickname, u.avatar, u.bio, " +
            "(SELECT GROUP_CONCAT(c.name SEPARATOR '、') FROM category c WHERE FIND_IN_SET(c.id, a.category_ids)) AS categoryName " +
            "FROM article a " +
            "LEFT JOIN user u ON a.user_id = u.id " +
            "WHERE a.status = 1 " +
            "ORDER BY a.view_count DESC " +
            "LIMIT 10")
    java.util.List<ArticleVO> getHotArticleVOs();

    @Select("<script>" +
            "SELECT a.*, u.username, u.nickname, u.avatar, u.bio, " +
            "(SELECT GROUP_CONCAT(c.name SEPARATOR '、') FROM category c WHERE FIND_IN_SET(c.id, a.category_ids)) AS categoryName " +
            "FROM article a " +
            "LEFT JOIN user u ON a.user_id = u.id " +
            "<where>" +
            "   a.user_id = #{userId}" +
            "   <if test='status != null'>" +
            "       AND a.status = #{status}" +
            "   </if>" +
            "</where>" +
            "ORDER BY a.create_time DESC" +
            "</script>")
    IPage<ArticleVO> getUserArticleVOPage(Page<ArticleVO> page,
                                          @Param("userId") Long userId,
                                          @Param("status") Integer status);

    @Select("<script>" +
            "SELECT DISTINCT a.*, u.username, u.nickname, u.avatar, u.bio, " +
            "(SELECT GROUP_CONCAT(c2.name SEPARATOR '、') FROM category c2 WHERE FIND_IN_SET(c2.id, a.category_ids)) AS categoryName " +
            "FROM article a " +
            "LEFT JOIN user u ON a.user_id = u.id " +
            "LEFT JOIN category c ON FIND_IN_SET(c.id, a.category_ids) " +
            "<where>" +
            "   a.status = 1" +
            "   <if test='categoryId != null'>" +
            "       AND FIND_IN_SET(#{categoryId}, a.category_ids)" +
            "   </if>" +
            "   <if test='keyword != null and keyword != \"\"'>" +
            "       AND (a.title LIKE CONCAT('%', #{keyword}, '%')" +
            "           OR a.summary LIKE CONCAT('%', #{keyword}, '%')" +
            "           OR a.content LIKE CONCAT('%', #{keyword}, '%')" +
            "           OR c.name LIKE CONCAT('%', #{keyword}, '%'))" +
            "   </if>" +
            "</where>" +
            "ORDER BY a.create_time DESC" +
            "</script>")
    IPage<ArticleVO> searchGlobalArticles(Page<ArticleVO> page,
                                          @Param("keyword") String keyword,
                                          @Param("categoryId") Long categoryId);

    @Select("<script>" +
            "SELECT DISTINCT a.*, u.username, u.nickname, u.avatar, u.bio, " +
            "(SELECT GROUP_CONCAT(c2.name SEPARATOR '、') FROM category c2 WHERE FIND_IN_SET(c2.id, a.category_ids)) AS categoryName " +
            "FROM article a " +
            "LEFT JOIN user u ON a.user_id = u.id " +
            "LEFT JOIN category c ON FIND_IN_SET(c.id, a.category_ids) " +
            "<where>" +
            "   a.user_id = #{userId}" +
            "   <if test='categoryId != null'>" +
            "       AND FIND_IN_SET(#{categoryId}, a.category_ids)" +
            "   </if>" +
            "   <if test='keyword != null and keyword != \"\"'>" +
            "       AND (a.title LIKE CONCAT('%', #{keyword}, '%')" +
            "           OR a.summary LIKE CONCAT('%', #{keyword}, '%')" +
            "           OR a.content LIKE CONCAT('%', #{keyword}, '%')" +
            "           OR c.name LIKE CONCAT('%', #{keyword}, '%'))" +
            "   </if>" +
            "</where>" +
            "ORDER BY a.create_time DESC" +
            "</script>")
    IPage<ArticleVO> searchPersonalArticles(Page<ArticleVO> page,
                                            @Param("userId") Long userId,
                                            @Param("keyword") String keyword,
                                            @Param("categoryId") Long categoryId);

}
