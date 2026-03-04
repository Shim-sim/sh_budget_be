package com.shbudget.domain.pushsubscription.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PushUnsubscribeRequest(
        @NotBlank String endpoint
) {
}
