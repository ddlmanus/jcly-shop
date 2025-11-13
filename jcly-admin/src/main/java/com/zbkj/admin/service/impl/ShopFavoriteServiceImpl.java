package com.zbkj.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zbkj.admin.service.ShopFavoriteService;
import com.zbkj.common.model.admin.SystemAdmin;
import com.zbkj.common.model.user.User;
import com.zbkj.common.model.user.UserMerchantCollect;
import com.zbkj.common.request.PageParamRequest;
import com.zbkj.common.utils.SecurityUtil;
import com.zbkj.service.dao.UserDao;
import com.zbkj.service.dao.UserMerchantCollectDao;
import com.zbkj.service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 店铺收藏服务实现类
 */
@Service
public class ShopFavoriteServiceImpl implements ShopFavoriteService {

    @Resource
    private UserMerchantCollectDao userMerchantCollectDao;

    @Resource
    private UserDao userDao;

    @Autowired
    private UserService userService;

    /**
     * 获取店铺收藏列表
     * @param searchType 搜索类型
     * @param content 搜索内容
     * @param dateLimit 日期限制
     * @param pageParamRequest 分页参数
     * @return 店铺收藏列表
     */
    @Override
    public List<UserMerchantCollect> getList(String searchType, String content, String dateLimit, PageParamRequest pageParamRequest) {
        SystemAdmin systemAdmin = SecurityUtil.getLoginUserVo().getUser();
        Integer merId = systemAdmin.getMerId();
        if (merId == null || merId <= 0) {
            return new ArrayList<>();
        }

        Page<UserMerchantCollect> page = new Page<>(pageParamRequest.getPage(), pageParamRequest.getLimit());
        LambdaQueryWrapper<UserMerchantCollect> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        
        // 只查询当前商户的收藏记录
        lambdaQueryWrapper.eq(UserMerchantCollect::getMerId, merId);
        
//        // 按照日期范围查询
//        if (!StringUtils.isEmpty(dateLimit)) {
//            String[] dateArr = dateLimit.split(",");
//            if (dateArr.length == 2) {
//                lambdaQueryWrapper.between(UserMerchantCollect::getCreateTime,
//                        DateUtil.strToDate(dateArr[0] + " 00:00:00", DateUtil.DATE_FORMAT_DATE_TIME),
//                        DateUtil.strToDate(dateArr[1] + " 23:59:59", DateUtil.DATE_FORMAT_DATE_TIME));
//            }
//        }
        
        // 按照创建时间倒序排序
        lambdaQueryWrapper.orderByDesc(UserMerchantCollect::getCreateTime);
        
        // 查询收藏记录
        Page<UserMerchantCollect> collectPage = userMerchantCollectDao.selectPage(page, lambdaQueryWrapper);
        List<UserMerchantCollect> collectList = collectPage.getRecords();
        
        // 首先为所有收藏记录设置格式化后的收藏时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (UserMerchantCollect collect : collectList) {
            // 设置格式化后的收藏时间
            if (collect.getCreateTime() != null) {
                collect.setCreate_time(collect.getCreateTime().format(formatter));
            }
        }
        
        if (collectList.size() > 0) {
            // 获取用户ID列表
            List<Integer> uidList = collectList.stream().map(UserMerchantCollect::getUid).collect(Collectors.toList());
            
            // 查询用户信息
            LambdaQueryWrapper<User> userLambdaQueryWrapper = new LambdaQueryWrapper<>();
            userLambdaQueryWrapper.in(User::getId, uidList);
            
            // 根据搜索条件过滤用户
            if (!StringUtils.isEmpty(content)) {
                if ("all".equals(searchType) || StringUtils.isEmpty(searchType)) {
                    userLambdaQueryWrapper.and(wrapper -> wrapper
                            .like(User::getNickname, content)
                            .or()
                            .like(User::getPhone, content));
                } else if ("nickname".equals(searchType)) {
                    userLambdaQueryWrapper.like(User::getNickname, content);
                } else if ("phone".equals(searchType)) {
                    userLambdaQueryWrapper.like(User::getPhone, content);
                }
            }
            
            List<User> userList = userDao.selectList(userLambdaQueryWrapper);
            
            // 如果没有符合条件的用户，返回空列表
            if (userList.size() == 0) {
                return new ArrayList<>();
            }
            
            // 将用户信息映射到收藏记录中
            Map<Integer, User> userMap = userList.stream().collect(Collectors.toMap(User::getId, user -> user));
            for (UserMerchantCollect collect : collectList) {
                User user = userMap.get(collect.getUid());
                if (user != null) {
                    collect.setNickname(user.getNickname());
                    collect.setAvatar(user.getAvatar());
                    collect.setPhone(user.getPhone());
                }
            }
            
            // 过滤掉没有对应用户信息的收藏记录
            collectList = collectList.stream()
                    .filter(collect -> userMap.containsKey(collect.getUid()))
                    .collect(Collectors.toList());
        }
        
        return collectList;
    }
}