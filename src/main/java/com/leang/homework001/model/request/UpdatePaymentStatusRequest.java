package com.leang.homework001.model.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class UpdatePaymentStatusRequest {
    private List<Integer> tickedIds;
    private Boolean paymentStatus;
}
