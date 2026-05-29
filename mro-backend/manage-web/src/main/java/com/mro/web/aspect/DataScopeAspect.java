package com.mro.web.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mro.common.dubbo.system.response.UserDataScopeDTO;
import com.mro.common.dubbo.system.request.*;
import com.mro.common.dubbo.system.response.*;
import com.mro.common.dubbo.system.service.RoleDubboService;
import com.mro.web.context.UserContext;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.rpc.RpcContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据权限切面。
 *
 * 拦截标注了 @DataScope 的 Controller 方法，在方法执行前：
 * 1. 通过 Dubbo 查询当前用户的有效 dataScope 及自定义部门列表
 * 2. 根据 dataScope 类型计算 "可见 deptId 列表"（或 "仅本人" 标志）
 * 3. 将结果序列化后写入 Dubbo Attachment，随本次 RPC 调用传递给 system-service
 *
 * Attachment 键约定：
 *   ds.deptIds  — JSON 数组字符串，如 "[1,2,3]"，null 表示不限制（全部）
 *   ds.selfOnly — "true" 表示 dataScope=4（仅本人），Service 层用 createUserId 过滤
 */
@Aspect
@Component
public class DataScopeAspect {

    private static final Logger log = LoggerFactory.getLogger(DataScopeAspect.class);

    /** Attachment key：可见部门 id 列表（JSON 数组），absent / null = 不限制 */
    public static final String ATT_DEPT_IDS  = "ds.deptIds";
    /** Attachment key：仅本人标志 */
    public static final String ATT_SELF_ONLY = "ds.selfOnly";

    @DubboReference(version = "1.0.0", timeout = 3000, retries = 0)
    private RoleDubboService roleDubboService;

    @Autowired
    private ObjectMapper objectMapper;

    @Around("@annotation(com.mro.web.annotation.DataScope)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Long userId  = UserContext.getUserId();
        Long deptId  = UserContext.getDeptId();

        if (userId == null) {
            return pjp.proceed();
        }

        RpcContext.getClientAttachment().setAttachment("userId", userId.toString());

        try {
            UserDataScopeDTO ds = roleDubboService.getDataScopeByUserId(userId);
            applyAttachment(ds, userId, deptId);
        } catch (Exception e) {
            log.warn("DataScopeAspect: failed to resolve dataScope for userId={}, fallback to selfOnly", userId, e);
            RpcContext.getClientAttachment().setAttachment(ATT_SELF_ONLY, "true");
        }

        return pjp.proceed();
    }

    // -------------------------------------------------------------------------

    private void applyAttachment(UserDataScopeDTO ds, Long userId, Long deptId) throws Exception {
        int scope = ds.dataScope() == null ? 4 : ds.dataScope();

        switch (scope) {
            case 1 -> {
                // 全部数据：不写 deptIds，Service 层不加过滤
            }
            case 2 -> {
                // 本部门
                String json = objectMapper.writeValueAsString(List.of(deptId));
                RpcContext.getClientAttachment().setAttachment(ATT_DEPT_IDS, json);
            }
            case 3 -> {
                // 本部门及子部门：子部门列表由 system-service 的 Filter 展开
                // 这里只传 "需要展开" 标志 + 根部门 id，避免 manage-web 再发一次 Dubbo 查询
                // system-service DataScopeDubboFilter 识别后调用 SysDeptMapper 展开
                RpcContext.getClientAttachment().setAttachment(ATT_DEPT_IDS, "expand:" + deptId);
            }
            case 4 -> {
                // 仅本人
                RpcContext.getClientAttachment().setAttachment(ATT_SELF_ONLY, "true");
            }
            case 5 -> {
                // 自定义部门
                List<Long> customDepts = ds.customDeptIds();
                if (customDepts == null || customDepts.isEmpty()) {
                    // 无自定义配置，退化为仅本人
                    RpcContext.getClientAttachment().setAttachment(ATT_SELF_ONLY, "true");
                } else {
                    String json = objectMapper.writeValueAsString(customDepts);
                    RpcContext.getClientAttachment().setAttachment(ATT_DEPT_IDS, json);
                }
            }
            default -> RpcContext.getClientAttachment().setAttachment(ATT_SELF_ONLY, "true");
        }
    }
}
