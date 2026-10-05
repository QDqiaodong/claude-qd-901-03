package com.heat.supply.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.heat.supply.domain.RoomTempSample;
import com.heat.supply.dto.LatestTempDTO;

public interface RoomTempSampleRepository extends JpaRepository<RoomTempSample, Long> {

    /**
     * 同户按抽测日期倒序、同日再按录入先后（自增 id 倒序）排列，保证同日多条记录顺序确定。
     */
    List<RoomTempSample> findByResidentIdOrderBySampleDateDescIdDesc(Long residentId);

    @Query("select v.id from RoomTempSample v order by v.sampleDate desc, v.id desc")
    List<Long> findIdsOrderBySampleDateDesc();

    /**
     * 手写 JPQL：每户唯一一条「最近一次」抽测结果（热力格染色 / 不达标统计 / 报修引用统一口径）。
     *
     * 规则必须是确定的：先按业务日期取最大，业务日期相同（两人先后或近同时为同户补录同日抽测）
     * 再按录入先后取最大——录入先后以自增主键 id 表示，id 仅在落库时分配，提交失败/回滚的记录
     * 拿不到 id，更不可能短暂地成为「最新」。绝不依赖数据库未声明的返回顺序，
     * 也不通过覆盖/删除/限制每日一条来消歧；历史抽测原样保留。
     * 跨日期补录时（先录今天、后补录昨天），业务日期优先，昨天的记录不会误成为当前结果。
     */
    @Query("select new com.heat.supply.dto.LatestTempDTO("
            + "rt.id, rt.resident.id, rt.sampleDate, rt.temperature, rt.qualified, rt.householdName) "
            + "from RoomTempSample rt join rt.resident r "
            + "where r.merged = false and rt.id = ("
            + "select max(z.id) from RoomTempSample z "
            + "where z.resident.id = rt.resident.id "
            + "and z.sampleDate = ("
            + "select max(y.sampleDate) from RoomTempSample y where y.resident.id = rt.resident.id))")
    List<LatestTempDTO> findLatestPerResident();

    /** 归并：抽测记录改挂（抽测日期、室温、结论等一律不动），批量更新只改关联外键 */
    @Modifying
    @Query("update RoomTempSample s set s.resident = :keeper where s.resident = :merged")
    int reassignResident(@Param("keeper") com.heat.supply.domain.Resident keeper,
                         @Param("merged") com.heat.supply.domain.Resident merged);

    long countByResidentId(Long residentId);
}
