package com.codify.sactionsservice.screening.service.dto;

import java.util.List;

public record ScreeningResult(ScreeningVerdict verdict, List<ScreeningMatch> matches) {
}
