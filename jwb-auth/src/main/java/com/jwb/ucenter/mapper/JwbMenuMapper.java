package com.jwb.ucenter.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jwb.ucenter.model.po.JwbMenu;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface JwbMenuMapper extends BaseMapper<JwbMenu> {
    @Select("SELECT	* FROM jwb_menu WHERE id IN (SELECT menu_id FROM jwb_permission WHERE role_id IN ( SELECT role_id FROM jwb_user_role WHERE user_id = #{userId} ))")
    List<JwbMenu> selectPermissionByUserId(@Param("userId") String userId);
}
