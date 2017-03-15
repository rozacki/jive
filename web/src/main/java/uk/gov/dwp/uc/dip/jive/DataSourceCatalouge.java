package uk.gov.dwp.uc.dip.jive;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.collections.map.HashedMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by chrisrozacki on 08/03/2017.
 */
public class DataSourceCatalouge {
    final Map<String,Set<String>> databasesCollections  = createDatabasesCollections();

    public Set<String> getDatabases(){
        return databasesCollections.keySet();
    }

    public Set<String> getCollections(String database){
        return databasesCollections.get(database);
    }

    Map<String, Set<String>> createDatabasesCollections(){
        Map<String,Set<String>> dbs = new HashMap<>();
        dbs.put("accepted-data", ImmutableSet.of("carerCircumstances",
                "childcare",
                "childrenCircumstances",
                "earningsData",
                "employmentCircumstances",
                "healthAndDisabilityCircumstances",
                "housingCircumstances",
                "ineligiblePartner",
                "otherBenefit",
                "otherIncome",
                "overlappingBenefit",
                "personDetails",
                "terminalIllness",
                "workAndEarningsCircumstances",
                "workCapabilityAssessmentDecision"));

        dbs.put("advances", ImmutableSet.of("advanceDebtPosition",
                "advanceGroup",
                "recoverableHardshipPayment"));

        dbs.put("agent-core", ImmutableSet.of(
                "agent",
                "agentToDo",
                "agentToDoArchive",
                "agentWorkGroupAllocation",
                "claimantHistory",
                "journalFeedback",
                "manualOverride",
                "systemWorkGroupAllocation",
                "team"));

        dbs.put("appointments", ImmutableSet.of(
        "appointment"));

        dbs.put("calculator", ImmutableSet.of(
                        "calculation"));

        dbs.put("claimant-history", ImmutableSet.of(
                "claimHistoryEntry"));

        dbs.put("core", ImmutableSet.of(
                "carerDeclaration",
                "childcareDeclaration",
                "childDeclaration",
                "claimant",
                "claimantCommitment",
                "contract",
                "educationDeclaration",
                "healthAndDisabilityDeclaration",
                "healthDeclaration",
                "housingDeclaration",
                "journal",
                "periodOfSickness",
                "personalDetails",
                "statement",
                "survey",
                "toDo",
                "workAndEarningsDeclaration",
                "workGroupAllocation"));

        dbs.put("matchingService", ImmutableSet.of(
                "localUserMatchingData",
                "managementInformation",
                "matchingServiceRequests"));

        dbs.put("organisation", ImmutableSet.of(
                "deliveryUnitAddress",
                "organisation"));

        dbs.put("penalties-and-deductions", ImmutableSet.of(
                "debtPosition",
                "fraudPenalty",
                "outOfClaimDays",
                "sanction",
                "sanctionEscalation",
                "sanctionProgress",
                "sanctionTerminationDecision"));

        return Collections.unmodifiableMap(dbs);
    }
}
