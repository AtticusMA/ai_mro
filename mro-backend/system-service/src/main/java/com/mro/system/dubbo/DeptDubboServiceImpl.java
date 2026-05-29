package com.mro.system.dubbo;

import com.mro.common.dubbo.system.request.CreateDeptCommand;
import com.mro.common.dubbo.system.response.DeptDTO;
import com.mro.common.dubbo.system.response.DeptTreeDTO;
import com.mro.common.dubbo.system.request.UpdateDeptCommand;
import com.mro.common.dubbo.system.request.*;
import com.mro.common.dubbo.system.response.*;
import com.mro.common.dubbo.system.service.DeptDubboService;
import com.mro.system.entity.SysDept;
import com.mro.system.mapper.SysDeptMapper;
import com.mro.system.service.DeptService;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.rpc.RpcContext;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@DubboService
public class DeptDubboServiceImpl implements DeptDubboService {

    @Autowired
    private DeptService deptService;

    @Autowired
    private SysDeptMapper deptMapper;

    @Override
    public List<DeptTreeDTO> getDeptTree() {
        return deptService.getDeptTree();
    }

    @Override
    public DeptDTO getDeptById(Long deptId) {
        return deptService.getDeptById(deptId);
    }

    @Override
    public Long createDept(CreateDeptCommand cmd) {
        Long operatorId = getOperatorId();
        Long operatorDeptId = getOperatorDeptId();
        deptService.createDept(cmd, operatorId, operatorDeptId);
        return 0L;
    }

    @Override
    public void updateDept(UpdateDeptCommand cmd) {
        Long operatorId = getOperatorId();
        deptService.updateDept(cmd, operatorId);
    }

    @Override
    public void deleteDept(Long deptId) {
        Long operatorId = getOperatorId();
        deptService.deleteDept(deptId, operatorId);
    }

    @Override
    public List<DeptDTO> getDeptsByIds(List<Long> deptIds) {
        return deptMapper.selectBatchIds(deptIds).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getDescendantDeptIds(Long deptId) {
        return deptMapper.selectDescendantIds(deptId);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Long getOperatorId() {
        String v = RpcContext.getServiceContext().getAttachment("userId");
        return v != null ? Long.parseLong(v) : 1L;
    }

    private Long getOperatorDeptId() {
        String v = RpcContext.getServiceContext().getAttachment("deptId");
        return v != null ? Long.parseLong(v) : 1L;
    }

    private DeptDTO toDTO(SysDept dept) {
        return new DeptDTO(
                dept.getId(),
                dept.getDeptName(),
                dept.getDeptCode(),
                dept.getParentId(),
                dept.getAncestors(),
                dept.getOrderNum(),
                dept.getLeader(),
                dept.getPhone(),
                dept.getEmail(),
                dept.getStatus());
    }
}
