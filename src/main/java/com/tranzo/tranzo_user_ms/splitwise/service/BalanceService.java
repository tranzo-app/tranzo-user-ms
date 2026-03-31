package com.tranzo.tranzo_user_ms.splitwise.service;

import com.tranzo.tranzo_user_ms.splitwise.dto.SettlementProposal;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.BalanceResponse;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.IndividualBalanceResponse;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.UserDashboardResponse;
import com.tranzo.tranzo_user_ms.splitwise.dto.response.UserResponse;
import com.tranzo.tranzo_user_ms.splitwise.entity.Balance;
import com.tranzo.tranzo_user_ms.splitwise.entity.Expense;
import com.tranzo.tranzo_user_ms.splitwise.entity.ExpenseSplit;
import com.tranzo.tranzo_user_ms.splitwise.entity.Settlement;
import com.tranzo.tranzo_user_ms.splitwise.entity.SplitwiseGroup;
import com.tranzo.tranzo_user_ms.splitwise.exception.InsufficientBalanceException;
import com.tranzo.tranzo_user_ms.splitwise.repository.BalanceRepository;
import com.tranzo.tranzo_user_ms.splitwise.repository.ExpenseRepository;
import com.tranzo.tranzo_user_ms.splitwise.repository.SplitwiseGroupRepository;
import com.tranzo.tranzo_user_ms.trip.repository.TripRepository;
import com.tranzo.tranzo_user_ms.user.model.UserProfileEntity;
import com.tranzo.tranzo_user_ms.user.model.UsersEntity;
import com.tranzo.tranzo_user_ms.user.repository.UserRepository;
import com.tranzo.tranzo_user_ms.user.service.TravelPalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing and calculating balances between users in Splitwise groups.
 */
@Slf4j
@Service
@Transactional
public class BalanceService {

    private final BalanceRepository balanceRepository;
    private final ExpenseRepository expenseRepository;
    private final SettlementOptimizationService settlementOptimizationService;
    private final UserRepository userRepository;
    private final SplitwiseGroupRepository splitwiseGroupRepository;
    private final SplitwiseGroupService splitwiseGroupService;
    private final TripRepository tripRepository;
    private final TravelPalService travelPalService;

    public BalanceService(BalanceRepository balanceRepository,
                          ExpenseRepository expenseRepository,
                          SettlementOptimizationService settlementOptimizationService,
                          UserRepository userRepository,
                          SplitwiseGroupRepository splitwiseGroupRepository,
                          SplitwiseGroupService splitwiseGroupService,
                          TripRepository tripRepository,
                          TravelPalService travelPalService) {
        this.balanceRepository = balanceRepository;
        this.expenseRepository = expenseRepository;
        this.settlementOptimizationService = settlementOptimizationService;
        this.userRepository = userRepository;
        this.splitwiseGroupRepository = splitwiseGroupRepository;
        this.splitwiseGroupService = splitwiseGroupService;
        this.tripRepository = tripRepository;
        this.travelPalService = travelPalService;
    }

    @Transactional(readOnly = true)
    public List<BalanceResponse> getGroupBalances(UUID groupId) {
        log.debug("Calculating balances for group: {}", groupId);
        List<Object[]> rows = balanceRepository.getBalanceSummaryForGroup(groupId);
        List<BalanceResponse> result = new ArrayList<>();
        for (Object[] row : rows) {
            if (row.length >= 4 && row[0] != null) {
                UUID userId = (UUID) row[0];
                BigDecimal totalOwedTo = row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO;
                BigDecimal totalOwedBy = row[2] != null ? (BigDecimal) row[2] : BigDecimal.ZERO;
                BigDecimal netBalance = row[3] != null ? (BigDecimal) row[3] : totalOwedTo.subtract(totalOwedBy);
                UserResponse userResponse = toUserResponse(userId);
                result.add(BalanceResponse.builder()
                        .user(userResponse)
                        .totalOwed(totalOwedBy)
                        .totalOwing(totalOwedTo)
                        .netBalance(netBalance)
                        .individualBalances(null)
                        .balanceDetails(null)
                        .build());
            }
        }
        result.sort((a, b) -> b.getNetBalance().compareTo(a.getNetBalance()));
        log.debug("Calculated balances for {} users in group {}", result.size(), groupId);
        return result;
    }

    @Transactional(readOnly = true)
    public BalanceResponse getUserBalanceInGroup(UUID groupId, UUID userId) {
        log.debug("Calculating balance for user {} in group: {}", userId, groupId);
        BigDecimal totalOwed = balanceRepository.getTotalOwedByUserInGroup(groupId, userId);
        BigDecimal totalOwing = balanceRepository.getTotalOwedToUserInGroup(groupId, userId);
        BigDecimal netBalance = totalOwing.subtract(totalOwed);
        List<Balance> balances = balanceRepository.findBalancesForUserInGroup(groupId, userId);
        List<IndividualBalanceResponse> details = new ArrayList<>();
        for (Balance b : balances) {
            UUID otherId = b.getOwedBy().equals(userId) ? b.getOwedTo() : b.getOwedBy();
            BigDecimal amount = b.getAmount();
            String type = b.getOwedBy().equals(userId) ? "OWING" : "OWED";
            UserResponse other = toUserResponse(otherId);
            details.add(IndividualBalanceResponse.builder()
                    .otherUser(other)
                    .amount(amount)
                    .type(type)
                    .build());
        }
        return BalanceResponse.builder()
                .user(toUserResponse(userId))
                .totalOwed(totalOwed)
                .totalOwing(totalOwing)
                .netBalance(netBalance)
                .individualBalances(null)
                .balanceDetails(details)
                .build();
    }

    public void updateBalancesForExpense(Expense expense) {
        UUID groupId = expense.getGroupId();
        UUID payer = expense.getPaidBy();
        for (ExpenseSplit split : expense.getSplits()) {
            UUID debtor = split.getUserId();
            if (payer.equals(debtor)) continue;
            BigDecimal amount = split.getAmount();
            updateOrCreateBalance(debtor, payer, groupId, amount);
        }
    }

    public void recalculateBalancesForGroup(UUID groupId) {
        log.debug("Recalculating balances for group: {}", groupId);
        balanceRepository.deleteByGroupId(groupId);
        List<Expense> expenses = expenseRepository.findByGroupId(groupId);
        for (Expense expense : expenses) {
            updateBalancesForExpense(expense);
        }
    }

    private void updateOrCreateBalance(UUID fromUserId, UUID toUserId, UUID groupId, BigDecimal amount) {
        if (fromUserId.equals(toUserId) || amount.compareTo(BigDecimal.ZERO) <= 0) return;
        Optional<Balance> opt = balanceRepository.findByGroupIdAndOwedByAndOwedTo(groupId, fromUserId, toUserId);
        if (opt.isPresent()) {
            Balance b = opt.get();
            b.addAmount(amount);
            if (b.shouldDelete()) {
                balanceRepository.delete(b);
            } else {
                balanceRepository.save(b);
            }
        } else {
            Optional<Balance> reverseOpt = balanceRepository.findByGroupIdAndOwedByAndOwedTo(groupId, toUserId, fromUserId);
            if (reverseOpt.isPresent()) {
                Balance reverse = reverseOpt.get();
                reverse.subtractAmount(amount);
                if (reverse.shouldDelete()) {
                    balanceRepository.delete(reverse);
                } else {
                    balanceRepository.save(reverse);
                }
            } else {
                var group = splitwiseGroupRepository.findById(groupId).orElseThrow();
                Balance newBalance = Balance.builder()
                        .group(group)
                        .owedBy(fromUserId)
                        .owedTo(toUserId)
                        .amount(amount)
                        .build();
                balanceRepository.save(newBalance);
            }
        }
    }

    public void validateSettlementAmount(UUID groupId, UUID fromUserId, UUID toUserId, BigDecimal amount) {
        Optional<Balance> opt = balanceRepository.findByGroupIdAndOwedByAndOwedTo(groupId, fromUserId, toUserId);
        BigDecimal available = opt.map(Balance::getAmount).orElse(BigDecimal.ZERO);
        if (available.compareTo(amount) < 0) {
            throw new InsufficientBalanceException(available, amount);
        }
    }

    public void updateBalancesForSettlement(Settlement settlement) {
        UUID groupId = settlement.getGroup().getId();
        UUID paidBy = settlement.getPaidBy();
        UUID paidTo = settlement.getPaidTo();
        BigDecimal amount = settlement.getAmount();
        Optional<Balance> opt = balanceRepository.findByGroupIdAndOwedByAndOwedTo(groupId, paidBy, paidTo);
        if (opt.isPresent()) {
            Balance b = opt.get();
            b.subtractAmount(amount);
            if (b.shouldDelete()) {
                balanceRepository.delete(b);
            } else {
                balanceRepository.save(b);
            }
        } else {
            Optional<Balance> reverseOpt = balanceRepository.findByGroupIdAndOwedByAndOwedTo(groupId, paidTo, paidBy);
            if (reverseOpt.isPresent()) {
                Balance reverse = reverseOpt.get();
                reverse.addAmount(amount);
                balanceRepository.save(reverse);
            } else {
                Balance newBalance = Balance.builder()
                        .group(settlement.getGroup())
                        .owedBy(paidTo)
                        .owedTo(paidBy)
                        .amount(amount)
                        .build();
                balanceRepository.save(newBalance);
            }
        }
    }

    public void reverseBalancesForSettlement(Settlement settlement) {
        UUID groupId = settlement.getGroup().getId();
        UUID paidBy = settlement.getPaidBy();
        UUID paidTo = settlement.getPaidTo();
        BigDecimal amount = settlement.getAmount();
        Optional<Balance> opt = balanceRepository.findByGroupIdAndOwedByAndOwedTo(groupId, paidBy, paidTo);
        if (opt.isPresent()) {
            Balance b = opt.get();
            b.addAmount(amount);
            balanceRepository.save(b);
        } else {
            Optional<Balance> reverseOpt = balanceRepository.findByGroupIdAndOwedByAndOwedTo(groupId, paidTo, paidBy);
            if (reverseOpt.isPresent()) {
                Balance reverse = reverseOpt.get();
                reverse.subtractAmount(amount);
                if (reverse.shouldDelete()) {
                    balanceRepository.delete(reverse);
                } else {
                    balanceRepository.save(reverse);
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public List<SettlementProposal> getOptimizedSettlements(UUID groupId) {
        Map<UUID, BigDecimal> netBalances = calculateNetBalancesForGroup(groupId);
        return settlementOptimizationService.optimizeSettlements(netBalances);
    }

    private Map<UUID, BigDecimal> calculateNetBalancesForGroup(UUID groupId) {
        List<Object[]> rows = balanceRepository.getBalanceSummaryForGroup(groupId);
        Map<UUID, BigDecimal> net = new HashMap<>();
        for (Object[] row : rows) {
            if (row.length >= 4 && row[0] != null && row[3] != null) {
                net.put((UUID) row[0], (BigDecimal) row[3]);
            }
        }
        return net;
    }

    private UserResponse toUserResponse(UUID userId) {
        if (userId == null) return null;
        UsersEntity user = userRepository.findUserByUserUuid(userId).orElse(null);
        if (user == null) return UserResponse.builder().userUuid(userId).build();
        String name = "";
        if (user.getUserProfileEntity() != null) {
            UserProfileEntity p = user.getUserProfileEntity();
            name = (p.getFirstName() != null ? p.getFirstName() : "") + " " + (p.getLastName() != null ? p.getLastName() : "");
        }
        return UserResponse.builder()
                .userUuid(user.getUserUuid())
                .name(name.trim().isEmpty() ? null : name.trim())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .build();
    }

    @Transactional(readOnly = true)
    public UserDashboardResponse getUserDashboard(UUID userId) {
        log.debug("Generating dashboard for user: {}", userId);
        
        // Get all valid groups user is part of (using the fixed logic from SplitwiseGroupService)
        List<com.tranzo.tranzo_user_ms.splitwise.dto.response.GroupResponse> userGroupResponses = splitwiseGroupService.getUserGroups(userId);
        List<SplitwiseGroup> userGroups = userGroupResponses.stream()
                .map(groupResponse -> splitwiseGroupRepository.findById(groupResponse.getId()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        log.debug("Found {} valid groups for user dashboard", userGroups.size());
        
        // Calculate totals across all groups
        BigDecimal totalOwed = BigDecimal.ZERO;
        BigDecimal totalOwing = BigDecimal.ZERO;
        List<UserDashboardResponse.IndividualOwe> userOwesList = new ArrayList<>();
        List<UserDashboardResponse.IndividualOwed> owedToUserList = new ArrayList<>();
        List<UserDashboardResponse.ExpenseSummary> expenseSummaries = new ArrayList<>();
        
        for (SplitwiseGroup group : userGroups) {
            // Get user balance in this group
            BalanceResponse groupBalance = getUserBalanceInGroup(group.getId(), userId);
            totalOwed = totalOwed.add(groupBalance.getTotalOwed());
            totalOwing = totalOwing.add(groupBalance.getTotalOwing());
            
            // Process individual balances for this group (all users, not just travel pals)
            processIndividualBalancesForGroup(group, userId, userOwesList, owedToUserList);
            
            // Add expense summary for this group (only if user has non-zero balance)
            addExpenseSummaryForGroup(group, expenseSummaries, userId);
        }
        
        // Build individual balance summary (now includes all users, not just travel pals)
        UserDashboardResponse.IndividualBalanceSummary individualBalanceSummary = UserDashboardResponse.IndividualBalanceSummary.builder()
                .userOwesList(userOwesList)
                .owedToUserList(owedToUserList)
                .currency("INR") // Changed to INR as requested
                .totalIndividuals(userOwesList.size() + owedToUserList.size())
                .totalOwedAmount(userOwesList.stream()
                        .map(UserDashboardResponse.IndividualOwe::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .totalOweAmount(owedToUserList.stream()
                        .map(UserDashboardResponse.IndividualOwed::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .build();
        
        BigDecimal totalOutstandingBalance = totalOwing.subtract(totalOwed);
        
        log.debug("Dashboard generated for user: {} | groups: {} | totalOwed: {} | totalOwing: {} | expenseSummaries: {}", 
                userId, userGroups.size(), totalOwed, totalOwing, expenseSummaries.size());
        
        return UserDashboardResponse.builder()
                .totalAmountUserOwes(totalOwed)
                .totalAmountOwedToUser(totalOwing)
                .totalOutstandingBalance(totalOutstandingBalance)
                .individualBalanceSummary(individualBalanceSummary)
                .expenseSummary(expenseSummaries)
                .build();
    }
    
    private void processIndividualBalancesForGroup(SplitwiseGroup group, UUID userId, 
                                       List<UserDashboardResponse.IndividualOwe> userOwesList,
                                       List<UserDashboardResponse.IndividualOwed> owedToUserList) {
        List<Balance> balances = balanceRepository.findBalancesForUserInGroup(group.getId(), userId);
        
        for (Balance balance : balances) {
            UUID otherUserId = balance.getOwedBy().equals(userId) ? balance.getOwedTo() : balance.getOwedBy();
            
            // Include all individual summaries regardless of travel pal status
            BigDecimal amount = balance.getAmount();
            
            // Skip if amount is exactly zero
            if (amount.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            
            if (balance.getOwedBy().equals(userId)) {
                // User owes to other person
                userOwesList.add(createIndividualOwe(otherUserId, amount));
            } else {
                // Other person owes to user
                owedToUserList.add(createIndividualOwed(otherUserId, amount));
            }
        }
    }
    
    private UserDashboardResponse.IndividualOwe createIndividualOwe(UUID userId, BigDecimal amount) {
        UsersEntity user = userRepository.findUserByUserUuid(userId).orElse(null);
        String userName = "";
        String userEmail = user != null ? user.getEmail() : "";
        String profilePictureUrl = "";
        
        if (user != null && user.getUserProfileEntity() != null) {
            UserProfileEntity profile = user.getUserProfileEntity();
            userName = (profile.getFirstName() != null ? profile.getFirstName() : "") + 
                      " " + (profile.getLastName() != null ? profile.getLastName() : "");
            profilePictureUrl = profile.getProfilePictureUrl() != null ? profile.getProfilePictureUrl() : "";
        }
        
        return UserDashboardResponse.IndividualOwe.builder()
                .userId(userId)
                .userName(userName.trim())
                .userEmail(userEmail)
                .amount(amount)
                .profilePictureUrl(profilePictureUrl)
                .build();
    }
    
    private UserDashboardResponse.IndividualOwed createIndividualOwed(UUID userId, BigDecimal amount) {
        UsersEntity user = userRepository.findUserByUserUuid(userId).orElse(null);
        String userName = "";
        String userEmail = user != null ? user.getEmail() : "";
        String profilePictureUrl = "";
        
        if (user != null && user.getUserProfileEntity() != null) {
            UserProfileEntity profile = user.getUserProfileEntity();
            userName = (profile.getFirstName() != null ? profile.getFirstName() : "") + 
                      " " + (profile.getLastName() != null ? profile.getLastName() : "");
            profilePictureUrl = profile.getProfilePictureUrl() != null ? profile.getProfilePictureUrl() : "";
        }
        
        return UserDashboardResponse.IndividualOwed.builder()
                .userId(userId)
                .userName(userName.trim())
                .userEmail(userEmail)
                .amount(amount)
                .profilePictureUrl(profilePictureUrl)
                .build();
    }
    
    private void addExpenseSummaryForGroup(SplitwiseGroup group, List<UserDashboardResponse.ExpenseSummary> expenseSummaries, UUID userId) {
        com.tranzo.tranzo_user_ms.trip.model.TripEntity trip = tripRepository.findById(group.getTripId()).orElse(null);
        if (trip == null) return;
        
        // Get user's total owe amount for this trip
        BigDecimal oweAmount = balanceRepository.getTotalOwedByUserInGroup(group.getId(), userId);
        
        // Get user's total owed amount for this trip
        BigDecimal owedAmount = balanceRepository.getTotalOwedToUserInGroup(group.getId(), userId);
        
        // Skip adding expense summary if both amounts are exactly zero
        if (oweAmount.compareTo(BigDecimal.ZERO) == 0 && owedAmount.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        
        expenseSummaries.add(UserDashboardResponse.ExpenseSummary.builder()
                .tripId(trip.getTripId())
                .tripTitle(trip.getTripTitle())
                .tripDate(trip.getTripStartDate() != null ? trip.getTripStartDate() : LocalDate.now())
                .oweAmount(oweAmount)
                .owedAmount(owedAmount)
                .tripDestination(trip.getTripDestination() != null ? trip.getTripDestination() : "Unknown Destination")
                .tripStatus(trip.getTripStatus() != null ? trip.getTripStatus().toString() : "UNKNOWN")
                .currency("INR") // Changed to INR as requested
                .build());
    }
}
