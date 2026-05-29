package com.mro.web.module.training.app;

import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.training.request.*;
import com.mro.common.dubbo.training.response.*;
import com.mro.common.dubbo.training.request.*;
import com.mro.common.dubbo.training.response.*;
import com.mro.common.dubbo.training.service.TrainingDubboService;
import com.mro.web.context.UserContext;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrainingAppService {

    @DubboReference(version = "1.0.0")
    private TrainingDubboService trainingDubboService;

    public PageResult<TrainingScenarioDTO> listScenarios(String status, String category, int pageNum, int pageSize) {
        return trainingDubboService.listScenarios(
                new ScenarioQueryParam(status, category, pageNum, pageSize));
    }

    public Long createScenario(CreateScenarioCommand cmd) {
        CreateScenarioCommand cmdWithUser = new CreateScenarioCommand(
                cmd.name(), cmd.category(), cmd.difficulty(),
                cmd.modelUrl(), cmd.durationMinutes(), UserContext.getUserId());
        return trainingDubboService.createScenario(cmdWithUser);
    }

    public void updateScenario(Long id, UpdateScenarioCommand cmd) {
        UpdateScenarioCommand cmdWithUser = new UpdateScenarioCommand(
                id, cmd.name(), cmd.category(), cmd.difficulty(),
                cmd.modelUrl(), cmd.durationMinutes(), UserContext.getUserId());
        trainingDubboService.updateScenario(cmdWithUser);
    }

    public void publishScenario(Long scenarioId) {
        trainingDubboService.publishScenario(scenarioId, UserContext.getUserId());
    }

    public PageResult<TraineeDTO> listTrainees(int pageNum, int pageSize) {
        return trainingDubboService.listTrainees(UserContext.get(), pageNum, pageSize);
    }

    public TraineeProfileDTO getTraineeProfile(Long traineeId) {
        return trainingDubboService.getTraineeProfile(traineeId);
    }

    public List<Long> createSessions(CreateTrainingSessionCommand cmd) {
        CreateTrainingSessionCommand cmdWithUser = new CreateTrainingSessionCommand(
                cmd.scenarioId(), cmd.traineeIds(), cmd.mode(),
                cmd.scheduledAt(), UserContext.getUserId());
        return trainingDubboService.createSessions(cmdWithUser);
    }

    public PageResult<TrainingSessionDTO> listSessions(Long scenarioId, Long traineeId, String status, String mode, int pageNum, int pageSize) {
        return trainingDubboService.listSessions(
                new SessionQueryParam(scenarioId, traineeId, status, mode, pageNum, pageSize));
    }

    public TrainingSessionDTO getSession(Long sessionId) {
        return trainingDubboService.getSession(sessionId);
    }

    public List<SkillAssessmentDTO> getAssessments(Long sessionId) {
        return trainingDubboService.getAssessments(sessionId);
    }

    public IndividualReportDTO generateIndividualReport(Long traineeId, LocalDate start, LocalDate end) {
        return trainingDubboService.generateIndividualReport(traineeId, start, end);
    }

    public OverviewReportDTO generateOverviewReport(LocalDate start, LocalDate end) {
        return trainingDubboService.generateOverviewReport(UserContext.get(), start, end);
    }
}
