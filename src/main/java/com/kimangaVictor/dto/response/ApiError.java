package com.kimangaVictor.dto.response;

import java.util.Map;

/**
 * @param code   stable machine-readable identifier the frontend can branch on
 * @param message human-readable summary safe to surface in the UI
 * @param fields per-field validation messages, empty for non-validation errors
 */
public record ApiError(String code, String message, Map<String, String> fields) {
}
