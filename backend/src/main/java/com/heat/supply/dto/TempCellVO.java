package com.heat.supply.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

/** 室温热力格子：楼栋 × 单元 */
@Data
@NoArgsConstructor
public class TempCellVO {

    private String buildingNo;
    private String unitNo;
    private List<Long> residentIds;
    private Integer householdCount;
    private Integer sampleCount;
    /** 该格子最近一次抽测室温 */
    private BigDecimal latestTemp;
    private LocalDate latestDate;
    private Boolean qualified;
    private Boolean overheated;
    private Integer unqualifiedCount;
    private Long latestSampleId;
    private List<String> households;
    /** 格内最近一次抽测不达标住户的抽测 id（一键报修逐户引用，保证与最新记录一致） */
    private List<Long> unqualifiedSampleIds;

    public TempCellVO(String buildingNo, String unitNo, List<Long> residentIds, Integer householdCount,
                      Integer sampleCount, BigDecimal latestTemp, LocalDate latestDate, Boolean qualified,
                      Boolean overheated, Integer unqualifiedCount, Long latestSampleId, List<String> households,
                      List<Long> unqualifiedSampleIds) {
        this.buildingNo = buildingNo;
        this.unitNo = unitNo;
        this.residentIds = residentIds;
        this.householdCount = householdCount;
        this.sampleCount = sampleCount;
        this.latestTemp = latestTemp;
        this.latestDate = latestDate;
        this.qualified = qualified;
        this.overheated = overheated;
        this.unqualifiedCount = unqualifiedCount;
        this.latestSampleId = latestSampleId;
        this.households = households;
        this.unqualifiedSampleIds = unqualifiedSampleIds;
    }
}
