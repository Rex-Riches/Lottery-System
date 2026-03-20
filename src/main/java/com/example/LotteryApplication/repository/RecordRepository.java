package com.example.LotteryApplication.repository;
import com.example.LotteryApplication.model.Record;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RecordRepository  extends JpaRepository<Record,Integer> {
    List<Record> findByRecordDate(LocalDate recordDate);
}
