package com.forerp.erp.attendance.service.support;

import com.forerp.erp.attendance.domain.Attendance;
import com.forerp.erp.attendance.dto.AttendanceDto;
import org.springframework.stereotype.Component;

@Component
public class AttendanceResponseMapper {

    public AttendanceDto.Response toResponse(Attendance attendance){
        return new AttendanceDto.Response(
                attendance.getId(),
                attendance.getUser().getName(),
                attendance.getClockIn(),
                attendance.getClockOut(),
                attendance.getStatus()
        );
    }

    public AttendanceDto.HistoryResponse toHistoryResponse(Attendance attendance){
        return new AttendanceDto.HistoryResponse(
                attendance.getId(),
                attendance.getUser().getId(),
                attendance.getUser().getName(),
                attendance.getWorkDate(),
                attendance.getClockIn(),
                attendance.getClockOut(),
                attendance.getStatus()
        );
    }
}
