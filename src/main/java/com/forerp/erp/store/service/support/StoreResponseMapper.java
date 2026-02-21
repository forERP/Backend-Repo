package com.forerp.erp.store.service.support;

import com.forerp.erp.store.domain.Store;
import com.forerp.erp.store.dto.StoreDto;
import org.springframework.stereotype.Component;

@Component
public class StoreResponseMapper {

    public StoreDto.Response toDto(Store store){
        return new StoreDto.Response(
                store.getId(),
                store.getStoreCode(),
                store.getName(),
                store.getPhone(),
                store.getAddress(),
                store.getLatitude(),
                store.getLongitude(),
                store.getStoreType(),
                store.getStatus(),
                store.getCreatedAt()
        );
    }
}
