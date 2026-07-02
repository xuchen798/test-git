package com.blog.common;

public class RedisKeyConstants {

    public static final String USER_LOGIN_TOKEN = "blog:user:token:";
    public static final String USER_CAPTCHA = "blog:user:captcha:";
    public static final String USER_INFO = "blog:user:info:";
    public static final String ARTICLE_VIEW = "blog:article:view:";
    public static final String ARTICLE_LIKE = "blog:article:like:";
    public static final String COMMENT_LIKE = "blog:comment:like:";
    public static final String HOT_ARTICLE = "blog:article:hot";
    public static final String CATEGORY_LIST = "blog:category:list";
    public static final String TAG_LIST = "blog:tag:list";
    public static final String BLOG_CONFIG = "blog:config";
    public static final String SEARCH_HISTORY = "blog:search:history:";
    public static final String VIEW_COUNT = "blog:view:count:";

    private RedisKeyConstants() {
    }

}
