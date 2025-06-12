import { useQuery } from "@tanstack/react-query";
import { employeeKeys, salaryKeys } from "./queryKeyFactory";
import { getEmployeePartialDetails, getEmployeeByEmployeeNumber, fetchEmployeeMonthlySalary, fetchMonthlyCutoffs } from "./http";

export const useEmployeePartialDetails = (accessToken) => {
  return useQuery({
    queryKey: employeeKeys.detail("partialDetails"),
    queryFn: getEmployeePartialDetails,
    staleTime: 5 * 60 * 1000,
    retry: 1,
    enabled: !!accessToken,
  });
};

export const useFetchByEmployeeNumber = (accessToken, employeeNumber) => {
  return useQuery({
    queryKey: employeeKeys.detail(employeeNumber),
    queryFn: () => getEmployeeByEmployeeNumber(employeeNumber),
    staleTime: 5 * 60 * 1000,
    retry: 1,
    enabled: !!accessToken && !!employeeNumber,
  });
};

export function useFetchMonthlyCutoffs(accessToken, enabled) {
  return useQuery({
    queryKey: salaryKeys.monthlyCutoffs(),
    queryFn: fetchMonthlyCutoffs,
    enabled: !!accessToken && enabled, // Only fetch if token is available and enabled
    staleTime: Infinity, // Cutoffs are likely static or change infrequently
    onError: (error) => {
      console.error("Error fetching monthly cutoffs:", error);
    },
  });
}


export function useFetchEmployeeMonthlySalary(accessToken, employeeNumber, yearMonth, enabled) {
  return useQuery({
    queryKey: salaryKeys.employeeSalary(employeeNumber, yearMonth),
    queryFn: () => fetchEmployeeMonthlySalary(employeeNumber, yearMonth),
    enabled: !!accessToken && !!employeeNumber && !!yearMonth && enabled,
    staleTime: 0, // Always refetch as salary calculation might depend on live attendance data
    onError: (error) => {
      console.error(`Error calculating salary for ${employeeNumber} - ${yearMonth}:`, error);
    },
  });
}
