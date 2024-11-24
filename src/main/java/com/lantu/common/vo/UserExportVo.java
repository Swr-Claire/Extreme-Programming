package com.lantu.common.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户对象导出VO
 *
 * @author Lion Li
 */

@Data
@NoArgsConstructor
public class UserExportVo implements Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * 联系人名称
     */
    @ExcelProperty(value = "姓名")
    private String userName;

    /**
     * 联系方式类型
     */
    @ExcelProperty(value = "联系方式类型")
    private String contactName;

    /**
     * 联系方式的值
     */
    @ExcelProperty(value = "联系方式")
    private String contactValue;
}
