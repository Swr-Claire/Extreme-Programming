package com.lantu.sys.service;

import com.lantu.common.vo.UserExportVo;
import com.lantu.sys.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author laocai
 * @since 2023-02-07
 */
public interface IUserService extends IService<User> {

    Map<String, Object> login(User user);

    Map<String, Object> getUserInfo(String token);

    void logout(String token);

    @Transactional(rollbackFor = Exception.class)
    void saveFromImportData(List<UserExportVo> list);

    @Transactional(rollbackFor = Exception.class)
    void saveOrUpdateUser(User user);

    List<User> findByUserName(String username);
}
