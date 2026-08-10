package com.minibanking.overview.aggregation;

public record ServiceCallResult<T>(
        T data,
        boolean available,
        String service,
        String failureReason
) {

    public static <T> ServiceCallResult<T> available(String service, T data) {
        return new ServiceCallResult<>(data, true, service, null);
    }

    public static <T> ServiceCallResult<T> unavailable(String service, T fallback, Throwable throwable) {
        String reason = throwable == null ? "Unknown downstream failure" : throwable.getClass().getSimpleName();
        return new ServiceCallResult<>(fallback, false, service, reason);
    }
}
