package com.acme.salarymgmt.compensation.dto;

import java.util.List;

public record EmployeeFilterOptionsResponse(
        List<String> departments,
        List<String> countries
) {
}
