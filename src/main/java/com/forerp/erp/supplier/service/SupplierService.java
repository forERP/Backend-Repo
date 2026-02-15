package com.forerp.erp.supplier.service;

import com.forerp.erp.supplier.domain.Supplier;
import com.forerp.erp.supplier.dto.SupplierCreateRequestDto;
import com.forerp.erp.supplier.dto.SupplierResponseDto;
import com.forerp.erp.supplier.dto.SupplierUpdateRequestDto;
import com.forerp.erp.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierResponseDto getSupplier(Long supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new IllegalArgumentException("거래처를 찾을 수 없습니다: " + supplierId));
        return SupplierResponseDto.from(supplier);
    }

    public List<SupplierResponseDto> getAllSuppliers() {
        return supplierRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(SupplierResponseDto::from)
                .toList();
    }

    public Page<SupplierResponseDto> searchSuppliers(
            String name,
            String contactName,
            String status,
            Pageable pageable
    ) {
        return supplierRepository.search(
                        normalize(name),
                        normalize(contactName),
                        parseActive(status),
                        pageable
                )
                .map(SupplierResponseDto::from);
    }

    @Transactional
    public SupplierResponseDto createSupplier(SupplierCreateRequestDto request) {
        Supplier supplier = Supplier.create(
                request.getName(),
                request.getContactName(),
                request.getContactPhone(),
                request.getContactEmail(),
                request.getAddress()
        );

        if (Boolean.FALSE.equals(request.getActive())) {
            supplier.update(null, request.getContactName(), request.getContactPhone(), request.getContactEmail(), request.getAddress(), false);
        }

        Supplier saved = supplierRepository.save(supplier);
        return SupplierResponseDto.from(saved);
    }

    @Transactional
    public SupplierResponseDto updateSupplier(Long supplierId, SupplierUpdateRequestDto request) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new IllegalArgumentException("거래처를 찾을 수 없습니다: " + supplierId));

        supplier.update(
                request.getName(),
                request.getContactName(),
                request.getContactPhone(),
                request.getContactEmail(),
                request.getAddress(),
                request.getActive()
        );

        return SupplierResponseDto.from(supplier);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Boolean parseActive(String status) {
        if (status == null || status.isBlank()) {
            return null;
        }

        return switch (status.trim().toUpperCase()) {
            case "ACTIVE" -> true;
            case "INACTIVE" -> false;
            default -> throw new IllegalArgumentException("Invalid supplier status: " + status);
        };
    }
}
