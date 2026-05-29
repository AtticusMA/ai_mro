package com.mro.common.dubbo.ar.service;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.common.request.HealthPageParam;
import com.mro.common.dubbo.common.request.UserContextDTO;
import com.mro.common.dubbo.ar.request.*;
import com.mro.common.dubbo.ar.response.*;

public interface ArDubboService {

    PageResult<InspectionTaskDTO> listInspections(InspectionQueryParam param, UserContextDTO ctx);

    Long createInspection(CreateInspectionCommand cmd);

    void startInspection(Long taskId, Long operatorId);

    void completeInspection(Long taskId, Long operatorId);

    PageResult<AnomalyRecordDTO> listAnomalies(Long taskId, HealthPageParam param);

    ArSessionDTO createSession(CreateSessionCommand cmd);

    void joinSession(Long sessionId, Long expertId);

    void endSession(Long sessionId, Long operatorId);

    void sendAnnotation(Long sessionId, AnnotationDTO annotation);

    PageResult<VideoArchiveDTO> listArchives(ArchiveQueryParam param, UserContextDTO ctx);
}
