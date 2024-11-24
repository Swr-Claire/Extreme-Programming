package com.lantu.common.listener;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.lantu.common.excel.ExcelListener;
import com.lantu.common.excel.ExcelResult;
import com.lantu.common.utils.spring.SpringUtils;
import com.lantu.common.vo.UserExportVo;
import com.lantu.sys.entity.User;
import com.lantu.sys.service.IUserService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 系统用户自定义导入
 *
 * @author Lion Li
 */
@Slf4j
public class SysUserImportListener extends AnalysisEventListener<UserExportVo> implements ExcelListener<UserExportVo> {

    private final IUserService userService;

    @Getter
    private List<UserExportVo> userList = new ArrayList<>();



    public SysUserImportListener() {
        this.userService = SpringUtils.getBean(IUserService.class);

    }

    @Override
    public void invoke(UserExportVo userVo, AnalysisContext context) {
        userList.add(userVo);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {

    }

    @Override
    public ExcelResult<UserExportVo> getExcelResult() {
        ExcelResult excelResult = new ExcelResult<UserExportVo>() {
            @Override
            public List<UserExportVo> getList() {
                return userList;
            }

            @Override
            public List<String> getErrorList() {
                return null;
            }

            @Override
            public String getAnalysis() {
                return null;
            }
        };
        return excelResult;
    }
}
