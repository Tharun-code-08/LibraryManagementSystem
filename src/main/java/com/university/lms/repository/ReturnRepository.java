package com.university.lms.repository;

import java.time.LocalDateTime;
import java.util.List;

import com.university.lms.entity.Return;

public interface ReturnRepository {

    List<Return> findByReturnDateRange(LocalDateTime from, LocalDateTime to);

    Return save(Return returnRecord);
}
