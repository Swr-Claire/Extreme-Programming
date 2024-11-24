package com.lantu.sys.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lantu.common.excel.ExcelResult;
import com.lantu.common.listener.SysUserImportListener;
import com.lantu.common.util.ExcelUtil;
import com.lantu.common.utils.L;
import com.lantu.common.vo.Result;
import com.lantu.common.vo.UserExportVo;
import com.lantu.sys.entity.User;
import com.lantu.sys.entity.UserContactDetail;
import com.lantu.sys.service.IUserContactDetailService;
import com.lantu.sys.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author laocai
 * @since 2023-02-07
 */
@RestController
@RequestMapping("/user")
// @CrossOrigin   跨域处理
public class UserController {
    @Autowired
    private IUserService userService;
    @Autowired
    private IUserContactDetailService contactDetailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/all")
    public Result<List<User>> getAllUser(){
        List<User> list = userService.list();
        return Result.success(list,"查询成功");
    }

    @PostMapping("/login")
    public Result<Map<String,Object>> login(@RequestBody User user){
        Map<String,Object> data = userService.login(user);
        if(data != null){
            return Result.success(data);
        }
        return Result.fail(20002,"用户名或密码错误");
    }

    @GetMapping("/info")
    public Result<Map<String,Object>> getUserInfo(@RequestParam("token") String token){
        // 根据token获取用户信息，redis
        Map<String,Object> data = userService.getUserInfo(token);
        if(data != null){
            return Result.success(data);
        }
        return Result.fail(20003,"登录信息无效，请重新登录");
    }

    @PostMapping("/logout")
    public Result<?> logout(@RequestHeader("X-Token") String token){
        userService.logout(token);
        return Result.success();
    }

    @GetMapping("/list")
    public Result<Map<String,Object>> getUserList(@RequestParam(value = "username",required = false) String username,
                                              @RequestParam(value = "isFrequent",required = false) Integer isFrequent,
                                              @RequestParam(value = "pageNo") Long pageNo,
                                              @RequestParam(value = "pageSize") Long pageSize){

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasLength(username),User::getUsername,username);
        wrapper.eq(null != isFrequent,User::getIsFrequent,isFrequent);
        wrapper.orderByDesc(User::getId);

        Page<User> page = new Page<>(pageNo,pageSize);
        userService.page(page,wrapper);
        this.contactDetailService.fillUserContactDetail(page.getRecords());
        Map<String,Object> data = new HashMap<>();
        data.put("total",page.getTotal());
        data.put("rows",page.getRecords());

        return Result.success(data);

    }

    @PostMapping
    public Result<?> addUser(@RequestBody User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.saveOrUpdateUser(user);
        return Result.success("新增用户成功");
    }

    @PutMapping
    public Result<?> updateUser(@RequestBody User user){
        user.setPassword(null);
        userService.saveOrUpdateUser(user);
        return Result.success("修改用户成功");
    }

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable("id") Integer id){
        User user = userService.getById(id);
        List<UserContactDetail> details = contactDetailService.findByUserId(id);
        user.setUserContactDetails(details);
        return Result.success(user);
    }

    @DeleteMapping("/{id}")
    public Result<User> deleteUserById(@PathVariable("id") Integer id){
        userService.removeById(id);
        return Result.success("删除用户成功");
    }

    @PostMapping("/editIsFrequentById")
    public Result<User> editIsFrequentById(@RequestParam("id") Integer id){
        User user = userService.getById(id);
        if(null == user){
            return Result.fail("联系人不存在");
        }
        user.setIsFrequent(user.getIsFrequent()==1?0:1);
        userService.updateById(user);
        return Result.success("修改是否常用联系人成功");
    }

    /**
     * 导出用户列表
     */
    @RequestMapping("/export")
    public void export(@RequestParam(value = "username",required = false) String username,
                       @RequestParam(value = "isFrequent",required = false) Integer isFrequent,
                       @RequestParam(value = "pageNo") Long pageNo,
                       @RequestParam(value = "pageSize") Long pageSize,HttpServletResponse response) {
        Result<Map<String, Object>> userList = this.getUserList(username,isFrequent, pageNo, pageSize);
        List<UserExportVo> listVo = new ArrayList<>();
        for (User user : (List<User>)userList.getData().get("rows")){
            if(null != user.getUserContactDetails()){
                for (UserContactDetail detail : user.getUserContactDetails()){
                    UserExportVo vo = new UserExportVo();
                    vo.setUserName(user.getUsername());
                    vo.setContactName(detail.getName());
                    vo.setContactValue(detail.getDetail());
                    listVo.add(vo);
                }
            }

        }

        ExcelUtil.exportExcel(listVo, "导出联系人", UserExportVo.class, response);
    }

    /**
     * 导入数据
     *
     * @param file          导入文件
     */
    @PostMapping(value = "/importData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Void> importData(@RequestPart("file") MultipartFile file) throws Exception {
        ExcelResult<UserExportVo> result = ExcelUtil.importExcel(file.getInputStream(), UserExportVo.class, new SysUserImportListener());
        List<UserExportVo> list = result.getList();
        //  调用保存的方法
        try {
            this.userService.saveFromImportData(list);
        }catch (Exception e){
            return Result.fail(e.getMessage());
        }
        return Result.success();
    }

    /**
     * 获取导入模板
     */
    @RequestMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response) {
        ExcelUtil.exportExcel(new ArrayList<>(), "联系人数据", UserExportVo.class, response);
    }

}
