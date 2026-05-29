package com.mro.web.module.ar.app;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.common.request.*;
import com.mro.common.dubbo.ar.request.*;
import com.mro.common.dubbo.ar.response.*;
import com.mro.common.dubbo.ar.service.ArDubboService;
import com.mro.web.annotation.DataScope;
import com.mro.web.context.UserContext;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArAppService {

    @DubboReference(version = "1.0.0", timeout = 5000, retries = 0)
    private ArDubboService arDubboService;

    @DataScope
    public PageResult<InspectionTaskDTO> listInspections(InspectionQueryParam param) {
        return arDubboService.listInspections(param, buildCtx());
    }

    public Long createInspection(CreateInspectionCommand cmd) {
        return arDubboService.createInspection(cmd);
    }

    public void startInspection(Long taskId) {
        arDubboService.startInspection(taskId, UserContext.getUserId());
    }

    public void completeInspection(Long taskId) {
        arDubboService.completeInspection(taskId, UserContext.getUserId());
    }

    public PageResult<AnomalyRecordDTO> listAnomalies(Long taskId, int pageNum, int pageSize) {
        return arDubboService.listAnomalies(taskId, new HealthPageParam(pageNum, pageSize));
    }

    public ArSessionDTO createSession(CreateSessionCommand cmd) {
        return arDubboService.createSession(cmd);
    }

    public void joinSession(Long sessionId) {
        arDubboService.joinSession(sessionId, UserContext.getUserId());
    }

    public void endSession(Long sessionId) {
        arDubboService.endSession(sessionId, UserContext.getUserId());
    }

    public void sendAnnotation(Long sessionId, AnnotationDTO annotation) {
        arDubboService.sendAnnotation(sessionId, annotation);
    }

    @DataScope
    public PageResult<VideoArchiveDTO> listArchives(ArchiveQueryParam param) {
        return arDubboService.listArchives(param, buildCtx());
    }

    private UserContextDTO buildCtx() {
        return new UserContextDTO(UserContext.getUserId(), UserContext.getDeptId(),
                UserContext.getRoles(), UserContext.getPermissions());
    }
}
