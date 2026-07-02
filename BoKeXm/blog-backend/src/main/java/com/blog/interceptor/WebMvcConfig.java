package com.blog.interceptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.Resource;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private LoginInterceptor loginInterceptor;

    @Value("${file.upload.path:D:/blog/upload/}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:/upload/}")
    private String uploadUrlPrefix;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/user/captcha",
                        "/user/forgot/send-code",
                        "/user/forgot/reset",
                        "/article/list",
                        "/article/hot",
                        "/article/category/list",
                        "/search",
                        "/comment/list/**",
                        "/swagger-resources/**",
                        "/webjars/**",
                        "/v3/**",
                        "/swagger-ui/**",
                        "/doc.html",
                        "/favicon.ico",
                        "/error",
                        uploadUrlPrefix + "**"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(uploadUrlPrefix + "**")
                .addResourceLocations("file:" + uploadPath);
    }

}
