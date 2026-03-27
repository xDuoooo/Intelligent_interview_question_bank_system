package com.xduo.springbootinit.controller;

import cn.hutool.core.io.FileUtil;
import com.xduo.springbootinit.common.BaseResponse;
import com.xduo.springbootinit.common.ErrorCode;
import com.xduo.springbootinit.common.ResultUtils;
import com.xduo.springbootinit.constant.FileConstant;
import com.xduo.springbootinit.exception.BusinessException;
import com.xduo.springbootinit.manager.CosManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;
import com.xduo.springbootinit.model.dto.file.UploadFileRequest;
import com.xduo.springbootinit.model.entity.User;
import com.xduo.springbootinit.model.enums.FileUploadBizEnum;
import com.xduo.springbootinit.service.UserService;
import java.io.File;
import java.util.Arrays;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件接口

 */
@RestController
@RequestMapping("/file")
@Slf4j
public class FileController {

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Value("${cos.client.accessKey:xxx}")
    private String cosAccessKey;

    /**
     * 文件上传
     *
     * @param multipartFile
     * @param uploadFileRequest
     * @param request
     * @return
     */
    @PostMapping("/upload")
    public BaseResponse<String> uploadFile(@RequestPart("file") MultipartFile multipartFile,
            UploadFileRequest uploadFileRequest, HttpServletRequest request) {
        String biz = uploadFileRequest.getBiz();
        FileUploadBizEnum fileUploadBizEnum = FileUploadBizEnum.getEnumByValue(biz);
        if (fileUploadBizEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        
        // 频率限制：单用户每小时限 10 次上传
        String redisKey = "user:upload:limit:" + loginUser.getId();
        String uploadCountStr = stringRedisTemplate.opsForValue().get(redisKey);
        int uploadCount = uploadCountStr == null ? 0 : Integer.parseInt(uploadCountStr);
        if (uploadCount >= 10) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "上传过于频繁，请一小时后再试");
        }
        
        validFile(multipartFile, fileUploadBizEnum);
        
        // 计数增加
        if (uploadCount == 0) {
            stringRedisTemplate.opsForValue().set(redisKey, "1", 1, TimeUnit.HOURS);
        } else {
            stringRedisTemplate.opsForValue().increment(redisKey);
        }

        // 文件目录：根据业务、用户来划分
        String uuid = RandomStringUtils.randomAlphanumeric(8);
        String filename = uuid + "-" + multipartFile.getOriginalFilename();
        String filepath = String.format("/%s/%s/%s", fileUploadBizEnum.getValue(), loginUser.getId(), filename);
        
        // 优先使用 COS，若未配置则使用本地存储
        if (cosAccessKey != null && !cosAccessKey.equals("xxx")) {
            File file = null;
            try {
                file = File.createTempFile(filepath, null);
                multipartFile.transferTo(file);
                cosManager.putObject(filepath, file);
                return ResultUtils.success(FileConstant.COS_HOST + filepath);
            } catch (Exception e) {
                log.error("COS upload error, filepath = " + filepath, e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传至云存储失败");
            } finally {
                if (file != null) file.delete();
            }
        } else {
            // 本地存储兜底
            try {
                String uploadDir = System.getProperty("user.dir") + File.separator + "uploads";
                Path bizPath = Paths.get(uploadDir, fileUploadBizEnum.getValue(), String.valueOf(loginUser.getId()));
                if (!Files.exists(bizPath)) {
                    Files.createDirectories(bizPath);
                }
                Path targetPath = bizPath.resolve(filename);
                Files.copy(multipartFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                
                // 返回本地访问 URL (带 context-path /api)
                return ResultUtils.success("/api/files" + filepath);
            } catch (Exception e) {
                log.error("Local upload error, filepath = " + filepath, e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "本地文件上传失败");
            }
        }
    }

    /**
     * 校验文件
     *
     * @param multipartFile
     * @param fileUploadBizEnum 业务类型
     */
    private void validFile(MultipartFile multipartFile, FileUploadBizEnum fileUploadBizEnum) {
        // 文件大小
        long fileSize = multipartFile.getSize();
        // 文件后缀
        String fileSuffix = FileUtil.getSuffix(multipartFile.getOriginalFilename());
        final long ONE_M = 1024 * 1024L;
        if (FileUploadBizEnum.USER_AVATAR.equals(fileUploadBizEnum)) {
            if (fileSize > ONE_M) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件大小不能超过 1M");
            }
            if (!Arrays.asList("jpeg", "jpg", "svg", "png", "webp").contains(fileSuffix)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件类型错误");
            }
        }
    }
}
