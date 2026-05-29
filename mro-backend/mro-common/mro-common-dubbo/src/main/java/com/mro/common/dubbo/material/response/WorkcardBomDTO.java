package com.mro.common.dubbo.material.response;
import com.mro.common.dubbo.material.request.MaterialRequestItemDTO;
import java.io.Serializable;
import java.util.List;
public record WorkcardBomDTO(List<MaterialRequestItemDTO> list) implements Serializable {}
