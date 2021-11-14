package com.agan.redis.config;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description: todo
 * @Author: jianweil
 * @date: 2021/11/2 9:55
 */
@NoArgsConstructor
@Data
public class aa {

    private Long dataTime;
    private List<VariableValueDTO> variableValue;
    private String energyName;
    private Long serviceTime;
    private String dsn;
    private Integer quality;
    private Boolean breakPointFlag;

    @NoArgsConstructor
    @Data
    public static class VariableValueDTO {
        private String name;
        private Integer value;
    }
}
