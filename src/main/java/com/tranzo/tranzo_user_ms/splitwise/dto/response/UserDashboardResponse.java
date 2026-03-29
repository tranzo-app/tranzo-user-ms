package com.tranzo.tranzo_user_ms.splitwise.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * DTO for user dashboard response containing splitwise summary.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDashboardResponse {

    private BigDecimal totalAmountUserOwes;
    private BigDecimal totalAmountOwedToUser;
    private BigDecimal totalOutstandingBalance;
    private TravelPalSummary travelPalSummary;
    private List<ExpenseSummary> expenseSummary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TravelPalSummary {
        private List<TravelPalOwe> travelPalsUserOwes;
        private List<TravelPalOwed> travelPalsOwedToUser;
    private String currency;
    private int totalTravelPals;
    private BigDecimal totalOwedAmount;
        private BigDecimal totalOweAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TravelPalOwe {
        private UUID userId;
        private String userName;
        private String userEmail;
        private BigDecimal amount;
        private String profilePictureUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TravelPalOwed {
        private UUID userId;
        private String userName;
        private String userEmail;
        private BigDecimal amount;
        private String profilePictureUrl;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExpenseSummary {
        private UUID tripId;
        private String tripTitle;
        private LocalDate tripDate;
        private BigDecimal oweAmount;
        private String tripDestination;
        private String tripStatus;
        private String currency;
    }
}
