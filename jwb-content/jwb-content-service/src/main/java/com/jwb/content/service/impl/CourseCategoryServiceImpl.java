package com.jwb.content.service.impl;

import com.jwb.content.mapper.CourseCategoryMapper;
import com.jwb.content.service.CourseCategoryService;
import com.jwb.model.dto.CourseCategoryTreeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zss
 * @description 针对表【course_category(课程分类)】的数据库操作Service实现
 * @createDate 2024-06-12 15:39:43
 */
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    /**
     * 课程分类树查询
     *
     * @param id
     */
    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);
        //将list转map,以备使用,排除根节点
        Map<String, CourseCategoryTreeDto> mapTemp = courseCategoryTreeDtos.stream().filter(item -> !id.equals(item.getId()))
                .collect(Collectors.toMap(key -> key.getId(), value -> value, (key1, key2) -> key2));
        //最终返回的list
        List<CourseCategoryTreeDto> categoryTreeDtos = new ArrayList<>();
        //依次遍历每个元素,排除根节点
        courseCategoryTreeDtos.stream().filter(item -> !id.equals(item.getId())).forEach(item -> {
            // 一级id下的二级1-1才直接加入最后返回的list
            if (item.getParentid().equals(id)) {
                // id的子节点才加入
                categoryTreeDtos.add(item);
            }
            // 找到当前节点的父节点
            CourseCategoryTreeDto courseCategoryTreeDto = mapTemp.get(item.getParentid());
            // 将三级1-1-1放到二级1-1下
            if (courseCategoryTreeDto != null) {
                if (courseCategoryTreeDto.getChildrenTreeNodes() == null) {
                    courseCategoryTreeDto.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                }
                //下边开始往ChildrenTreeNodes属性中放子节点
                courseCategoryTreeDto.getChildrenTreeNodes().add(item);
            }
        });
        return categoryTreeDtos;
    }
}




