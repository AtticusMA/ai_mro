package com.mro.workcard.filter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mro.workcard.context.DataScopeContext;
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

import java.util.List;

import static org.apache.dubbo.common.constants.CommonConstants.PROVIDER;

@Activate(group = PROVIDER)
public class DataScopeDubboFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(DataScopeDubboFilter.class);

    private static final String ATT_DEPT_IDS  = "ds.deptIds";
    private static final String ATT_SELF_ONLY = "ds.selfOnly";

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
            } else if (deptIdsVal != null && !deptIdsVal.startsWith("expand:")) {
                List<Long> ids = objectMapper.readValue(deptIdsVal, new TypeReference<List<Long>>() {});
                DataScopeContext.setDeptIds(ids);
            }
            // expand: Workcard 暂无 deptId 字段，退化为不限制

            return invoker.invoke(invocation);
        } catch (Exception e) {
            log.warn("DataScopeDubboFilter parse error, skip data scope", e);
            return invoker.invoke(invocation);
        } finally {
            DataScopeContext.clear();
        }
    }
}
