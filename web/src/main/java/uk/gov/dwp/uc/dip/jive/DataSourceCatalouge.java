package uk.gov.dwp.uc.dip.jive;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.Map;
import java.util.Set;

/**
 * Created by chrisrozacki on 08/03/2017.
 */
public class DataSourceCatalouge {
    final Map<String,Set<String>> databasesCollections  = ImmutableMap.of(
            "accepted-data", ImmutableSet.of("carerCircumstances",
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
                    "workCapabilityAssessmentDecision")
    );

            /*
    final String[][] databasesCollections{"accepted-data": [
        'carerCircumstances',
                'childcare',
                'childrenCircumstances',
                'earningsData',
                'employmentCircumstances',
                'healthAndDisabilityCircumstances',
                'housingCircumstances',
                'ineligiblePartner',
                'otherBenefit',
                'otherIncome',
                'overlappingBenefit',
                'personDetails',
                'terminalIllness',
                'workAndEarningsCircumstances',
                'workCapabilityAssessmentDecision'],
        'advances': [
        'advanceDebtPosition',
                'advanceGroup',
                'recoverableHardshipPayment'],
        'agent-core': [
        'agent',
                'agentToDo',
                'agentToDoArchive',
                'agentWorkGroupAllocation',
                'claimantHistory',
                'journalFeedback',
                'manualOverride',
                'systemWorkGroupAllocation',
                'team'],
        'appointments': [
        'appointment'],
        'calculator': [
        'calculation'],
        'claimant-history': [
        'claimHistoryEntry'],
        'core': [
        'carerDeclaration',
                'childcareDeclaration',
                'childDeclaration',
                'claimant',
                'claimantCommitment',
                'contract',
                'educationDeclaration',
                'healthAndDisabilityDeclaration',
                'healthDeclaration',
                'housingDeclaration',
                'journal',
                'periodOfSickness',
                'personalDetails',
                'statement',
                'survey',
                'toDo',
                'workAndEarningsDeclaration',
                'workGroupAllocation'],
        'matchingService': [
        'localUserMatchingData',
                'managementInformation',
                'matchingServiceRequests'],
        'organisation': [
        'deliveryUnitAddress',
                'organisation'],
        'penalties-and-deductions': [
        'debtPosition',
                'fraudPenalty',
                'outOfClaimDays',
                'sanction',
                'sanctionEscalation',
                'sanctionProgress',
                'sanctionTerminationDecision']
    }
    */
    public Set<String> getDatabases(){
        return databasesCollections.keySet();
    }

    public Set<String> getCollections(String database){
        return databasesCollections.get(database);
    }
}
