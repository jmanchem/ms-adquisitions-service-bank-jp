package com.example.msacquisitionbank.models.dto;

import com.example.msacquisitionbank.models.entities.Acquisition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
public class AverageBalanceDTO {
    private Double average;
    private List<Acquisition> acquisitions;
}
