package com.lantu.sys.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lantu.common.utils.L;
import com.lantu.common.vo.UserExportVo;
import com.lantu.sys.entity.User;
import com.lantu.sys.entity.UserContactDetail;
import com.lantu.sys.mapper.UserMapper;
import com.lantu.sys.service.IUserContactDetailService;
import com.lantu.sys.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author laocai
 * @since 2023-02-07
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private IUserContactDetailService userContactDetailService;

//    @Override
//    public Map<String, Object> login(User user) {
//        // 根据用户名查询
//        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
//        wrapper.eq(User::getUsername,user.getUsername());
//        User loginUser = this.baseMapper.getone(wrapper);
//        // 结果不为空，并且密码和传入密码匹配，则生成token，并将用户信息存入redis
//        if(loginUser != null && passwordEncoder.matches(user.getPassword(),loginUser.getPassword())){
//            // 暂时用UUID, 终极方案是jwt
//            String key = "user:" + UUID.randomUUID();
//
//            // 存入redis
//            loginUser.setPassword(null);
//            redisTemplate.opsForValue().set(key,loginUser,30, TimeUnit.MINUTES);
//
//            // 返回数据
//            Map<String, Object> data = new HashMap<>();
//            data.put("token",key);
//            return data;
//        }
//        return null;
//    }

    @Override
    public Map<String, Object> login(User user) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper();
        wrapper.eq(User::getUsername,user.getUsername());
        wrapper.eq(User::getPassword,user.getPassword());
        User loginUser = this.getOne(wrapper);
        if(loginUser != null){
            Map<String, Object> data = new HashMap<>();
            String key = "user::" + UUID.randomUUID();
            data.put("token", key); // 待优化，最终方案jwt
            loginUser.setPassword(null);
            redisTemplate.opsForValue().set(key,loginUser,30, TimeUnit.MINUTES);
            return data;
        }
        return null;
    }

    /*@Override
    public Map<String, Object> login(User user) {
        // 根据用户名和密码查询
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername,user.getUsername());
        wrapper.eq(User::getPassword,user.getPassword());
        User loginUser = this.baseMapper.selectOne(wrapper);
        // 结果不为空，则生成token，并将用户信息存入redis
        if(loginUser != null){
            // 暂时用UUID, 终极方案是jwt
            String key = "user:" + UUID.randomUUID();

            // 存入redis
            loginUser.setPassword(null);
            redisTemplate.opsForValue().set(key,loginUser,30, TimeUnit.MINUTES);

            // 返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("token",key);
            return data;
        }
        return null;
    }*/

    @Override
    public Map<String, Object> getUserInfo(String token) {
        // 根据token获取用户信息，redis
        Object obj = redisTemplate.opsForValue().get(token);
        if(obj != null){
            User loginUser = JSON.parseObject(JSON.toJSONString(obj),User.class);
            Map<String, Object> data = new HashMap<>();
            data.put("name",loginUser.getUsername());
            data.put("avatar", loginUser.getAvatar());

            // 角色
            List<String> roleList = this.baseMapper.getRoleNameByUserId(loginUser.getId());
            data.put("roles",roleList);

            return data;
        }
        return null;
    }

    @Override
    public void logout(String token) {
        redisTemplate.delete(token);
    }

    private void valid(List<UserExportVo> list){
        for (UserExportVo userExportVo : list){
            if(StrUtil.isBlank(userExportVo.getUserName())){
                throw new RuntimeException("姓名不能为空");
            }
            if(StrUtil.isBlank(userExportVo.getContactName())){
                throw new RuntimeException("联系方式类型不能为空");
            }
            if(StrUtil.isBlank(userExportVo.getContactValue())){
                throw new RuntimeException("联系方式不能为空");
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveFromImportData(List<UserExportVo> list) {
        this.valid(list);
        List<String> collect = L.collect(list, UserExportVo::getUserName);
        List<User> userList = this.baseMapper.selectList(new LambdaQueryWrapper<User>().in(User::getUsername, collect));
        Map<String, User> userMap = L.toMap(userList, User::getUsername);
        Map<String, List<UserExportVo>> listMap = L.groupBy(list, UserExportVo::getUserName);
        for(String userName : listMap.keySet()){
            List<UserExportVo> importVos = listMap.get(userName);
            User user = null;
            if(userMap.containsKey(userName)){
                user = userMap.get(userName);
                List<UserContactDetail> details = this.userContactDetailService.findByUserId(user.getId());
                user.setUserContactDetails(details);
            }else {
                user = new User();
                user.setUsername(userName);
                user.setDeleted(0);
                user.setStatus(1);
                this.save(user);
            }
            if(null == user.getUserContactDetails()){
                List<UserContactDetail> details = new ArrayList<>();
                user.setUserContactDetails(details);
            }
            // 设置用户联系方式
            for(UserExportVo vo : importVos){
                UserContactDetail detail = new UserContactDetail();
                detail.setUserId(user.getId());
                detail.setName(vo.getContactName());
                detail.setDetail(vo.getContactValue());
                user.getUserContactDetails().add(detail);
            }
            this.saveOrUpdateUser(user);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateUser(User user){
        if(null != user.getId()){
            User oldUser = this.getById(user.getId());
            if(null != oldUser){
                this.baseMapper.updateById(user);
            }else {
                this.baseMapper.insert(user);
            }
        }else {
            this.baseMapper.insert(user);
        }
        this.userContactDetailService.saveUserContactDetail(user);
    }

    @Override
    public List<User> findByUserName(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername,username);
        List<User> list = this.list(wrapper);
        return list;
    }

}
