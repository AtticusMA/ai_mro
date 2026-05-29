package com.mro.system.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mro.system.context.DataScopeContext;
import com.mro.system.mapper.SysDeptMapper;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

import static org.apache.dubbo.common.constants.CommonConstants.PROVIDER;

/**
 * Dubbo Provider 侧 Filter，在每次 RPC 调用入口：
 * 1. 读取 Attachment 中的数据权限信息（ds.deptIds / ds.selfOnly）
 * 2. 若 ds.deptIds 以 "expand:" 开头，展开本部门+子部门 id 列表
 * 3. 将结果存入 DataScopeContext（ThreadLocal），供 Service 层查询时使用
 * 4. 调用结束后清理 ThreadLocal
 */
@Activate(group = PROVIDER)
public class DataScopeDubboFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(DataScopeDubboFilter.class);

    private static final String ATT_DEPT_IDS  = "ds.deptIds";
    private static final String ATT_SELF_ONLY = "ds.selfOnly";
    private static final String EXPAND_PREFIX = "expand:";

    @Autowired
    private SysDeptMapper deptMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        try {
            String deptIdsVal  = RpcContext.getServiceContext().getAttachment(ATT_DEPT_IDS);
            String selfOnlyVal = RpcContext.getServiceContext().getAttachment(ATT_SELF_ONLY);
            String userIdVal   = RpcContext.getServiceContext().getAttachment("userId");

            if (userIdVal != null) {
                try { DataScopeContext.setUserId(Long.parseLong(userIdVal)); } catch (Exception ignored) {}
            }

            if ("true".equals(selfOnlyVal)) {
                DataScopeContext.setSelfOnly(true);
            } else if (deptIdsVal != null) {
                if (deptIdsVal.startsWith(EXPAND_PREFIX)) {
                    // dataScope=3：展开本部门+子部门
                    long rootDeptId = Long.parseLong(deptIdsVal.substring(EXPAND_PREFIX.length()));
                    List<Long> ids = new ArrayList<>();
                    ids.add(rootDeptId);
                    List<Long> descendants = deptMapper.selectDescendantIds(rootDeptId);
                    if (descendants != null) ids.addAll(descendants);
                    DataScopeContext.setDeptIds(ids);
                } else {
                    // dataScope=2 或 5：直接反序列化
                    List<Long> ids = objectMapper.readValue(deptIdsVal, new TypeReference<List<Long>>() {});
                    DataScopeContext.setDeptIds(ids);
                }
            }
            // deptIdsVal == null && selfOnly == false → dataScope=1，不限制，Context 保持空

            return invoker.invoke(invocation);
        } catch (Exception e) {
            log.warn("DataScopeDubboFilter parse error, skip data scope", e);
            return invoker.invoke(invocation);
        } finally {
            DataScopeContext.clear();
        }
    }
}
