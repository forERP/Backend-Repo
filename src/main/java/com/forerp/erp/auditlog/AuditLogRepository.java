package com.forerp.erp.auditlog;

import com.forerp.erp.auditlog.dto.AdminLogListItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long>{

    @Query(
            value = """
                    select new com.forerp.erp.auditlog.dto.AdminLogListItemResponse(
                        l.id,
                        a.id,
                        a.name,
                        a.loginId,
                        a.employeeCode,
                        a.role,
                        s.id,
                        s.name,
                        s.storeCode,
                        l.action,
                        l.targetType,
                        l.targetId,
                        l.createdAt
                    )
                    from AuditLog l
                    join l.actor a
                    left join a.store s
                    where (:actorKeyword is null
                        or lower(a.name) like lower(concat('%', :actorKeyword, '%'))
                        or lower(a.loginId) like lower(concat('%', :actorKeyword, '%'))
                        or lower(a.employeeCode) like lower(concat('%', :actorKeyword, '%')))
                      and (:action is null or l.action = :action)
                      and (:targetType is null or l.targetType = :targetType)
                      and (:fromDateTime is null or l.createdAt >= :fromDateTime)
                      and (:toDateTime is null or l.createdAt < :toDateTime)
                    order by l.createdAt desc, l.id desc
                    """,
            countQuery = """
                    select count(l)
                    from AuditLog l
                    join l.actor a
                    left join a.store s
                    where (:actorKeyword is null
                        or lower(a.name) like lower(concat('%', :actorKeyword, '%'))
                        or lower(a.loginId) like lower(concat('%', :actorKeyword, '%'))
                        or lower(a.employeeCode) like lower(concat('%', :actorKeyword, '%')))
                      and (:action is null or l.action = :action)
                      and (:targetType is null or l.targetType = :targetType)
                      and (:fromDateTime is null or l.createdAt >= :fromDateTime)
                      and (:toDateTime is null or l.createdAt < :toDateTime)
                    """
    )
    Page<AdminLogListItemResponse> search(
            @Param("actorKeyword") String actorKeyword,
            @Param("action") String action,
            @Param("targetType") String targetType,
            @Param("fromDateTime") LocalDateTime fromDateTime,
            @Param("toDateTime") LocalDateTime toDateTime,
            Pageable pageable
    );
}
