package com.jwb.ucenter.model.po;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author itcast
 */
@Data
@TableName("jwb_permission")
public class JwbPermission implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;

    private String roleId;

    private String menuId;

    private LocalDateTime createTime;


}
