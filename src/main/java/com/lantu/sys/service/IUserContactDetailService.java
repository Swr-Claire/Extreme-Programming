package com.lantu.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lantu.sys.entity.User;
import com.lantu.sys.entity.UserContactDetail;
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
public interface IUserContactDetailService extends IService<UserContactDetail> {


    @Transactional(rollbackFor = Exception.class)
    void saveUserContactDetail(User user);

    List<UserContactDetail>  findByUserId(Integer userId);

    void  fillUserContactDetail(List<User> users);
}
