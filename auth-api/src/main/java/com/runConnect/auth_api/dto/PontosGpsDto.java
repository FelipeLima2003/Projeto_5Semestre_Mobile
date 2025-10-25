package com.runConnect.auth_api.dto;

import java.math.BigDecimal;

public record PontosGpsDto(
    BigDecimal latitude,
    BigDecimal longitude,
    Integer ordem
) {}
