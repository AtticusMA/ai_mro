package com.mro.training.dubbo;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.common.request.UserContextDTO;
import com.mro.common.dubbo.training.request.*;
import com.mro.common.dubbo.training.response.*;
import com.mro.common.dubbo.training.request.*;
import com.mro.common.dubbo.training.response.*;
import com.mro.common.dubbo.training.service.TrainingDubboService;
import com.mro.training.service.*;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

import java.time.LocalDate;
import java.util.List;

@DubboService(version = "1.0.0")
@RequiredArgsConstructor
public class TrainingDubboServiceImpl implements TrainingDubboService {

    private final TrainingScenarioService scenarioService;
    private final TraineeService traineeService;
    private final TrainingSessionService sessionService;
    private final SkillAssessmentService assessmentService;

    @Override
    public PageResult<TrainingScenarioDTO> listScenarios(ScenarioQueryParam param) {
        return scenarioService.listScenarios(param);
    }

    @Override
    public Long createScenario(CreateScenarioCommand cmd) {
        return scenarioService.createScenario(cmd);
    }

    @Override
    public void updateScenario(UpdateScenarioCommand cmd) {
        scenarioService.updateScenario(cmd);
    }

    @Override
    public void publishScenario(Long scenarioId, Long operatorId) {
        scenarioService.publishScenario(scenarioId, operatorId);
    }

    @Override
    public PageResult<TraineeDTO> listTrainees(UserContextDTO ctx, int pageNum, int pageSize) {
        return traineeService.listTrainees(pageNum, pageSize);
    }

    @Override
    public TraineeProfileDTO getTraineeProfile(Long traineeId) {
        return traineeService.getTraineeProfile(traineeId);
    }

    @Override
    public List<Long> createSessions(CreateTrainingSessionCommand cmd) {
        return sessionService.createSessions(cmd);
    }

    @Override
    public PageResult<TrainingSessionDTO> listSessions(SessionQueryParam param) {
        return sessionService.listSessions(param);
    }

    @Override
    public TrainingSessionDTO getSession(Long sessionId) {
        return sessionService.getSession(sessionId);
    }

    @Override
    public List<SkillAssessmentDTO> getAssessments(Long sessionId) {
        return assessmentService.getAssessments(sessionId);
    }

    @Override
    public IndividualReportDTO generateIndividualReport(Long traineeId, LocalDate start, LocalDate end) {
        return assessmentService.generateIndividualReport(traineeId, start, end);
    }

    @Override
    public OverviewReportDTO generateOverviewReport(UserContextDTO ctx, LocalDate start, LocalDate end) {
        return assessmentService.generateOverviewReport(start, end);
    }
}
