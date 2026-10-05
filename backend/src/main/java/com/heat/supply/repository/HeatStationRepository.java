package com.heat.supply.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.heat.supply.domain.HeatStation;
import com.heat.supply.dto.StationTempStatDTO;

public interface HeatStationRepository extends JpaRepository<HeatStation, Long> {

    Optional<HeatStation> findByCode(String code);

    List<HeatStation> findByHeatSourceIdOrderByCodeAsc(Long heatSourceId);

    @Query("select st.id from HeatStation st order by st.code asc")
    List<Long> findIdsOrderByCode();

    long countByStatus(String status);

    long countByHeatSourceId(Long heatSourceId);

    /**
     * 手写 JPQL：按换热站统计室温抽测。
     * 抽测次数 = 该站全部历史抽测条数（历史记录完整保留）；
     * 不达标户数只认每户「最近一次」抽测结论——最近一条 = 业务日期最大、同日再取录入 id 最大，
     * 与热力格汇总同一口径，较早（即使更低温）的记录不计入当前不达标户数。
     */
    @Query("select new com.heat.supply.dto.StationTempStatDTO("
            + "st.id, st.code, st.name, count(rt.id), "
            + "sum(case when rt is null or r.merged = true then 0 "
            + "when rt.id = (select max(z.id) from RoomTempSample z where z.resident.id = r.id "
            + "and z.sampleDate = (select max(y.sampleDate) from RoomTempSample y where y.resident.id = r.id)) "
            + "and rt.qualified = false then 1 else 0 end), "
            + "count(distinct r.id)) "
            + "from HeatStation st left join st.residents r left join r.samples rt "
            + "group by st.id, st.code, st.name "
            + "order by st.code asc")
    List<StationTempStatDTO> tempStatGroupByStation();
}
