package com.university.lms.service.circulation;

import com.university.lms.dto.request.ReservationRequestDTO;
import com.university.lms.dto.response.ReservationDTO;

public interface ReservationService {

    ReservationDTO reserve(ReservationRequestDTO request);

    void cancelReservation(Long reservationId);

    /** Expires unclaimed READY holds. Entry point for a scheduled sweep. */
    int expireStaleReservations();
}
