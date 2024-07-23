package com.jwb.company.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jwb.company.model.po.JwbUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author zss
 * @description 针对表【jwb_user】的数据库操作Mapper
 * @createDate 2024-07-23 19:37:58
 * @Entity com.jwb.company.model.po.JwbUser
 */
@Mapper
public interface JwbUserMapper extends BaseMapper<JwbUser> {

}




