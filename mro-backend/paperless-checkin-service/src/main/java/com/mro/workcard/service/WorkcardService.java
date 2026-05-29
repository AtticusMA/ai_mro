package com.mro.workcard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mro.common.core.exception.BizException;
import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.workcard.request.*;
import com.mro.common.dubbo.workcard.response.*;
import com.mro.workcard.context.DataScopeContext;
import com.mro.workcard.domain.entity.*;
import com.mro.workcard.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WorkcardService {

    private static final int ERR_WORKCARD_NOT_FOUND  = 4900;
    private static final int ERR_CARD_NO_EXISTS      = 4901;
    private static final int ERR_INVALID_STATUS      = 4902;
    private static final int ERR_STEP_NOT_FOUND      = 4903;
    private static final int ERR_NO_APPROVE_PERM     = 4908;

    private final WorkcardMapper workcardMapper;
    private final WorkcardStepMapper stepMapper;
    private final WorkcardSignatureMapper signatureMapper;
    private final ApprovalFlowMapper approvalFlowMapper;

    @Transactional
    public Long createWorkcard(CreateWorkcardCommand cmd) {
        String cardNo = generateCardNo();
        Workcard w = new Workcard();
        w.setCardNo(cardNo);
        w.setTitle(cmd.title());
        w.setCardType(cmd.cardType());
        w.setAircraftId(cmd.aircraftId());
        w.setPriority(cmd.priority());
        w.setStatus("draft");
        w.setCreatedBy(cmd.createdBy());
        w.setDueDate(cmd.dueDate());
        workcardMapper.insert(w);

        if (cmd.steps() != null) {
            for (CreateStepCommand sc : cmd.steps()) {
                WorkcardStep step = new WorkcardStep();
                step.setWorkcardId(w.getId());
                step.setStepNo(sc.stepNo());
                step.setDescription(sc.description());
                step.setRequiredTools(toJson(sc.requiredTools()));
                step.setRequiredMaterials(toJson(sc.requiredMaterials()));
                step.setManualRef(sc.manualRef());
                step.setStatus("pending");
                stepMapper.insert(step);
            }
        }
        return w.getId();
    }

    public PageResult<WorkcardDTO> listWorkcards(WorkcardQueryParam param) {
        LambdaQueryWrapper<Workcard> wrapper = new LambdaQueryWrapper<>();
        if (param.status() != null) wrapper.eq(Workcard::getStatus, param.status());
        if (param.cardType() != null) wrapper.eq(Workcard::getCardType, param.cardType());
        if (param.aircraftId() != null) wrapper.eq(Workcard::getAircraftId, param.aircraftId());
        if (param.priority() != null) wrapper.eq(Workcard::getPriority, param.priority());
        wrapper.orderByDesc(Workcard::getCreatedAt);

        // 数据权限过滤：selfOnly 按创建人；Workcard 暂无 deptId，部门范围暂不额外限制
        if (DataScopeContext.isSelfOnly()) {
            Long userId = DataScopeContext.getUserId();
            if (userId != null) {
                wrapper.eq(Workcard::getCreatedBy, userId);
            }
        }

        Page<Workcard> page = workcardMapper.selectPage(new Page<>(param.pageNum(), param.pageSize()), wrapper);
        List<WorkcardDTO> dtos = page.getRecords().stream().map(this::toListDTO).toList();
        return PageResult.of(dtos, page.getTotal(), param.pageNum(), param.pageSize());
    }

    public WorkcardDetailDTO getWorkcard(Long workcardId) {
        Workcard w = getOrThrow(workcardId);
        List<WorkcardStep> steps = stepMapper.selectList(
                new LambdaQueryWrapper<WorkcardStep>()
                        .eq(WorkcardStep::getWorkcardId, workcardId)
                        .orderByAsc(WorkcardStep::getStepNo));
        List<WorkcardStepDTO> stepDtos = steps.stream().map(this::toStepDTO).toList();
        return new WorkcardDetailDTO(w.getId(), w.getCardNo(), w.getTitle(),
                w.getCardType(), w.getAircraftId(), w.getPriority(), w.getStatus(), stepDtos);
    }

    @Transactional
    public void updateWorkcard(UpdateWorkcardCommand cmd) {
        Workcard w = getOrThrow(cmd.id());
        if (!"draft".equals(w.getStatus())) throw new BizException(ERR_INVALID_STATUS, "只有草稿状态可修改");
        w.setTitle(cmd.title());
        w.setCardType(cmd.cardType());
        w.setAircraftId(cmd.aircraftId());
        w.setPriority(cmd.priority());
        w.setDueDate(cmd.dueDate());
        workcardMapper.updateById(w);
    }

    @Transactional
    public void submitForApproval(Long workcardId, Long operatorId) {
        Workcard w = getOrThrow(workcardId);
        if (!"draft".equals(w.getStatus())) throw new BizException(ERR_INVALID_STATUS, "只有草稿状态可提交审批");
        w.setStatus("submitted");
        workcardMapper.updateById(w);

        ApprovalFlow flow = new ApprovalFlow();
        flow.setWorkcardId(workcardId);
        flow.setApproverId(operatorId);
        flow.setAction("submit");
        flow.setActedAt(Instant.now());
        approvalFlowMapper.insert(flow);
    }

    @Transactional
    public void approveWorkcard(Long workcardId, String action, String comment, Long operatorId) {
        Workcard w = getOrThrow(workcardId);
        if (!"submitted".equals(w.getStatus())) throw new BizException(ERR_INVALID_STATUS, "只有待审批状态可审批");

        switch (action) {
            case "approve" -> w.setStatus("approved");
            case "reject"  -> w.setStatus("draft");
            case "return"  -> w.setStatus("draft");
            default -> throw new BizException(ERR_INVALID_STATUS, "无效审批动作");
        }
        w.setApprovedBy(operatorId);
        workcardMapper.updateById(w);

        ApprovalFlow flow = new ApprovalFlow();
        flow.setWorkcardId(workcardId);
        flow.setApproverId(operatorId);
        flow.setAction(action);
        flow.setComment(comment);
        flow.setActedAt(Instant.now());
        approvalFlowMapper.insert(flow);
    }

    @Transactional
    public void issueWorkcard(Long workcardId, Long operatorId) {
        Workcard w = getOrThrow(workcardId);
        if (!"approved".equals(w.getStatus())) throw new BizException(ERR_INVALID_STATUS, "只有已审批状态可下发");
        w.setStatus("issued");
        workcardMapper.updateById(w);
    }

    @Transactional
    public void completeStep(Long workcardId, Long stepId, Long operatorId) {
        getOrThrow(workcardId);
        WorkcardStep step = stepMapper.selectOne(
                new LambdaQueryWrapper<WorkcardStep>()
                        .eq(WorkcardStep::getId, stepId)
                        .eq(WorkcardStep::getWorkcardId, workcardId));
        if (step == null) throw new BizException(ERR_STEP_NOT_FOUND, "步骤不存在");
        step.setStatus("completed");
        step.setCompletedBy(operatorId);
        step.setCompletedAt(Instant.now());
        stepMapper.updateById(step);

        // advance workcard to in_progress if still issued
        Workcard w = workcardMapper.selectById(workcardId);
        if ("issued".equals(w.getStatus())) {
            w.setStatus("in_progress");
            workcardMapper.updateById(w);
        }

        // check if all steps completed
        long total = stepMapper.countByWorkcard(workcardId);
        long completed = stepMapper.countCompletedByWorkcard(workcardId);
        if (total > 0 && total == completed) {
            w.setStatus("completed");
            workcardMapper.updateById(w);
        }
    }

    public WorkcardProgressDTO getProgress() {
        long total = workcardMapper.selectCount(null);
        long inProgress = workcardMapper.selectCount(
                new LambdaQueryWrapper<Workcard>().eq(Workcard::getStatus, "in_progress"));
        long completed = workcardMapper.selectCount(
                new LambdaQueryWrapper<Workcard>().eq(Workcard::getStatus, "completed"));
        long overdue = workcardMapper.selectCount(
                new LambdaQueryWrapper<Workcard>()
                        .in(Workcard::getStatus, List.of("issued", "in_progress"))
                        .lt(Workcard::getDueDate, Instant.now()));
        double rate = total > 0 ? Math.round((double) completed / total * 1000) / 10.0 : 0.0;
        return new WorkcardProgressDTO((int) total, (int) inProgress, (int) completed, (int) overdue, rate);
    }

    public PageResult<WorkcardAlertDTO> getAlerts(PageParam param) {
        Instant threshold = Instant.now().plusSeconds(24 * 3600);
        Instant now = Instant.now();

        LambdaQueryWrapper<Workcard> wrapper = new LambdaQueryWrapper<Workcard>()
                .in(Workcard::getStatus, List.of("issued", "in_progress"))
                .isNotNull(Workcard::getDueDate)
                .lt(Workcard::getDueDate, threshold)
                .orderByAsc(Workcard::getDueDate);

        Page<Workcard> page = workcardMapper.selectPage(
                new Page<>(param.pageNum(), param.pageSize()), wrapper);

        List<WorkcardAlertDTO> dtos = page.getRecords().stream().map(w -> {
            long total = stepMapper.countByWorkcard(w.getId());
            long comp = stepMapper.countCompletedByWorkcard(w.getId());
            int rate = total > 0 ? (int) Math.round((double) comp / total * 100) : 0;
            double hoursUntilDue = w.getDueDate() != null
                    ? (w.getDueDate().toEpochMilli() - now.toEpochMilli()) / 3_600_000.0
                    : 0.0;
            List<String> assignees = workcardMapper.selectAssigneeNamesByWorkcard(w.getId());
            return new WorkcardAlertDTO(w.getId(), w.getCardNo(), w.getTitle(),
                    w.getDueDate(), Math.round(hoursUntilDue * 10) / 10.0, rate, assignees);
        }).toList();
        return PageResult.of(dtos, page.getTotal(), param.pageNum(), param.pageSize());
    }

    public List<SignatureDTO> getSignatures(Long workcardId) {
        getOrThrow(workcardId);
        List<WorkcardSignature> sigs = signatureMapper.selectList(
                new LambdaQueryWrapper<WorkcardSignature>()
                        .eq(WorkcardSignature::getWorkcardId, workcardId)
                        .orderByAsc(WorkcardSignature::getSignedAt));
        return sigs.stream().map(s -> new SignatureDTO(
                s.getId(), s.getWorkcardId(), s.getStepId(),
                s.getSignerId(), null, s.getSignatureType(),
                s.getBlockchainHash(), s.getSignedAt()
        )).toList();
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    public Workcard getOrThrow(Long id) {
        Workcard w = workcardMapper.selectById(id);
        if (w == null) throw new BizException(ERR_WORKCARD_NOT_FOUND, "工卡不存在");
        return w;
    }

    private WorkcardDTO toListDTO(Workcard w) {
        long total = stepMapper.countByWorkcard(w.getId());
        long comp = stepMapper.countCompletedByWorkcard(w.getId());
        int rate = total > 0 ? (int) Math.round((double) comp / total * 100) : 0;
        return new WorkcardDTO(w.getId(), w.getCardNo(), w.getTitle(),
                w.getCardType(), w.getAircraftId(), w.getPriority(), w.getStatus(),
                null, w.getDueDate(), rate);
    }

    @SuppressWarnings("unchecked")
    private WorkcardStepDTO toStepDTO(WorkcardStep s) {
        return new WorkcardStepDTO(s.getId(), s.getStepNo(), s.getDescription(),
                parseJson(s.getRequiredTools()), parseJson(s.getRequiredMaterials()),
                s.getManualRef(), s.getStatus(), null, s.getCompletedAt());
    }

    private String generateCardNo() {
        String prefix = "WC-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        long count = workcardMapper.selectCount(
                new LambdaQueryWrapper<Workcard>().likeRight(Workcard::getCardNo, prefix));
        return String.format("%s-%03d", prefix, count + 1);
    }

    private String toJson(List<Map<String, String>> list) {
        if (list == null || list.isEmpty()) return "[]";
        try {
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            return om.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> parseJson(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
            return om.readValue(json, List.class);
        } catch (Exception e) {
            return List.of();
        }
    }
}
