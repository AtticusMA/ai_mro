package com.mro.workcard.service;

import com.mro.common.core.exception.BizException;
import com.mro.common.dubbo.workcard.response.SignatureResultDTO;
import com.mro.common.dubbo.workcard.request.SignWorkcardCommand;
import com.mro.common.dubbo.workcard.response.BlockchainVerifyDTO;
import com.mro.common.dubbo.workcard.response.SignatureVerifyDTO;
import com.mro.workcard.domain.entity.WorkcardSignature;
import com.mro.workcard.domain.entity.WorkcardStep;
import com.mro.workcard.mapper.WorkcardSignatureMapper;
import com.mro.workcard.mapper.WorkcardStepMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SignatureService {

    private static final int ERR_STEP_NOT_FOUND     = 4903;
    private static final int ERR_STEP_NOT_COMPLETED = 4904;
    private static final int ERR_NO_QUALIFICATION   = 4905;
    private static final int ERR_SIG_INVALID        = 4906;

    private final WorkcardSignatureMapper signatureMapper;
    private final WorkcardStepMapper stepMapper;

    @Transactional
    public SignatureResultDTO signWorkcard(SignWorkcardCommand cmd) {
        if (cmd.digitalSignature() == null || cmd.digitalSignature().isBlank())
            throw new BizException(ERR_SIG_INVALID, "数字签名不能为空");

        if (cmd.stepId() != null) {
            WorkcardStep step = stepMapper.selectOne(
                    new LambdaQueryWrapper<WorkcardStep>()
                            .eq(WorkcardStep::getId, cmd.stepId())
                            .eq(WorkcardStep::getWorkcardId, cmd.workcardId()));
            if (step == null) throw new BizException(ERR_STEP_NOT_FOUND, "步骤不存在");
            if (!"completed".equals(step.getStatus()))
                throw new BizException(ERR_STEP_NOT_COMPLETED, "步骤未完成，不允许签署");
        }

        Instant now = Instant.now();
        String rawData = cmd.workcardId() + "|" + cmd.stepId() + "|" + cmd.signerId()
                + "|" + cmd.digitalSignature() + "|" + now.toEpochMilli();
        String hash = sm3Hash(rawData);

        WorkcardSignature sig = new WorkcardSignature();
        sig.setWorkcardId(cmd.workcardId());
        sig.setStepId(cmd.stepId());
        sig.setSignerId(cmd.signerId());
        sig.setSignatureType(cmd.signatureType());
        sig.setDigitalSignature(cmd.digitalSignature());
        sig.setBlockchainHash(hash);
        sig.setSignedAt(now);
        signatureMapper.insert(sig);

        return new SignatureResultDTO(sig.getId(), hash, now);
    }

    public BlockchainVerifyDTO verifyBlockchain(Long workcardId) {
        List<WorkcardSignature> sigs = signatureMapper.selectList(
                new LambdaQueryWrapper<WorkcardSignature>()
                        .eq(WorkcardSignature::getWorkcardId, workcardId)
                        .orderByAsc(WorkcardSignature::getSignedAt));

        boolean allValid = true;
        List<SignatureVerifyDTO> results = sigs.stream().map(s -> {
            String rawData = s.getWorkcardId() + "|" + s.getStepId() + "|" + s.getSignerId()
                    + "|" + s.getDigitalSignature() + "|" + s.getSignedAt().toEpochMilli();
            String expected = sm3Hash(rawData);
            boolean tampered = !expected.equals(s.getBlockchainHash());
            return new SignatureVerifyDTO(s.getId(), null, s.getSignatureType(),
                    s.getBlockchainHash(), s.getSignedAt(), tampered);
        }).toList();

        boolean verified = results.stream().noneMatch(SignatureVerifyDTO::tampered);
        return new BlockchainVerifyDTO(workcardId, verified, results);
    }

    // SM3 simulation: use SHA-256 prefixed with "SM3:" until native SM3 lib is available
    private String sm3Hash(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(data.getBytes(StandardCharsets.UTF_8));
            return "SM3:" + HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new BizException(ERR_SIG_INVALID, "哈希计算失败");
        }
    }
}
