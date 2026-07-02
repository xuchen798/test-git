package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.RedisKeyConstants;
import com.blog.common.RedisUtil;
import com.blog.entity.Category;
import com.blog.mapper.CategoryMapper;
import com.blog.service.CategoryService;
import com.blog.vo.CategoryVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private RedisUtil redisUtil;

    private static final String CATEGORY_CACHE_KEY = RedisKeyConstants.CATEGORY_LIST;

    @Override
    public List<CategoryVO> listAllCategories() {
        Object cacheObj = redisUtil.get(CATEGORY_CACHE_KEY);
        if (cacheObj != null) {
            return (List<CategoryVO>) cacheObj;
        }

        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Category::getSort);
        List<Category> categories = categoryMapper.selectList(wrapper);

        List<CategoryVO> result;
        if (categories == null || categories.isEmpty()) {
            result = Collections.emptyList();
            redisUtil.set(CATEGORY_CACHE_KEY, result, 60, TimeUnit.SECONDS);
            return result;
        }

        result = categories.stream().map(category -> {
            CategoryVO vo = new CategoryVO();
            BeanUtils.copyProperties(category, vo);
            return vo;
        }).collect(Collectors.toList());

        int expireTime = 600 + (int) (Math.random() * 120);
        redisUtil.set(CATEGORY_CACHE_KEY, result, expireTime, TimeUnit.SECONDS);

        return result;
    }

    @Override
    public CategoryVO getCategoryById(Long id) {
        if (id == null) {
            return null;
        }
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            return null;
        }
        CategoryVO vo = new CategoryVO();
        BeanUtils.copyProperties(category, vo);
        return vo;
    }

}
