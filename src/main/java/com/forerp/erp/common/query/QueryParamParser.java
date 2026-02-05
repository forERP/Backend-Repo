package com.forerp.erp.common.query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public final class QueryParamParser {

    private QueryParamParser() {}

    public static LocalDateTime parseFromDate(String from) {
        if (from == null || from.isBlank()) return null;
        try {
            return LocalDate.parse(from.trim()).atStartOfDay();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("from 형식이 올바르지 않습니다. yyyy-MM-dd");
        }
    }

    /* to 날짜 포함 검색을 위해 to+1일 00:00 미만(<) 조건으로 조회할 때 쓰는 값 */
    public static LocalDateTime parseToDateExclusive(String to) {
        if (to == null || to.isBlank()) return null;
        try {
            return LocalDate.parse(to.trim()).plusDays(1).atStartOfDay();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("to 형식이 올바르지 않습니다. yyyy-MM-dd");
        }
    }

    public static <E extends Enum<E>> E parseEnumOrNull(String value, Class<E> enumType, String fieldName) {
        if (value == null || value.isBlank()) return null;
        try {
            return Enum.valueOf(enumType, value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("유효하지 않은 " + fieldName + " 입니다: " + value);
        }
    }
}