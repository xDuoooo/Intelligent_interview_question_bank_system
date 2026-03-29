package com.xduo.springbootinit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xduo.springbootinit.common.ErrorCode;
import com.xduo.springbootinit.exception.BusinessException;
import com.xduo.springbootinit.mapper.SystemConfigMapper;
import com.xduo.springbootinit.model.dto.systemconfig.SystemConfigUpdateRequest;
import com.xduo.springbootinit.model.entity.SystemConfig;
import com.xduo.springbootinit.model.vo.SystemConfigVO;
import com.xduo.springbootinit.service.SystemConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 系统配置服务实现
 */
@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {

    private static final Object INIT_LOCK = new Object();

    private static final String DEFAULT_SITE_NAME = "IntelliFace 智面";

    private static final String DEFAULT_SEO_KEYWORDS = "面试, 刷题, Java, 互联网";

    private static final String DEFAULT_ANNOUNCEMENT = "欢迎来到智面 1.0 版本，体验 AI 智能面经！";

    @Override
    public SystemConfig getCurrentConfig() {
        QueryWrapper<SystemConfig> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("id").last("limit 1");
        SystemConfig systemConfig = this.getOne(queryWrapper, false);
        if (systemConfig != null) {
            return systemConfig;
        }
        synchronized (INIT_LOCK) {
            SystemConfig latestConfig = this.getOne(queryWrapper, false);
            if (latestConfig != null) {
                return latestConfig;
            }
            SystemConfig defaultConfig = buildDefaultConfig();
            boolean saved = this.save(defaultConfig);
            if (!saved) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化系统配置失败");
            }
            return defaultConfig;
        }
    }

    @Override
    public SystemConfigVO getSystemConfigVO() {
        return toVO(getCurrentConfig());
    }

    @Override
    public SystemConfigVO getPublicSystemConfigVO() {
        return toVO(getCurrentConfig());
    }

    @Override
    public boolean updateCurrentConfig(SystemConfigUpdateRequest systemConfigUpdateRequest) {
        if (systemConfigUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        String siteName = StringUtils.trimToEmpty(systemConfigUpdateRequest.getSiteName());
        if (StringUtils.isBlank(siteName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "站点名称不能为空");
        }
        if (siteName.length() > 64) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "站点名称不能超过 64 个字符");
        }
        String seoKeywords = StringUtils.trimToEmpty(systemConfigUpdateRequest.getSeoKeywords());
        if (seoKeywords.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "SEO 关键词不能超过 512 个字符");
        }
        String announcement = StringUtils.trimToEmpty(systemConfigUpdateRequest.getAnnouncement());
        if (announcement.length() > 1024) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "系统公告不能超过 1024 个字符");
        }
        SystemConfig currentConfig = getCurrentConfig();
        SystemConfig updateConfig = new SystemConfig();
        updateConfig.setId(currentConfig.getId());
        updateConfig.setSiteName(siteName);
        updateConfig.setSeoKeywords(seoKeywords);
        updateConfig.setAnnouncement(announcement);
        updateConfig.setAllowRegister(Boolean.TRUE.equals(systemConfigUpdateRequest.getAllowRegister()) ? 1 : 0);
        updateConfig.setRequireCaptcha(Boolean.TRUE.equals(systemConfigUpdateRequest.getRequireCaptcha()) ? 1 : 0);
        updateConfig.setMaintenanceMode(Boolean.TRUE.equals(systemConfigUpdateRequest.getMaintenanceMode()) ? 1 : 0);
        return this.updateById(updateConfig);
    }

    @Override
    public boolean isAllowRegister() {
        return Integer.valueOf(1).equals(getCurrentConfig().getAllowRegister());
    }

    @Override
    public boolean isRequireCaptcha() {
        return Integer.valueOf(1).equals(getCurrentConfig().getRequireCaptcha());
    }

    @Override
    public boolean isMaintenanceMode() {
        return Integer.valueOf(1).equals(getCurrentConfig().getMaintenanceMode());
    }

    private SystemConfig buildDefaultConfig() {
        SystemConfig systemConfig = new SystemConfig();
        systemConfig.setSiteName(DEFAULT_SITE_NAME);
        systemConfig.setSeoKeywords(DEFAULT_SEO_KEYWORDS);
        systemConfig.setAnnouncement(DEFAULT_ANNOUNCEMENT);
        systemConfig.setAllowRegister(1);
        systemConfig.setRequireCaptcha(1);
        systemConfig.setMaintenanceMode(0);
        return systemConfig;
    }

    private SystemConfigVO toVO(SystemConfig systemConfig) {
        SystemConfigVO systemConfigVO = new SystemConfigVO();
        BeanUtils.copyProperties(systemConfig, systemConfigVO);
        systemConfigVO.setAllowRegister(Integer.valueOf(1).equals(systemConfig.getAllowRegister()));
        systemConfigVO.setRequireCaptcha(Integer.valueOf(1).equals(systemConfig.getRequireCaptcha()));
        systemConfigVO.setMaintenanceMode(Integer.valueOf(1).equals(systemConfig.getMaintenanceMode()));
        return systemConfigVO;
    }
}
