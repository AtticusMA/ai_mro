package com.mro.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mro.common.core.constant.ErrorCode;
import com.mro.common.core.exception.BizException;
import com.mro.common.core.response.PageResult;
import com.mro.common.dubbo.system.request.CreateDictCommand;
import com.mro.common.dubbo.system.response.DictDTO;
import com.mro.common.dubbo.system.response.DictItemDTO;
import com.mro.common.dubbo.system.request.DictQueryParam;
import com.mro.common.dubbo.system.request.UpdateDictCommand;
import com.mro.system.entity.SysDict;
import com.mro.system.mapper.SysDictMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class DictService {

    private static final Logger log = LoggerFactory.getLogger(DictService.class);
    private static final String DICT_CACHE_PREFIX = "sys:dict:";
    private static final long DICT_CACHE_TTL_HOURS = 1L;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired private SysDictMapper dictMapper;
    @Autowired private StringRedisTemplate redisTemplate;
    @Autowired private ObjectMapper objectMapper;

    public PageResult<DictDTO> listDicts(DictQueryParam param) {
        Page<SysDict> page = new Page<>(param.pageNum(), param.pageSize());
        LambdaQueryWrapper<SysDict> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(param.dictGroup())) {
            wrapper.eq(SysDict::getDictGroup, param.dictGroup());
        }
        if (StringUtils.hasText(param.keyword())) {
            wrapper.and(w -> w.like(SysDict::getDictCode, param.keyword())
                    .or().like(SysDict::getDictLabel, param.keyword()));
        }
        if (param.status() != null) {
            wrapper.eq(SysDict::getStatus, param.status());
        }
        Page<SysDict> result = dictMapper.selectPage(page, wrapper);
        List<DictDTO> records = result.getRecords().stream()
                .map(this::toDictDTO).collect(Collectors.toList());
        return new PageResult<>(records, result.getTotal(), param.pageNum(), param.pageSize());
    }

    public List<DictItemDTO> getDictByGroup(String dictGroup) {
        String cacheKey = DICT_CACHE_PREFIX + dictGroup;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, new TypeReference<List<DictItemDTO>>() {});
            } catch (Exception e) {
                log.warn("Failed to deserialize dict cache for group={}", dictGroup, e);
            }
        }
        List<DictItemDTO> items = dictMapper.selectItemsByGroup(dictGroup);
        try {
            String json = objectMapper.writeValueAsString(items);
            redisTemplate.opsForValue().set(cacheKey, json, DICT_CACHE_TTL_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("Failed to cache dict items for group={}", dictGroup, e);
        }
        return items;
    }

    public DictDTO getDictById(Long id) {
        SysDict dict = dictMapper.selectById(id);
        if (dict == null) {
            throw new BizException(ErrorCode.SYS_DICT_NOT_FOUND, "字典不存在");
        }
        return toDictDTO(dict);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long createDict(CreateDictCommand cmd, Long operatorId) {
        Long count = dictMapper.selectCount(new LambdaQueryWrapper<SysDict>()
                .eq(SysDict::getDictGroup, cmd.dictGroup())
                .eq(SysDict::getDictCode, cmd.dictCode()));
        if (count > 0) {
            throw new BizException(ErrorCode.SYS_DICT_CODE_DUPLICATE, "字典编码已存在");
        }
        SysDict dict = new SysDict();
        dict.setDictGroup(cmd.dictGroup());
        dict.setDictCode(cmd.dictCode());
        dict.setDictLabel(cmd.dictLabel());
        dict.setStatus(cmd.status() == null ? 0 : cmd.status());
        dict.setRemark(cmd.remark());
        dict.setCreateUserId(operatorId);
        dict.setCreateTime(LocalDateTime.now());
        dictMapper.insert(dict);
        evictDictCache(cmd.dictGroup());
        return dict.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateDict(UpdateDictCommand cmd, Long operatorId) {
        SysDict dict = dictMapper.selectById(cmd.id());
        if (dict == null) {
            throw new BizException(ErrorCode.SYS_DICT_NOT_FOUND, "字典不存在");
        }
        String oldGroup = dict.getDictGroup();
        if (cmd.dictLabel() != null) dict.setDictLabel(cmd.dictLabel());
        if (cmd.status() != null) dict.setStatus(cmd.status());
        if (cmd.remark() != null) dict.setRemark(cmd.remark());
        dict.setUpdateUserId(operatorId);
        dict.setUpdateTime(LocalDateTime.now());
        dictMapper.updateById(dict);
        evictDictCache(oldGroup);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteDict(Long id, Long operatorId) {
        SysDict dict = dictMapper.selectById(id);
        if (dict == null) {
            throw new BizException(ErrorCode.SYS_DICT_NOT_FOUND, "字典不存在");
        }
        String dictGroup = dict.getDictGroup();
        dictMapper.deleteById(id);
        evictDictCache(dictGroup);
    }

    private void evictDictCache(String dictGroup) {
        if (StringUtils.hasText(dictGroup)) {
            redisTemplate.delete(DICT_CACHE_PREFIX + dictGroup);
        }
    }

    private DictDTO toDictDTO(SysDict dict) {
        String ct = dict.getCreateTime() != null ? dict.getCreateTime().format(DATE_FMT) : null;
        return new DictDTO(dict.getId(), dict.getDictGroup(), dict.getDictCode(),
                dict.getDictLabel(), dict.getStatus(), dict.getRemark(), ct);
    }
}
