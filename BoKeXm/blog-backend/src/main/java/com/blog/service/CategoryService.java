package com.blog.service;

import com.blog.vo.CategoryVO;

import java.util.List;

public interface CategoryService {

    List<CategoryVO> listAllCategories();

    CategoryVO getCategoryById(Long id);

}
