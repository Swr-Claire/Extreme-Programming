package com.lantu.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lantu.common.utils.L;
import com.lantu.sys.entity.User;
import com.lantu.sys.entity.UserContactDetail;
import com.lantu.sys.mapper.UserContactDetailMapper;
import com.lantu.sys.service.IUserContactDetailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author laocai
 * @since 2023-02-07
 */
@Service
public class UserContactDetailServiceImpl extends ServiceImpl<UserContactDetailMapper, UserContactDetail> implements IUserContactDetailService {
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveUserContactDetail(User user) {
        List<UserContactDetail> details = user.getUserContactDetails();
        if(null != details && details.size() > 0){
            for(UserContactDetail detail : details){
                detail.setUserId(user.getId());
            }
            // 先删除再保存
            this.deleteByUserId(user.getId());
            this.saveBatch(details);
        }
    }

    /**
     * 根据用户id删除联系方式
     * @param userId 用户id
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteByUserId(Integer userId) {
        LambdaQueryWrapper<UserContactDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserContactDetail::getUserId,userId);
        this.remove(wrapper);
    }


    /**
     * 根据用户id查询额外的联系方式
     * @param userId 用户id
     */
    @Override
    public List<UserContactDetail>  findByUserId(Integer userId) {
        LambdaQueryWrapper<UserContactDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserContactDetail::getUserId,userId);
        List<UserContactDetail> list = this.list(wrapper);
        return list;
    }

    /**
     * 填充用户的额外联系方式
     */
    @Override
    public void  fillUserContactDetail(List<User> users) {
        if(null != users && users.size() > 0){
            List<Integer> userIds = L.collect(users, User::getId);
            Wrapper<UserContactDetail> wrapper = new LambdaQueryWrapper<UserContactDetail>().in(UserContactDetail::getUserId,userIds);
            List<UserContactDetail> list = this.list(wrapper);
            Map<Integer, List<UserContactDetail>> map = L.groupBy(list, UserContactDetail::getUserId);
            users.forEach(user -> user.setUserContactDetails(map.get(user.getId())));
        }

    }

}
