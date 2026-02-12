package com.forerp.erp.store.service;

import com.forerp.erp.auditlog.AuditLogService;
import com.forerp.erp.common.jwt.SecurityUtil;
import com.forerp.erp.store.domain.Store;
import com.forerp.erp.store.domain.StoreStatus;
import com.forerp.erp.store.domain.StoreType;
import com.forerp.erp.store.dto.StoreDto;
import com.forerp.erp.store.repository.StoreRepository;
import com.forerp.erp.store.service.support.StoreReader;
import com.forerp.erp.store.service.support.StoreResponseMapper;
import com.forerp.erp.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;
    private final StoreReader storeReader;
    private final StoreResponseMapper storeResponseMapper;

    private final AuditLogService auditLogService;
    private final SecurityUtil securityUtil;

    // 매장 생성
    @Transactional
    public StoreDto.Response createStore(StoreDto.CreateRequest request){

        String generatedCode;
        Optional<Store> last = storeRepository.findTopByStoreCodeNotOrderByStoreCodeDesc("000");
        if (last.isPresent()) {
            try {
                int lastNum = Integer.parseInt(last.get().getStoreCode());
                generatedCode = String.format("%03d", lastNum + 1);
            } catch (NumberFormatException e) {
                generatedCode = String.format("%03d", storeReader.countAll() + 1);
            }
        } else {
            // No non-HQ stores yet => first store after HQ should be 001
            generatedCode = "001";
        }

        StoreType type = (request.getType() == null) ? StoreType.STORE : request.getType();

        Store store = Store.builder()
            .name(request.getName())
            .storeCode(generatedCode)
            .storeType(type)
            .status(StoreStatus.OPEN)
            .phone(request.getPhone())
            .address(request.getAddress())
            .build();

        Store saved = storeRepository.save(store);
        logAction("CREATE_STORE", saved.getId());

        return storeResponseMapper.toDto(saved);
    }

    // 매장 상태 변경
    @Transactional
    public StoreDto.Response updateStoreStatus(Long id, StoreDto.UpdateStatusRequest request){
        Store store = storeReader.getStore(id);

        if(request.getStatus() == StoreStatus.CLOSED){
            store.close();
        }else if(request.getStatus() == StoreStatus.INACTIVE){
            store.deactivate();
        }else if(request.getStatus() == StoreStatus.OPEN){
            store.open();
        }

        logAction("UPDATE_STORE_STATUS", store.getId());
        return storeResponseMapper.toDto(store);
    }

    // 매장 정보 수정
    @Transactional
    public StoreDto.Response updateStore(Long id, StoreDto.UpdateRequest request){
        Store store = storeReader.getStore(id);
        store.updateInfo(request.getName(), request.getPhone(), request.getAddress());
        
        logAction("UPDATE_STORE_INFO", store.getId());
        return storeResponseMapper.toDto(store);
    }

    // 전체 매장 조회
    public List<StoreDto.Response> getAllStores(){
        return storeReader.getAllStores().stream()
                .map(storeResponseMapper::toDto)
                .collect(Collectors.toList());
    }

    // 단건 조회
    public StoreDto.Response getStore(Long id){
        return storeResponseMapper.toDto(storeReader.getStore(id));
    }

    private void logAction(String action, Long targetId){
        try {
            User actor = securityUtil.getCurrentUser();
            auditLogService.logAction(actor, action, "STORE", targetId);
        }catch (Exception e){

        }
    }
}
