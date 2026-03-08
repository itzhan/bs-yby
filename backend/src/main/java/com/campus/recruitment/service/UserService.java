package com.campus.recruitment.service;

import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.dto.DashboardVO;
import com.campus.recruitment.dto.UserUpdateDTO;
import com.campus.recruitment.dto.UserVO;

public interface UserService {

    /**
     * 分页查询用户列表
     */
    PageResult<UserVO> listUsers(int page, int size, String role, String keyword);

    /**
     * 根据 ID 查询用户
     */
    UserVO getUserById(Long id);

    /**
     * 获取当前登录用户
     */
    UserVO getCurrentUser();

    /**
     * 更新用户信息
     */
    void updateUser(Long id, UserUpdateDTO dto);

    /**
     * 更新用户状态（管理员）
     */
    void updateStatus(Long id, Integer status);

    /**
     * 删除用户
     */
    void deleteUser(Long id);

    /**
     * 获取仪表盘数据
     */
    DashboardVO getDashboard();
}
