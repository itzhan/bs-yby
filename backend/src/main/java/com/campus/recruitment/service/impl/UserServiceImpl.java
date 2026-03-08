package com.campus.recruitment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.recruitment.common.BusinessException;
import com.campus.recruitment.common.PageResult;
import com.campus.recruitment.dto.DashboardVO;
import com.campus.recruitment.dto.JobVO;
import com.campus.recruitment.dto.UserUpdateDTO;
import com.campus.recruitment.dto.UserVO;
import com.campus.recruitment.entity.*;
import com.campus.recruitment.mapper.*;
import com.campus.recruitment.security.SecurityUtils;
import com.campus.recruitment.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final StudentProfileMapper studentProfileMapper;
    private final CompanyProfileMapper companyProfileMapper;
    private final JobMapper jobMapper;
    private final ApplicationMapper applicationMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public PageResult<UserVO> listUsers(int page, int size, String role, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(role)) {
            wrapper.eq(User::getRole, role);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getEmail, keyword)
                    .or().like(User::getPhone, keyword));
        }
        wrapper.orderByDesc(User::getCreatedAt);

        Page<User> pageParam = new Page<>(page, size);
        Page<User> pageResult = userMapper.selectPage(pageParam, wrapper);

        List<UserVO> voList = pageResult.getRecords().stream()
                .map(this::toUserVO)
                .collect(Collectors.toList());

        return PageResult.of(voList, pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize());
    }

    @Override
    public UserVO getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return toUserVO(user);
    }

    @Override
    public UserVO getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("未登录");
        }
        return getUserById(userId);
    }

    @Override
    public void updateUser(Long id, UserUpdateDTO dto) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (dto.getNickname() != null) {
            user.setNickname(dto.getNickname());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (StringUtils.hasText(dto.getPassword())) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        userMapper.updateById(user);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        userMapper.deleteById(id);
    }

    @Override
    public DashboardVO getDashboard() {
        DashboardVO vo = new DashboardVO();

        // 统计总数
        vo.setTotalStudents(userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getRole, "STUDENT")));
        vo.setTotalCompanies(userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getRole, "COMPANY")));
        vo.setTotalJobs(jobMapper.selectCount(null));
        vo.setTotalApplications(applicationMapper.selectCount(null));

        // 最近发布的 10 个职位
        Page<Job> jobPage = new Page<>(1, 10);
        LambdaQueryWrapper<Job> jobWrapper = new LambdaQueryWrapper<>();
        jobWrapper.eq(Job::getStatus, 2) // 已发布
                  .orderByDesc(Job::getCreatedAt);
        Page<Job> recentJobPage = jobMapper.selectPage(jobPage, jobWrapper);
        List<JobVO> recentJobs = recentJobPage.getRecords().stream().map(job -> {
            JobVO jv = new JobVO();
            jv.setId(job.getId());
            jv.setCompanyId(job.getCompanyId());
            jv.setTitle(job.getTitle());
            jv.setJobType(job.getJobType());
            jv.setSalaryMin(job.getSalaryMin());
            jv.setSalaryMax(job.getSalaryMax());
            jv.setCity(job.getCity());
            jv.setCategory(job.getCategory());
            jv.setStatus(job.getStatus());
            jv.setCreatedAt(job.getCreatedAt());
            // 关联公司信息
            CompanyProfile cp = companyProfileMapper.selectOne(
                    new LambdaQueryWrapper<CompanyProfile>().eq(CompanyProfile::getUserId, job.getCompanyId()));
            if (cp != null) {
                jv.setCompanyName(cp.getCompanyName());
                jv.setCompanyLogo(cp.getLogo());
                jv.setIndustry(cp.getIndustry());
            }
            return jv;
        }).collect(Collectors.toList());
        vo.setRecentJobs(recentJobs);

        // 申请状态统计
        Map<String, Long> statusStats = new LinkedHashMap<>();
        statusStats.put("待查看", applicationMapper.selectCount(
                new LambdaQueryWrapper<Application>().eq(Application::getStatus, 0)));
        statusStats.put("已查看", applicationMapper.selectCount(
                new LambdaQueryWrapper<Application>().eq(Application::getStatus, 1)));
        statusStats.put("面试中", applicationMapper.selectCount(
                new LambdaQueryWrapper<Application>().eq(Application::getStatus, 2)));
        statusStats.put("已录用", applicationMapper.selectCount(
                new LambdaQueryWrapper<Application>().eq(Application::getStatus, 3)));
        statusStats.put("已拒绝", applicationMapper.selectCount(
                new LambdaQueryWrapper<Application>().eq(Application::getStatus, 4)));
        statusStats.put("已撤回", applicationMapper.selectCount(
                new LambdaQueryWrapper<Application>().eq(Application::getStatus, 5)));
        vo.setApplicationStatusStats(statusStats);

        // 行业分布统计
        List<CompanyProfile> allCompanies = companyProfileMapper.selectList(
                new LambdaQueryWrapper<CompanyProfile>().isNotNull(CompanyProfile::getIndustry));
        Map<String, Long> industryStats = allCompanies.stream()
                .filter(c -> StringUtils.hasText(c.getIndustry()))
                .collect(Collectors.groupingBy(CompanyProfile::getIndustry, Collectors.counting()));
        vo.setIndustryStats(industryStats);

        // 月度申请统计（最近 6 个月）
        List<Map<String, Object>> monthlyStats = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        for (int i = 5; i >= 0; i--) {
            LocalDateTime monthStart = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime monthEnd = monthStart.plusMonths(1);
            Long monthCount = applicationMapper.selectCount(
                    new LambdaQueryWrapper<Application>()
                            .ge(Application::getCreatedAt, monthStart)
                            .lt(Application::getCreatedAt, monthEnd));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("month", monthStart.format(formatter));
            item.put("count", monthCount);
            monthlyStats.add(item);
        }
        vo.setMonthlyStats(monthlyStats);

        return vo;
    }

    private UserVO toUserVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }
}
