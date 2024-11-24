package com.lantu.sys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 
 * </p>
 *
 * @author laocai
 * @since 2023-02-07
 */
@TableName("x_user")
@Data
public class User implements Serializable {

    private static final long serialVersionUID = 1L;


    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String username;

    private String password;

    private String email;

    private String phone;

    private Integer status;

    private String avatar;
    /**
     * 是否常用联系人 1 常用 0 不常用 数据库默认值为0
     */
    private Integer isFrequent;

    private Integer deleted;

    @TableField(exist = false)
    private List<UserContactDetail> userContactDetails;

    @Override
    public String toString() {
        return "User{" +
            "id=" + id +
            ", username=" + username +
            ", password=" + password +
            ", email=" + email +
            ", phone=" + phone +
            ", status=" + status +
            ", avatar=" + avatar +
            ", deleted=" + deleted +
        "}";
    }
}
