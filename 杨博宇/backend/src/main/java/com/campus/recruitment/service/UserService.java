package com.campus.recruitment.service;

import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.dto.DashboardVO;
import com.campus.recruitment.dto.UserUpdateDTO;
import com.campus.recruitment.dto.UserVO;

public interface UserService {

    PageResult<UserVO> listUsers(int page, int size, String role, String keyword);

    UserVO getUserById(Long id);

    UserVO getCurrentUser();

    void updateUser(Long id, UserUpdateDTO dto);

    void updateStatus(Long id, Integer status);

    void deleteUser(Long id);

    /** 管理员全局仪表盘 */
    DashboardVO getDashboard();

    /** 企业专属工作台数据 */
    DashboardVO getCompanyDashboard(Long userId);
}
