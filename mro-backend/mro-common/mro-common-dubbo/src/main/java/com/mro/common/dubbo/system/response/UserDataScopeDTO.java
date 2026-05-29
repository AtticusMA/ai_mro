package com.mro.common.dubbo.system.response;

import java.io.Serializable;
import java.util.List;

/**
 * 用户数据权限信息，供 manage-web AOP 计算可见 deptId 列表。
 *
 * @param dataScope   有效数据范围（取用户所有角色中最宽松的值，1=全部…5=自定义）
 * @param customDeptIds dataScope=5 时的自定义部门列表，其他情况为空
 */
public record UserDataScopeDTO(
        Integer dataScope,
        List<Long> customDeptIds
) implements Serializable {
}
