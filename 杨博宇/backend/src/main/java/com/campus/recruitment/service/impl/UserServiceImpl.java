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
        if (dto.getNickname() != null) user.setNickname(dto.getNickname());
        if (dto.getAvatar() != null) user.setAvatar(dto.getAvatar());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getPhone() != null) user.setPhone(dto.getPhone());
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

        vo.setTotalStudents(userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getRole, "STUDENT")));
        vo.setTotalCompanies(userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getRole, "COMPANY")));
        vo.setTotalJobs(jobMapper.selectCount(null));
        vo.setTotalApplications(applicationMapper.selectCount(null));

        // 最近 10 个已发布职位（批量查公司，修复 companyId 映射：Job.companyId = CompanyProfile.id）
        Page<Job> jobPage = new Page<>(1, 10);
        Page<Job> recentJobPage = jobMapper.selectPage(jobPage,
                new LambdaQueryWrapper<Job>().eq(Job::getStatus, 2).orderByDesc(Job::getCreatedAt));

        Set<Long> cpIds = recentJobPage.getRecords().stream()
                .map(Job::getCompanyId).collect(Collectors.toSet());
        Map<Long, CompanyProfile> cpMap = cpIds.isEmpty() ? Map.of() :
                companyProfileMapper.selectList(
                        new LambdaQueryWrapper<CompanyProfile>().in(CompanyProfile::getId, cpIds))
                        .stream().collect(Collectors.toMap(CompanyProfile::getId, v -> v, (a, b) -> a));

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
            CompanyProfile cp = cpMap.get(job.getCompanyId());
            if (cp != null) {
                jv.setCompanyName(cp.getCompanyName());
                jv.setCompanyLogo(cp.getLogo());
                jv.setIndustry(cp.getIndustry());
            }
            return jv;
        }).collect(Collectors.toList());
        vo.setRecentJobs(recentJobs);

        // 申请状态统计（key 用数字字符串，与管理员前端 applicationStatusMap 保持一致）
        Map<String, Long> statusStats = new LinkedHashMap<>();
        statusStats.put("0", applicationMapper.selectCount(
                new LambdaQueryWrapper<Application>().eq(Application::getStatus, 0)));
        statusStats.put("1", applicationMapper.selectCount(
                new LambdaQueryWrapper<Application>().eq(Application::getStatus, 1)));
        statusStats.put("2", applicationMapper.selectCount(
                new LambdaQueryWrapper<Application>().eq(Application::getStatus, 2)));
        statusStats.put("3", applicationMapper.selectCount(
                new LambdaQueryWrapper<Application>().eq(Application::getStatus, 3)));
        statusStats.put("4", applicationMapper.selectCount(
                new LambdaQueryWrapper<Application>().eq(Application::getStatus, 4)));
        statusStats.put("5", applicationMapper.selectCount(
                new LambdaQueryWrapper<Application>().eq(Application::getStatus, 5)));
        vo.setApplicationStatusStats(statusStats);

        // 行业分布统计 Top10（按数量降序）
        List<CompanyProfile> allCompanies = companyProfileMapper.selectList(
                new LambdaQueryWrapper<CompanyProfile>().isNotNull(CompanyProfile::getIndustry));
        Map<String, Long> industryStats = allCompanies.stream()
                .filter(c -> StringUtils.hasText(c.getIndustry()))
                .collect(Collectors.groupingBy(CompanyProfile::getIndustry, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new));
        vo.setIndustryStats(industryStats);

        // 月度申请统计（最近 6 个月）
        List<Map<String, Object>> monthlyStats = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        for (int i = 5; i >= 0; i--) {
            LocalDateTime monthStart = now.minusMonths(i)
                    .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
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

    @Override
    public DashboardVO getCompanyDashboard(Long userId) {
        CompanyProfile profile = companyProfileMapper.selectOne(
                new LambdaQueryWrapper<CompanyProfile>().eq(CompanyProfile::getUserId, userId));
        if (profile == null) {
            return new DashboardVO();
        }
        Long companyProfileId = profile.getId();
        DashboardVO vo = new DashboardVO();

        // 本企业岗位数
        long jobCount = jobMapper.selectCount(
                new LambdaQueryWrapper<Job>().eq(Job::getCompanyId, companyProfileId));
        vo.setTotalJobs(jobCount);

        // 本企业所有岗位 ID
        List<Long> jobIds = jobMapper.selectList(
                        new LambdaQueryWrapper<Job>().eq(Job::getCompanyId, companyProfileId)
                                .select(Job::getId))
                .stream().map(Job::getId).collect(Collectors.toList());

        if (jobIds.isEmpty()) {
            vo.setTotalApplications(0L);
            vo.setApplicationStatusStats(new LinkedHashMap<>());
            vo.setRecentJobs(List.of());
            return vo;
        }

        // 本企业投递总数
        long applicationCount = applicationMapper.selectCount(
                new LambdaQueryWrapper<Application>().in(Application::getJobId, jobIds));
        vo.setTotalApplications(applicationCount);

        // 申请状态统计（用中文 key，与企业前端保持一致）
        Map<String, Long> statusStats = new LinkedHashMap<>();
        for (int s = 0; s <= 5; s++) {
            String label = appStatusLabel(s);
            final int st = s;
            statusStats.put(label, applicationMapper.selectCount(
                    new LambdaQueryWrapper<Application>()
                            .in(Application::getJobId, jobIds)
                            .eq(Application::getStatus, st)));
        }
        vo.setApplicationStatusStats(statusStats);

        // 最近发布的 5 个岗位
        Page<Job> recentPage = jobMapper.selectPage(new Page<>(1, 5),
                new LambdaQueryWrapper<Job>()
                        .eq(Job::getCompanyId, companyProfileId)
                        .orderByDesc(Job::getCreatedAt));
        List<JobVO> recentJobs = recentPage.getRecords().stream().map(job -> {
            JobVO jv = new JobVO();
            jv.setId(job.getId());
            jv.setTitle(job.getTitle());
            jv.setJobType(job.getJobType());
            jv.setSalaryMin(job.getSalaryMin());
            jv.setSalaryMax(job.getSalaryMax());
            jv.setCity(job.getCity());
            jv.setCategory(job.getCategory());
            jv.setStatus(job.getStatus());
            jv.setCreatedAt(job.getCreatedAt());
            jv.setCompanyName(profile.getCompanyName());
            jv.setCompanyLogo(profile.getLogo());
            return jv;
        }).collect(Collectors.toList());
        vo.setRecentJobs(recentJobs);

        return vo;
    }

    private String appStatusLabel(int status) {
        switch (status) {
            case 0: return "待查看";
            case 1: return "已查看";
            case 2: return "面试中";
            case 3: return "已录用";
            case 4: return "已拒绝";
            case 5: return "已撤回";
            default: return String.valueOf(status);
        }
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
