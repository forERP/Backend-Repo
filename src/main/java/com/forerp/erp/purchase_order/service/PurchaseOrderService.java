package com.forerp.erp.purchase_order.service;

import com.forerp.erp.common.query.QueryParamParser;
import com.forerp.erp.inbound.dto.InboundCreateRequest;
import com.forerp.erp.inbound.repository.InboundRepository;
import com.forerp.erp.inbound.service.InboundService;
import com.forerp.erp.purchase_order.domain.PurchaseOrder;
import com.forerp.erp.purchase_order.domain.PurchaseOrderItem;
import com.forerp.erp.purchase_order.domain.PurchaseOrderStatus;
import com.forerp.erp.purchase_order.dto.PurchaseOrderDraftUpdateRequest;
import com.forerp.erp.purchase_order.dto.PurchaseOrderListResponse;
import com.forerp.erp.purchase_order.repository.PurchaseOrderRepository;
import com.forerp.erp.purchase_req.domain.PurchaseRequestStatus;
import com.forerp.erp.supplier.domain.Supplier;
import com.forerp.erp.supplier.repository.SupplierRepository;
import com.forerp.erp.warehouse.domain.Warehouse;
import com.forerp.erp.warehouse.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final InboundRepository inboundRepository;
    private final InboundService inboundService;
    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;

    @Transactional(readOnly = true)
    public PurchaseOrder get(Long purchaseOrderId) {
        return purchaseOrderRepository.findDetailById(purchaseOrderId)
                .orElseThrow(() -> new IllegalArgumentException("발주를 찾을 수 없습니다."));
    }

    @Transactional(readOnly = true)
    public byte[] exportDocument(Long purchaseOrderId) {
        PurchaseOrder purchaseOrder = get(purchaseOrderId);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("purchase-order");
            for (int i = 0; i < 6; i++) {
                sheet.setColumnWidth(i, 5200);
            }

            CellStyle titleStyle = createTitleStyle(workbook);
            CellStyle sectionLabelStyle = createSectionLabelStyle(workbook);
            CellStyle sectionValueStyle = createSectionValueStyle(workbook);
            CellStyle tableHeaderStyle = createTableHeaderStyle(workbook);
            CellStyle tableBodyStyle = createTableBodyStyle(workbook);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("발주서");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

            String exportDocumentNumber = generateExportDocumentNumber();
            Row documentNumberRow = sheet.createRow(2);
            Cell documentNumberLabelCell = documentNumberRow.createCell(0);
            documentNumberLabelCell.setCellValue("문서번호");
            documentNumberLabelCell.setCellStyle(sectionLabelStyle);
            Cell documentNumberValueCell = documentNumberRow.createCell(1);
            documentNumberValueCell.setCellValue(exportDocumentNumber);
            documentNumberValueCell.setCellStyle(sectionValueStyle);
            for (int i = 2; i <= 5; i++) {
                Cell mergedCell = documentNumberRow.createCell(i);
                mergedCell.setCellStyle(sectionValueStyle);
            }
            sheet.addMergedRegion(new CellRangeAddress(2, 2, 1, 5));

            writeSectionRow(
                    sheet, 3, sectionLabelStyle, sectionValueStyle,
                    "발주번호", formatDocumentNumber(purchaseOrder),
                    "발주일", formatDate(purchaseOrder.getCreatedAt())
            );
            writeSectionRow(
                    sheet, 4, sectionLabelStyle, sectionValueStyle,
                    "요청번호",
                    formatPurchaseRequestNumber(purchaseOrder),
                    "거래처",
                    nvl(purchaseOrder.getSupplier().getName())
            );
            writeSectionRow(
                    sheet, 5, sectionLabelStyle, sectionValueStyle,
                    "거래처 연락처",
                    nvl(purchaseOrder.getSupplier().getContactPhone()),
                    "거래처 담당자",
                    nvl(purchaseOrder.getSupplier().getContactName())
            );
            writeSectionRow(
                    sheet, 6, sectionLabelStyle, sectionValueStyle,
                    "매장",
                    nvl(purchaseOrder.getStore().getName()),
                    "창고",
                    nvl(purchaseOrder.getWarehouse().getName())
            );
            writeSectionRow(
                    sheet, 7, sectionLabelStyle, sectionValueStyle,
                    "납기요청일",
                    purchaseOrder.getDeliveryDueDate() == null ? "-" : purchaseOrder.getDeliveryDueDate().toString(),
                    "결제조건",
                    nvl(purchaseOrder.getPaymentTerms())
            );
            writeSectionRow(
                    sheet, 8, sectionLabelStyle, sectionValueStyle,
                    "수령인",
                    nvl(purchaseOrder.getReceiverName()),
                    "수령인 연락처",
                    nvl(purchaseOrder.getReceiverPhone())
            );

            Row shippingAddressRow = sheet.createRow(9);
            Cell shippingAddressLabelCell = shippingAddressRow.createCell(0);
            shippingAddressLabelCell.setCellValue("납품주소");
            shippingAddressLabelCell.setCellStyle(sectionLabelStyle);
            Cell shippingAddressValueCell = shippingAddressRow.createCell(1);
            shippingAddressValueCell.setCellValue(nvl(purchaseOrder.getShippingAddress()));
            shippingAddressValueCell.setCellStyle(sectionValueStyle);
            for (int i = 2; i <= 5; i++) {
                Cell mergedCell = shippingAddressRow.createCell(i);
                mergedCell.setCellStyle(sectionValueStyle);
            }
            sheet.addMergedRegion(new CellRangeAddress(9, 9, 1, 5));

            writeSectionRow(
                    sheet, 10, sectionLabelStyle, sectionValueStyle,
                    "작성자",
                    toAuthorText(purchaseOrder),
                    "직인",
                    "직인생략"
            );

            int headerRowIndex = 11;
            Row headerRow = sheet.createRow(headerRowIndex);
            String[] headers = {"No", "상품코드", "상품명", "수량", "납품창고", "비고"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(tableHeaderStyle);
            }

            int rowIndex = headerRowIndex + 1;
            List<PurchaseOrderItem> items = purchaseOrder.getItems();
            for (int i = 0; i < items.size(); i++) {
                PurchaseOrderItem item = items.get(i);
                Row row = sheet.createRow(rowIndex++);

                writeBodyCell(row, 0, i + 1, tableBodyStyle);
                writeBodyCell(row, 1, nvl(item.getProduct().getSku()), tableBodyStyle);
                writeBodyCell(row, 2, nvl(item.getProduct().getName()), tableBodyStyle);
                writeBodyCell(row, 3, item.getQuantity(), tableBodyStyle);
                writeBodyCell(row, 4, nvl(purchaseOrder.getWarehouse().getName()), tableBodyStyle);
                writeBodyCell(row, 5, "", tableBodyStyle);
            }

            if (items.isEmpty()) {
                Row emptyRow = sheet.createRow(rowIndex++);
                writeBodyCell(emptyRow, 0, "-", tableBodyStyle);
                writeBodyCell(emptyRow, 1, "-", tableBodyStyle);
                writeBodyCell(emptyRow, 2, "-", tableBodyStyle);
                writeBodyCell(emptyRow, 3, "-", tableBodyStyle);
                writeBodyCell(emptyRow, 4, "-", tableBodyStyle);
                writeBodyCell(emptyRow, 5, "-", tableBodyStyle);
            }

            Row memoRow = sheet.createRow(rowIndex + 1);
            Cell memoLabelCell = memoRow.createCell(0);
            memoLabelCell.setCellValue("메모");
            memoLabelCell.setCellStyle(sectionLabelStyle);
            Cell memoValueCell = memoRow.createCell(1);
            memoValueCell.setCellValue(nvl(purchaseOrder.getMemo()));
            memoValueCell.setCellStyle(sectionValueStyle);
            for (int i = 2; i <= 5; i++) {
                Cell mergedCell = memoRow.createCell(i);
                mergedCell.setCellStyle(sectionValueStyle);
            }
            sheet.addMergedRegion(new CellRangeAddress(rowIndex + 1, rowIndex + 1, 1, 5));

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("발주서 엑셀 파일 생성에 실패했습니다.", e);
        }
    }

    @Transactional(readOnly = true)
    public String buildDocumentDownloadFilename(Long purchaseOrderId) {
        PurchaseOrder purchaseOrder = get(purchaseOrderId);
        return "po-" + formatDocumentNumber(purchaseOrder) + ".xlsx";
    }

    public PurchaseOrder order(Long purchaseOrderId) {
        PurchaseOrder po = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new IllegalArgumentException("발주를 찾을 수 없습니다."));
        po.order();

        if (!inboundRepository.existsByPurchaseOrder_Id(po.getId())) {
            InboundCreateRequest inboundCreateRequest = new InboundCreateRequest();
            inboundCreateRequest.setPurchaseOrderId(po.getId());
            inboundService.createInbound(inboundCreateRequest);
        }

        return po;
    }

    public PurchaseOrder cancel(Long purchaseOrderId) {
        PurchaseOrder po = purchaseOrderRepository.findById(purchaseOrderId)
                .orElseThrow(() -> new IllegalArgumentException("발주를 찾을 수 없습니다."));
        po.cancel();
        return po;
    }

    public PurchaseOrder updateDraft(Long purchaseOrderId, PurchaseOrderDraftUpdateRequest request) {
        PurchaseOrder po = purchaseOrderRepository.findDetailById(purchaseOrderId)
                .orElseThrow(() -> new IllegalArgumentException("발주를 찾을 수 없습니다."));

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new IllegalArgumentException("거래처를 찾을 수 없습니다."));
        Warehouse warehouse = warehouseRepository.findById(request.getWarehouseId())
                .orElseThrow(() -> new IllegalArgumentException("창고를 찾을 수 없습니다."));

        po.updateDraft(
                supplier,
                warehouse,
                request.getDeliveryDueDate(),
                request.getReceiverName(),
                request.getReceiverPhone(),
                request.getShippingAddress(),
                request.getPaymentTerms(),
                request.getMemo()
        );

        return po;
    }

    public void deleteDraft(Long purchaseOrderId) {
        PurchaseOrder po = purchaseOrderRepository.findDetailById(purchaseOrderId)
                .orElseThrow(() -> new IllegalArgumentException("발주를 찾을 수 없습니다."));

        if (po.getStatus() != PurchaseOrderStatus.CREATED) {
            throw new IllegalStateException("작성 단계(CREATED) 발주서만 삭제할 수 있습니다.");
        }
        if (po.getPurchaseRequest() != null && po.getPurchaseRequest().getStatus() != PurchaseRequestStatus.REQUESTED) {
            throw new IllegalStateException("승인 이후에는 발주서를 삭제할 수 없습니다.");
        }

        purchaseOrderRepository.delete(po);
    }

    @Transactional(readOnly = true)
    public PurchaseOrderListResponse list(
            Long storeId,
            String storeKeyword,
            String storeName,
            String storeCode,
            Long warehouseId,
            Long supplierId,
            String supplierName,
            String status,
            String createdFrom,
            String createdTo,
            String orderedFrom,
            String orderedTo,
            String from,
            String to,
            int page,
            int size
    ) {
        PurchaseOrderStatus st = QueryParamParser.parseEnumOrNull(status, PurchaseOrderStatus.class, "status");
        String resolvedCreatedFrom = firstNonBlank(createdFrom, from);
        String resolvedCreatedTo = firstNonBlank(createdTo, to);
        LocalDateTime createdFromDt = QueryParamParser.parseFromDate(resolvedCreatedFrom);
        LocalDateTime createdToDt = QueryParamParser.parseToDateExclusive(resolvedCreatedTo);
        LocalDateTime orderedFromDt = QueryParamParser.parseFromDate(orderedFrom);
        LocalDateTime orderedToDt = QueryParamParser.parseToDateExclusive(orderedTo);
        String storeKeywordValue = normalizeKeyword(storeKeyword);
        String storeNameKeyword = normalizeKeyword(storeName);
        String storeCodeKeyword = normalizeKeyword(storeCode);
        String supplierNameKeyword = normalizeKeyword(supplierName);

        PageRequest pageable = PageRequest.of(page, size);
        Page<PurchaseOrder> result = purchaseOrderRepository.search(
                storeId,
                storeKeywordValue,
                storeNameKeyword,
                storeCodeKeyword,
                warehouseId,
                supplierId,
                supplierNameKeyword,
                st,
                createdFromDt,
                createdToDt,
                orderedFromDt,
                orderedToDt,
                pageable
        );

        List<PurchaseOrderListResponse.Item> content = result.getContent().stream()
                .map(po -> new PurchaseOrderListResponse.Item(
                        po.getId(),
                        po.getPurchaseRequest() == null ? null : po.getPurchaseRequest().getId(),
                        po.getSupplier().getId(),
                        po.getSupplier().getName(),
                        po.getStore().getId(),
                        po.getStore().getName(),
                        po.getStore().getStoreCode(),
                        po.getWarehouse().getId(),
                        po.getStatus().name(),
                        po.getCreatedAt(),
                        po.getOrderedAt()
                ))
                .toList();

        return new PurchaseOrderListResponse(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String trimmed = keyword.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        if (fallback != null && !fallback.isBlank()) {
            return fallback;
        }
        return null;
    }

    private void writeSectionRow(
            Sheet sheet,
            int rowIndex,
            CellStyle labelStyle,
            CellStyle valueStyle,
            String leftLabel,
            String leftValue,
            String rightLabel,
            String rightValue
    ) {
        Row row = sheet.createRow(rowIndex);

        Cell leftLabelCell = row.createCell(0);
        leftLabelCell.setCellValue(leftLabel);
        leftLabelCell.setCellStyle(labelStyle);

        Cell leftValueCell = row.createCell(1);
        leftValueCell.setCellValue(leftValue);
        leftValueCell.setCellStyle(valueStyle);

        Cell rightLabelCell = row.createCell(3);
        rightLabelCell.setCellValue(rightLabel);
        rightLabelCell.setCellStyle(labelStyle);

        Cell rightValueCell = row.createCell(4);
        rightValueCell.setCellValue(rightValue);
        rightValueCell.setCellStyle(valueStyle);

        Cell spacerLeftCell = row.createCell(2);
        spacerLeftCell.setCellStyle(valueStyle);

        Cell spacerRightCell = row.createCell(5);
        spacerRightCell.setCellStyle(valueStyle);
    }

    private void writeBodyCell(Row row, int columnIndex, Object value, CellStyle cellStyle) {
        Cell cell = row.createCell(columnIndex);
        if (value instanceof Number number) {
            cell.setCellValue(number.doubleValue());
        } else {
            cell.setCellValue(value == null ? "" : value.toString());
        }
        cell.setCellStyle(cellStyle);
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 20);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createSectionLabelStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createSectionValueStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createTableHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createTableBodyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private String generateExportDocumentNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 10000);
        return timestamp + "-" + random;
    }

    private String formatDocumentNumber(PurchaseOrder purchaseOrder) {
        LocalDateTime createdAt = purchaseOrder.getCreatedAt();
        if (createdAt == null || purchaseOrder.getId() == null) {
            return purchaseOrder.getId() == null ? "-" : String.valueOf(purchaseOrder.getId());
        }
        return createdAt.format(DateTimeFormatter.ofPattern("yyMMdd")) + "-" + String.format("%04d", purchaseOrder.getId());
    }

    private String formatPurchaseRequestNumber(PurchaseOrder purchaseOrder) {
        if (purchaseOrder.getPurchaseRequest() == null) {
            return "-";
        }
        Long requestId = purchaseOrder.getPurchaseRequest().getId();
        LocalDateTime createdAt = purchaseOrder.getPurchaseRequest().getCreatedAt();
        if (requestId == null || createdAt == null) {
            return requestId == null ? "-" : String.valueOf(requestId);
        }
        return createdAt.format(DateTimeFormatter.ofPattern("yyMMdd")) + "-" + String.format("%04d", requestId);
    }

    private String formatDate(LocalDateTime value) {
        if (value == null) {
            return "-";
        }
        return value.toLocalDate().toString();
    }

    private String nvl(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String toAuthorText(PurchaseOrder po) {
        if (po.getAuthoredBy() == null) {
            return "-";
        }
        String name = nvl(po.getAuthoredBy().getName());
        String code = nvl(po.getAuthoredBy().getEmployeeCode());
        if ("-".equals(code)) {
            return name;
        }
        return name + "(" + code + ")";
    }
}
