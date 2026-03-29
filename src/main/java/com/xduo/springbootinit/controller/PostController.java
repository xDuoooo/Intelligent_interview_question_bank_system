package com.xduo.springbootinit.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xduo.springbootinit.common.BaseResponse;
import com.xduo.springbootinit.common.DeleteRequest;
import com.xduo.springbootinit.common.ErrorCode;
import com.xduo.springbootinit.common.ResultUtils;
import com.xduo.springbootinit.constant.PostConstant;
import com.xduo.springbootinit.constant.UserConstant;
import com.xduo.springbootinit.exception.BusinessException;
import com.xduo.springbootinit.exception.ThrowUtils;
import com.xduo.springbootinit.model.dto.post.PostAddRequest;
import com.xduo.springbootinit.model.dto.post.PostEditRequest;
import com.xduo.springbootinit.model.dto.post.PostOperateRequest;
import com.xduo.springbootinit.model.dto.post.PostQueryRequest;
import com.xduo.springbootinit.model.dto.post.PostReviewRequest;
import com.xduo.springbootinit.model.dto.post.PostUpdateRequest;
import com.xduo.springbootinit.model.entity.Post;
import com.xduo.springbootinit.model.entity.User;
import com.xduo.springbootinit.model.vo.PostVO;
import com.xduo.springbootinit.service.NotificationService;
import com.xduo.springbootinit.service.PostService;
import com.xduo.springbootinit.service.UserService;
import com.xduo.springbootinit.manager.AiManager;
import java.util.List;
import java.util.Date;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 帖子接口

 */
@RestController
@RequestMapping("/post")
@Slf4j
public class PostController {

    @Resource
    private PostService postService;

    @Resource
    private UserService userService;

    @Resource
    private NotificationService notificationService;

    @Resource
    private AiManager aiManager;

    // region 增删改查

    /**
     * 创建
     *
     * @param postAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addPost(@RequestBody PostAddRequest postAddRequest, HttpServletRequest request) {
        if (postAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Post post = new Post();
        BeanUtils.copyProperties(postAddRequest, post);
        List<String> tags = postAddRequest.getTags();
        if (tags != null) {
            post.setTags(JSONUtil.toJsonStr(tags));
        }
        postService.validPost(post, true);
        User loginUser = userService.getLoginUser(request);
        post.setUserId(loginUser.getId());
        post.setFavourNum(0);
        post.setThumbNum(0);
        applyPostReviewPolicy(post, loginUser, true);
        boolean result = postService.save(post);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newPostId = post.getId();
        return ResultUtils.success(newPostId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePost(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Post oldPost = postService.getById(id);
        ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldPost.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = postService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param postUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePost(@RequestBody PostUpdateRequest postUpdateRequest) {
        if (postUpdateRequest == null || postUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Post post = new Post();
        BeanUtils.copyProperties(postUpdateRequest, post);
        List<String> tags = postUpdateRequest.getTags();
        if (tags != null) {
            post.setTags(JSONUtil.toJsonStr(tags));
        }
        // 参数校验
        postService.validPost(post, false);
        long id = postUpdateRequest.getId();
        // 判断是否存在
        Post oldPost = postService.getById(id);
        ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = postService.updateById(post);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<PostVO> getPostVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Post post = postService.getById(id);
        if (post == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        User loginUser = userService.getLoginUserPermitNull(request);
        if (!isPostVisible(post, loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "帖子暂不可见");
        }
        return ResultUtils.success(postService.getPostVO(post, request));
    }

    /**
     * 获取热门帖子
     */
    @GetMapping("/hot/list")
    public BaseResponse<List<PostVO>> listHotPost(HttpServletRequest request) {
        return ResultUtils.success(postService.listHotPostVO(6, request));
    }

    /**
     * 获取相关帖子
     */
    @GetMapping("/related/list")
    public BaseResponse<List<PostVO>> listRelatedPost(long postId,
                                                      Integer size,
                                                      HttpServletRequest request) {
        ThrowUtils.throwIf(postId <= 0, ErrorCode.PARAMS_ERROR);
        return ResultUtils.success(postService.listRelatedPostVO(postId, size == null ? 4 : size, request));
    }

    /**
     * 分页获取列表（仅管理员）
     *
     * @param postQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Post>> listPostByPage(@RequestBody PostQueryRequest postQueryRequest) {
        ThrowUtils.throwIf(postQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        ThrowUtils.throwIf(current < 1 || size < 1 || size > 100, ErrorCode.PARAMS_ERROR, "分页参数不合法");
        Page<Post> postPage = postService.page(new Page<>(current, size),
                postService.getQueryWrapper(postQueryRequest));
        return ResultUtils.success(postPage);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param postQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PostVO>> listPostVOByPage(@RequestBody PostQueryRequest postQueryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(postQueryRequest == null, ErrorCode.PARAMS_ERROR);
        postQueryRequest.setReviewStatus(PostConstant.REVIEW_STATUS_APPROVED);
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(current < 1 || size < 1 || size > 20, ErrorCode.PARAMS_ERROR, "分页参数不合法");
        Page<Post> postPage = postService.page(new Page<>(current, size),
                postService.getQueryWrapper(postQueryRequest));
        return ResultUtils.success(postService.getPostVOPage(postPage, request));
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param postQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<PostVO>> listMyPostVOByPage(@RequestBody PostQueryRequest postQueryRequest,
            HttpServletRequest request) {
        if (postQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        postQueryRequest.setUserId(loginUser.getId());
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(current < 1 || size < 1 || size > 20, ErrorCode.PARAMS_ERROR, "分页参数不合法");
        Page<Post> postPage = postService.page(new Page<>(current, size),
                postService.getQueryWrapper(postQueryRequest));
        return ResultUtils.success(postService.getPostVOPage(postPage, request));
    }

    // endregion

    /**
     * 分页搜索（从 ES 查询，封装类）
     *
     * @param postQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/search/page/vo")
    public BaseResponse<Page<PostVO>> searchPostVOByPage(@RequestBody PostQueryRequest postQueryRequest,
            HttpServletRequest request) {
        ThrowUtils.throwIf(postQueryRequest == null, ErrorCode.PARAMS_ERROR);
        postQueryRequest.setReviewStatus(PostConstant.REVIEW_STATUS_APPROVED);
        long current = postQueryRequest.getCurrent();
        long size = postQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(current < 1 || size < 1 || size > 20, ErrorCode.PARAMS_ERROR, "分页参数不合法");
        Page<Post> postPage;
        try {
            postPage = postService.searchFromEs(postQueryRequest);
        } catch (Exception e) {
            log.warn("post search fallback to database, reason={}", e.getMessage());
            postPage = postService.page(new Page<>(current, size), postService.getQueryWrapper(postQueryRequest));
        }
        return ResultUtils.success(postService.getPostVOPage(postPage, request));
    }

    /**
     * 编辑（用户）
     *
     * @param postEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPost(@RequestBody PostEditRequest postEditRequest, HttpServletRequest request) {
        if (postEditRequest == null || postEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Post post = new Post();
        BeanUtils.copyProperties(postEditRequest, post);
        List<String> tags = postEditRequest.getTags();
        if (tags != null) {
            post.setTags(JSONUtil.toJsonStr(tags));
        }
        // 参数校验
        postService.validPost(post, false);
        User loginUser = userService.getLoginUser(request);
        long id = postEditRequest.getId();
        // 判断是否存在
        Post oldPost = postService.getById(id);
        ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldPost.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        applyPostReviewPolicy(post, loginUser, false);
        boolean result = postService.updateById(post);
        return ResultUtils.success(result);
    }

    /**
     * 审核帖子（仅管理员）
     */
    @PostMapping("/review")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> reviewPost(@RequestBody PostReviewRequest postReviewRequest,
                                            HttpServletRequest request) {
        ThrowUtils.throwIf(postReviewRequest == null || postReviewRequest.getId() == null || postReviewRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR);
        Integer reviewStatus = postReviewRequest.getReviewStatus();
        ThrowUtils.throwIf(reviewStatus == null || !PostConstant.ALLOWED_ADMIN_REVIEW_STATUS_SET.contains(reviewStatus),
                ErrorCode.PARAMS_ERROR, "审核状态不合法");
        String reviewMessage = StringUtils.trimToNull(postReviewRequest.getReviewMessage());
        if (PostConstant.REVIEW_STATUS_REJECTED == reviewStatus) {
            ThrowUtils.throwIf(StringUtils.isBlank(reviewMessage), ErrorCode.PARAMS_ERROR, "驳回时请填写审核意见");
        }
        ThrowUtils.throwIf(StringUtils.length(reviewMessage) > 512, ErrorCode.PARAMS_ERROR, "审核意见过长");
        User adminUser = userService.getLoginUser(request);
        Post oldPost = postService.getById(postReviewRequest.getId());
        ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR);

        Post updatePost = new Post();
        updatePost.setId(oldPost.getId());
        updatePost.setReviewStatus(reviewStatus);
        updatePost.setReviewMessage(reviewMessage);
        updatePost.setReviewUserId(adminUser.getId());
        updatePost.setReviewTime(new Date());
        boolean result = postService.updateById(updatePost);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        notificationService.sendNotification(
                oldPost.getUserId(),
                PostConstant.REVIEW_STATUS_APPROVED == reviewStatus ? "你的帖子已审核通过" : "你的帖子未通过审核",
                buildPostReviewNotificationContent(oldPost, reviewStatus, reviewMessage),
                "post_review",
                oldPost.getId()
        );
        return ResultUtils.success(true);
    }

    /**
     * 帖子运营设置（仅管理员）
     */
    @PostMapping("/operate")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> operatePost(@RequestBody PostOperateRequest postOperateRequest) {
        ThrowUtils.throwIf(postOperateRequest == null || postOperateRequest.getId() == null || postOperateRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR);
        Post oldPost = postService.getById(postOperateRequest.getId());
        ThrowUtils.throwIf(oldPost == null, ErrorCode.NOT_FOUND_ERROR);
        Post updatePost = new Post();
        updatePost.setId(oldPost.getId());
        if (postOperateRequest.getIsTop() != null) {
            updatePost.setIsTop(postOperateRequest.getIsTop() > 0 ? 1 : 0);
        }
        if (postOperateRequest.getIsFeatured() != null) {
            updatePost.setIsFeatured(postOperateRequest.getIsFeatured() > 0 ? 1 : 0);
        }
        boolean result = postService.updateById(updatePost);
        return ResultUtils.success(result);
    }

    private void applyPostReviewPolicy(Post post, User operator, boolean add) {
        if (userService.isAdmin(operator)) {
            post.setReviewStatus(PostConstant.REVIEW_STATUS_APPROVED);
            post.setReviewMessage("管理员直接发布");
            post.setReviewUserId(operator.getId());
            post.setReviewTime(new Date());
            post.setIsTop(post.getIsTop() == null ? 0 : post.getIsTop());
            post.setIsFeatured(post.getIsFeatured() == null ? 0 : post.getIsFeatured());
            return;
        }
        PostAutoReviewResult autoReviewResult = autoReviewPost(post);
        post.setReviewStatus(autoReviewResult.reviewStatus());
        post.setReviewMessage(autoReviewResult.reviewMessage());
        post.setReviewUserId(null);
        post.setReviewTime(PostConstant.REVIEW_STATUS_APPROVED == autoReviewResult.reviewStatus() ? new Date() : null);
        if (add) {
            post.setIsTop(0);
            post.setIsFeatured(0);
        }
    }

    private PostAutoReviewResult autoReviewPost(Post post) {
        String combinedText = StringUtils.defaultString(post.getTitle()) + "\n" + StringUtils.defaultString(post.getContent());
        String lowerCaseText = combinedText.toLowerCase();
        if (containsAny(lowerCaseText, "赌博", "色情", "外挂", "vpn", "代考", "作弊器", "刷单", "telegram", "qq裙", "qq群", "vx:", "微信:", "联系方式")) {
            return new PostAutoReviewResult(PostConstant.REVIEW_STATUS_REJECTED, "内容包含高风险词，已自动拦截，请修改后重试");
        }
        if (StringUtils.length(StringUtils.trimToEmpty(post.getContent())) < 80) {
            return new PostAutoReviewResult(PostConstant.REVIEW_STATUS_PENDING, "内容较短，已进入人工复核");
        }
        try {
            String aiResult = aiManager.doChat(buildPostAutoReviewSystemPrompt(), buildPostAutoReviewUserPrompt(post));
            String normalized = aiResult.toLowerCase();
            if (normalized.contains("reject")) {
                return new PostAutoReviewResult(PostConstant.REVIEW_STATUS_REJECTED, extractReviewMessage(aiResult, "AI 识别到内容存在风险，建议人工复核"));
            }
            if (normalized.contains("pending")) {
                return new PostAutoReviewResult(PostConstant.REVIEW_STATUS_PENDING, extractReviewMessage(aiResult, "AI 建议人工复核"));
            }
            return new PostAutoReviewResult(PostConstant.REVIEW_STATUS_APPROVED, "系统自动审核通过");
        } catch (Exception e) {
            log.warn("post auto review fallback, reason={}", e.getMessage());
            return new PostAutoReviewResult(PostConstant.REVIEW_STATUS_APPROVED, "规则自动审核通过");
        }
    }

    private String buildPostAutoReviewSystemPrompt() {
        return "你是内容审核助手。请仅输出一行 JSON，格式为 {\"decision\":\"approve|pending|reject\",\"reason\":\"...\"}。"
                + "如果内容明显违规、广告导流、联系方式引流、色情赌博作弊等，decision=reject；"
                + "如果内容有风险但不确定，decision=pending；"
                + "正常的面经、项目复盘、技术经验分享，decision=approve。";
    }

    private String buildPostAutoReviewUserPrompt(Post post) {
        return "请审核以下帖子内容：\n标题：" + StringUtils.defaultString(post.getTitle())
                + "\n标签：" + StringUtils.defaultString(post.getTags())
                + "\n内容：" + StringUtils.defaultString(post.getContent());
    }

    private String extractReviewMessage(String aiResult, String defaultMessage) {
        if (StringUtils.isBlank(aiResult)) {
            return defaultMessage;
        }
        int reasonIndex = aiResult.indexOf("\"reason\"");
        if (reasonIndex < 0) {
            return defaultMessage;
        }
        String text = aiResult.substring(reasonIndex);
        int colonIndex = text.indexOf(':');
        if (colonIndex < 0) {
            return defaultMessage;
        }
        String value = text.substring(colonIndex + 1).replaceAll("[\"{}]", "").trim();
        return StringUtils.isBlank(value) ? defaultMessage : StringUtils.abbreviate(value, 120);
    }

    private String buildPostReviewNotificationContent(Post post, Integer reviewStatus, String reviewMessage) {
        StringBuilder contentBuilder = new StringBuilder("你的帖子《")
                .append(post.getTitle())
                .append("》");
        if (PostConstant.REVIEW_STATUS_APPROVED == reviewStatus) {
            contentBuilder.append("已通过审核，现在可以在社区中公开展示。");
        } else {
            contentBuilder.append("未通过审核，请根据审核意见修改后重新提交。");
        }
        if (StringUtils.isNotBlank(reviewMessage)) {
            contentBuilder.append(" 审核意见：").append(reviewMessage);
        }
        return contentBuilder.toString();
    }

    private boolean isPostVisible(Post post, User loginUser) {
        Integer reviewStatus = post.getReviewStatus();
        if (reviewStatus == null || PostConstant.REVIEW_STATUS_APPROVED == reviewStatus) {
            return true;
        }
        if (loginUser == null) {
            return false;
        }
        return loginUser.getId().equals(post.getUserId()) || userService.isAdmin(loginUser);
    }

    private boolean containsAny(String text, String... keywords) {
        if (StringUtils.isBlank(text) || keywords == null) {
            return false;
        }
        for (String keyword : keywords) {
            if (StringUtils.isNotBlank(keyword) && text.contains(keyword.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private record PostAutoReviewResult(int reviewStatus, String reviewMessage) {
    }

}
