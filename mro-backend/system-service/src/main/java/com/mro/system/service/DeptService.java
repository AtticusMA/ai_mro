package com.mro.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mro.common.core.constant.ErrorCode;
import com.mro.common.core.exception.BizException;
import com.mro.common.dubbo.system.request.CreateDeptCommand;
import com.mro.common.dubbo.system.response.DeptDTO;
import com.mro.common.dubbo.system.response.DeptTreeDTO;
import com.mro.common.dubbo.system.request.UpdateDeptCommand;
import com.mro.system.entity.SysDept;
import com.mro.system.mapper.SysDeptMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class DeptService {

    private static final Logger log = LoggerFactory.getLogger(DeptService.class);

    private static final String DEPT_TREE_CACHE = "sys:dept:tree";
    private static final long DEPT_TREE_TTL_MINUTES = 30L;

    @Autowired
    private SysDeptMapper deptMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Return the department tree. Results are cached in Redis for 30 minutes.
     */
    public List<DeptTreeDTO> getDeptTree() {
        String cached = redisTemplate.opsForValue().get(DEPT_TREE_CACHE);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, new TypeReference<List<DeptTreeDTO>>() {});
            } catch (Exception e) {
                log.warn("Failed to deserialize dept tree cache", e);
            }
        }

        List<SysDept> all = deptMapper.selectList(
                new LambdaQueryWrapper<SysDept>()
                        .eq(SysDept::getIsDeleted, 0)
                        .orderByAsc(SysDept::getOrderNum));

        List<DeptTreeDTO> tree = buildTree(all, 0L);

        try {
            String json = objectMapper.writeValueAsString(tree);
            redisTemplate.opsForValue().set(DEPT_TREE_CACHE, json, DEPT_TREE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("Failed to cache dept tree", e);
        }

        return tree;
    }

    /**
     * Fetch a single department by id. Throws BizException(4110) if not found.
     */
    public DeptDTO getDeptById(Long id) {
        SysDept dept = deptMapper.selectById(id);
        if (dept == null || dept.getIsDeleted() == 1) {
            throw new BizException(ErrorCode.SYS_DEPT_NOT_FOUND, "部门不存在");
        }
        return toDTO(dept);
    }

    /**
     * Create a new department. Validates deptCode uniqueness, computes ancestors,
     * inserts the record, and evicts the tree cache.
     */
    @Transactional(rollbackFor = Exception.class)
    public void createDept(CreateDeptCommand cmd, Long operatorId, Long operatorDeptId) {
        // Validate deptCode uniqueness
        Long count = deptMapper.selectCount(
                new LambdaQueryWrapper<SysDept>()
                        .eq(SysDept::getDeptCode, cmd.deptCode())
                        .eq(SysDept::getIsDeleted, 0));
        if (count > 0) {
            throw new BizException(4111, "部门编码已存在");
        }

        SysDept dept = new SysDept();
        dept.setDeptName(cmd.deptName());
        dept.setDeptCode(cmd.deptCode());
        dept.setParentId(cmd.parentId() == null ? 0L : cmd.parentId());
        dept.setAncestors(computeAncestors(cmd.parentId()));
        dept.setOrderNum(cmd.orderNum());
        dept.setLeader(cmd.leader());
        dept.setPhone(cmd.phone());
        dept.setEmail(cmd.email());
        dept.setStatus(cmd.status() == null ? 0 : cmd.status());
        dept.setIsDeleted(0);
        dept.setCreateUserId(operatorId);
        dept.setCreateDeptId(operatorDeptId);
        dept.setCreateTime(LocalDateTime.now());
        deptMapper.insert(dept);

        evictDeptTreeCache();
    }

    /**
     * Update an existing department. Recalculates ancestors if parentId changed,
     * propagates ancestor changes to all descendants, and evicts the tree cache.
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateDept(UpdateDeptCommand cmd, Long operatorId) {
        SysDept dept = deptMapper.selectById(cmd.id());
        if (dept == null || dept.getIsDeleted() == 1) {
            throw new BizException(ErrorCode.SYS_DEPT_NOT_FOUND, "部门不存在");
        }

        boolean parentChanged = cmd.parentId() != null
                && !cmd.parentId().equals(dept.getParentId());

        dept.setDeptName(cmd.deptName());
        if (cmd.parentId() != null) {
            dept.setParentId(cmd.parentId());
        }
        dept.setOrderNum(cmd.orderNum());
        dept.setLeader(cmd.leader());
        dept.setPhone(cmd.phone());
        dept.setEmail(cmd.email());
        if (cmd.status() != null) {
            dept.setStatus(cmd.status());
        }
        dept.setUpdateUserId(operatorId);
        dept.setUpdateTime(LocalDateTime.now());

        if (parentChanged) {
            String newAncestors = computeAncestors(cmd.parentId());
            String oldAncestors = dept.getAncestors();
            dept.setAncestors(newAncestors);
            deptMapper.updateById(dept);
            // Propagate ancestor change to all descendants
            updateDescendantAncestors(dept.getId(), oldAncestors, newAncestors);
        } else {
            deptMapper.updateById(dept);
        }

        evictDeptTreeCache();
    }

    /**
     * Soft-delete a department. Throws if the department still has active children.
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDept(Long id, Long operatorId) {
        SysDept dept = deptMapper.selectById(id);
        if (dept == null || dept.getIsDeleted() == 1) {
            throw new BizException(ErrorCode.SYS_DEPT_NOT_FOUND, "部门不存在");
        }

        Long childCount = deptMapper.selectCount(
                new LambdaQueryWrapper<SysDept>()
                        .eq(SysDept::getParentId, id)
                        .eq(SysDept::getIsDeleted, 0));
        if (childCount > 0) {
            throw new BizException(4112, "存在子部门，不能删除");
        }

        dept.setIsDeleted(1);
        dept.setUpdateUserId(operatorId);
        dept.setUpdateTime(LocalDateTime.now());
        deptMapper.updateById(dept);

        evictDeptTreeCache();
    }

    /**
     * Return just the dept name for a given id, or null if not found.
     */
    public String getDeptName(Long deptId) {
        if (deptId == null) {
            return null;
        }
        SysDept dept = deptMapper.selectById(deptId);
        if (dept == null || dept.getIsDeleted() == 1) {
            return null;
        }
        return dept.getDeptName();
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void evictDeptTreeCache() {
        redisTemplate.delete(DEPT_TREE_CACHE);
    }

    /**
     * Recursively build a DeptTreeDTO tree from a flat list, starting at parentId.
     */
    private List<DeptTreeDTO> buildTree(List<SysDept> depts, Long parentId) {
        List<DeptTreeDTO> result = new ArrayList<>();
        for (SysDept dept : depts) {
            Long pid = dept.getParentId() == null ? 0L : dept.getParentId();
            if (parentId.equals(pid)) {
                List<DeptTreeDTO> children = buildTree(depts, dept.getId());
                result.add(new DeptTreeDTO(
                        dept.getId(),
                        dept.getDeptName(),
                        dept.getDeptCode(),
                        dept.getParentId(),
                        dept.getOrderNum(),
                        dept.getStatus(),
                        children));
            }
        }
        return result;
    }

    /**
     * Compute the ancestors string for a new child of parentId.
     * If parentId is 0 or null returns "0"; otherwise parent.ancestors + "," + parentId.
     */
    private String computeAncestors(Long parentId) {
        if (parentId == null || parentId == 0L) {
            return "0";
        }
        SysDept parent = deptMapper.selectById(parentId);
        if (parent == null) {
            return "0";
        }
        String parentAncestors = parent.getAncestors();
        if (parentAncestors == null || parentAncestors.isBlank()) {
            return String.valueOf(parentId);
        }
        return parentAncestors + "," + parentId;
    }

    /**
     * After a parent change, update ancestors for all descendants of deptId.
     */
    private void updateDescendantAncestors(Long deptId, String oldAncestors, String newAncestors) {
        List<Long> descendantIds = deptMapper.selectDescendantIds(deptId);
        if (descendantIds == null || descendantIds.isEmpty()) {
            return;
        }
        for (Long descId : descendantIds) {
            SysDept desc = deptMapper.selectById(descId);
            if (desc == null) {
                continue;
            }
            // Replace the old ancestor prefix with the new one
            String updatedAncestors = desc.getAncestors() == null
                    ? newAncestors
                    : desc.getAncestors().replace(oldAncestors, newAncestors);
            desc.setAncestors(updatedAncestors);
            deptMapper.updateById(desc);
        }
    }

    /**
     * Map a SysDept entity to a flat DeptDTO (no children).
     */
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
