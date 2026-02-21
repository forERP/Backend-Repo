package com.forerp.erp.inventory.repository;

import com.forerp.erp.inventory.domain.RefType;
import com.forerp.erp.inventory.domain.InventoryHistory;
import com.forerp.erp.inventory.dto.InventoryLogListItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;

public interface InventoryHistoryRepository extends JpaRepository<InventoryHistory, Long> {

    @Query(
            value = """
                    select new com.forerp.erp.inventory.dto.InventoryLogListItemResponse(
                        h.id,
                        h.refType,
                        h.changeType,
                        h.changeQty,
                        h.beforeQty,
                        h.afterQty,
                        h.refId,
                        h.refItemId,
                        h.createdAt,
                        sp.id,
                        st.id,
                        st.name,
                        st.storeCode,
                        wh.id,
                        wh.name,
                        wh.code,
                        p.id,
                        p.sku,
                        p.name,
                        actor.id,
                        actor.name,
                        actor.employeeCode,
                        h.memo
                    )
                    from InventoryHistory h
                    join h.storeProduct sp
                    join sp.store st
                    join sp.warehouse wh
                    join sp.product p
                    left join h.actorUser actor
                    where h.refType in :eventTypes
                      and (:storeKeyword is null
                        or lower(st.name) like lower(concat('%', :storeKeyword, '%'))
                        or lower(st.storeCode) like lower(concat('%', :storeKeyword, '%')))
                      and (:warehouseKeyword is null
                        or lower(wh.name) like lower(concat('%', :warehouseKeyword, '%'))
                        or lower(wh.code) like lower(concat('%', :warehouseKeyword, '%')))
                      and (:productKeyword is null
                        or lower(p.name) like lower(concat('%', :productKeyword, '%'))
                        or lower(p.sku) like lower(concat('%', :productKeyword, '%')))
                      and (:actorKeyword is null
                        or (actor is not null and (
                            lower(actor.name) like lower(concat('%', :actorKeyword, '%'))
                            or lower(actor.loginId) like lower(concat('%', :actorKeyword, '%'))
                            or lower(actor.employeeCode) like lower(concat('%', :actorKeyword, '%'))
                        )))
                      and (:fromDateTime is null or h.createdAt >= :fromDateTime)
                      and (:toDateTime is null or h.createdAt < :toDateTime)
                    order by h.createdAt desc, h.id desc
                    """,
            countQuery = """
                    select count(h)
                    from InventoryHistory h
                    join h.storeProduct sp
                    join sp.store st
                    join sp.warehouse wh
                    join sp.product p
                    left join h.actorUser actor
                    where h.refType in :eventTypes
                      and (:storeKeyword is null
                        or lower(st.name) like lower(concat('%', :storeKeyword, '%'))
                        or lower(st.storeCode) like lower(concat('%', :storeKeyword, '%')))
                      and (:warehouseKeyword is null
                        or lower(wh.name) like lower(concat('%', :warehouseKeyword, '%'))
                        or lower(wh.code) like lower(concat('%', :warehouseKeyword, '%')))
                      and (:productKeyword is null
                        or lower(p.name) like lower(concat('%', :productKeyword, '%'))
                        or lower(p.sku) like lower(concat('%', :productKeyword, '%')))
                      and (:actorKeyword is null
                        or (actor is not null and (
                            lower(actor.name) like lower(concat('%', :actorKeyword, '%'))
                            or lower(actor.loginId) like lower(concat('%', :actorKeyword, '%'))
                            or lower(actor.employeeCode) like lower(concat('%', :actorKeyword, '%'))
                        )))
                      and (:fromDateTime is null or h.createdAt >= :fromDateTime)
                      and (:toDateTime is null or h.createdAt < :toDateTime)
                    """
    )
    Page<InventoryLogListItemResponse> searchLogs(
            @Param("eventTypes") Collection<RefType> eventTypes,
            @Param("storeKeyword") String storeKeyword,
            @Param("warehouseKeyword") String warehouseKeyword,
            @Param("productKeyword") String productKeyword,
            @Param("actorKeyword") String actorKeyword,
            @Param("fromDateTime") LocalDateTime fromDateTime,
            @Param("toDateTime") LocalDateTime toDateTime,
            Pageable pageable
    );
}
