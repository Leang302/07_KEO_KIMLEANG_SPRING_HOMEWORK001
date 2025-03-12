package com.leang.homework001.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaginationResponse {
    private Integer totalElement;
    private Integer currentPage;
    private Integer pageSize;
    private Integer totalPage;
}
