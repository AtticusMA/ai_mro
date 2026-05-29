package com.mro.common.dubbo.training.service;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.common.request.UserContextDTO;
import com.mro.common.dubbo.training.request.*;
import com.mro.common.dubbo.training.response.*;

import java.time.LocalDate;
import java.util.List;

public interface TrainingDubboService {

    PageResult<TrainingScenarioDTO> listScenarios(ScenarioQueryParam param);

    Long createScenario(CreateScenarioCommand cmd);

    void updateScenario(UpdateScenarioCommand cmd);

    void publishScenario(Long scenarioId, Long operatorId);

    PageResult<TraineeDTO> listTrainees(UserContextDTO ctx, int pageNum, int pageSize);

    TraineeProfileDTO getTraineeProfile(Long traineeId);

    List<Long> createSessions(CreateTrainingSessionCommand cmd);

    PageResult<TrainingSessionDTO> listSessions(SessionQueryParam param);

    TrainingSessionDTO getSession(Long sessionId);

    List<SkillAssessmentDTO> getAssessments(Long sessionId);

    IndividualReportDTO generateIndividualReport(Long traineeId, LocalDate start, LocalDate end);

    OverviewReportDTO generateOverviewReport(UserContextDTO ctx, LocalDate start, LocalDate end);
}
