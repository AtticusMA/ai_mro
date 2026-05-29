package com.mro.ar.dubbo;

import com.mro.ar.service.AnomalyService;
import com.mro.ar.service.InspectionService;
import com.mro.ar.service.SessionService;
import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.common.request.*;
import com.mro.common.dubbo.ar.request.*;
import com.mro.common.dubbo.ar.response.*;
import com.mro.common.dubbo.ar.request.*;
import com.mro.common.dubbo.ar.response.*;
import com.mro.common.dubbo.ar.service.ArDubboService;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService(version = "1.0.0")
@RequiredArgsConstructor
public class ArDubboServiceImpl implements ArDubboService {

    private final InspectionService inspectionService;
    private final AnomalyService anomalyService;
    private final SessionService sessionService;

    @Override
    public PageResult<InspectionTaskDTO> listInspections(InspectionQueryParam param, UserContextDTO ctx) {
        return inspectionService.listInspections(param, ctx);
    }

    @Override
    public Long createInspection(CreateInspectionCommand cmd) {
        return inspectionService.createInspection(cmd);
    }

    @Override
    public void startInspection(Long taskId, Long operatorId) {
        inspectionService.startInspection(taskId, operatorId);
    }

    @Override
    public void completeInspection(Long taskId, Long operatorId) {
        inspectionService.completeInspection(taskId, operatorId);
    }

    @Override
    public PageResult<AnomalyRecordDTO> listAnomalies(Long taskId, HealthPageParam param) {
        return anomalyService.listAnomalies(taskId, param);
    }

    @Override
    public ArSessionDTO createSession(CreateSessionCommand cmd) {
        return sessionService.createSession(cmd);
    }

    @Override
    public void joinSession(Long sessionId, Long expertId) {
        sessionService.joinSession(sessionId, expertId);
    }

    @Override
    public void endSession(Long sessionId, Long operatorId) {
        sessionService.endSession(sessionId, operatorId);
    }

    @Override
    public void sendAnnotation(Long sessionId, AnnotationDTO annotation) {
        // Annotation relay is handled by ArSignalingHandler over WebSocket;
        // this Dubbo method persists the annotation event if needed.
        sessionService.getSessionOrThrow(sessionId);
    }

    @Override
    public PageResult<VideoArchiveDTO> listArchives(ArchiveQueryParam param, UserContextDTO ctx) {
        return sessionService.listArchives(param, ctx);
    }
}
