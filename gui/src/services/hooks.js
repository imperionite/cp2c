import { useQuery } from "@tanstack/react-query";
import { employeeKeys } from "./queryKeyFactory";
import { getEmployeePartialDetails, getEmployeeByEmployeeNumber } from "./http";

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
