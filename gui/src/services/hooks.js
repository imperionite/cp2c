import { useQuery } from "@tanstack/react-query";
import { userKeys, employeeKeys } from "./queryKeyFactory";
import {
  getUserProfile,
  getEmployeePartialDetails,
  getEmployeeBasicInfo,
  getEmployeeByEmployeeNumber,
  getMonthlyCutoffs,
  getMonthlyNet
} from "./http";

export const useUserProfile = (accessToken) => {
  return useQuery({
    queryKey: userKeys.detail("profile"), // defined related key for invallidate or caching
    queryFn: getUserProfile, // your async function that fetches user profile
    // enabled, // enables data fetching on condition
    staleTime: 5 * 60 * 1000, // optional: cache data for 5 minutes
    retry: 1, // optional: retry once on failure
    enabled: !!accessToken, // enable only if auth accessToken is truthy
  });
};

export const useEmployeePartialDetails = (accessToken) => {
  return useQuery({
    queryKey: employeeKeys.detail("partialDetails"),
    queryFn: getEmployeePartialDetails,
    staleTime: 5 * 60 * 1000,
    retry: 1,
    enabled: !!accessToken,
  });
};

export const useEmployeeBasicInfo = (accessToken) => {
  return useQuery({
    queryKey: employeeKeys.detail("basicInfo"),
    queryFn: getEmployeeBasicInfo,
    staleTime: 5 * 60 * 1000,
    retry: 1,
    enabled: !!accessToken,
  });
};

export const useFetchByEmployeeNumber = (accessToken, employeeNumber) => {
  return useQuery({
    queryKey: ["fetchByEmployeeNum", employeeNumber],
    queryFn: () => getEmployeeByEmployeeNumber(employeeNumber),
    staleTime: 5 * 60 * 1000,
    retry: 1,
    enabled: !!accessToken && !!employeeNumber,
  });
};

export const useFetchMonthlyCutOff = (accessToken) => {
  return useQuery({
    queryKey: ["monthlyCutoffs"], // More specific query key
    queryFn: () => getMonthlyCutoffs(), // Corrected: function that calls getMonthlyCutoffs
    staleTime: 5 * 60 * 1000,
    retry: 1,
    enabled: !!accessToken, 
  });
};

export const useMonthlyNet = (accessToken, employeeNumber, yearMonth, enabled) => {
  return useQuery({
    queryKey: ["monthlyNet", employeeNumber, yearMonth], // More specific query key
    queryFn: () => getMonthlyNet(employeeNumber, yearMonth), // Corrected: function that calls getMonthlyNet
    staleTime: 5 * 60 * 1000,
    retry: 1,
    // Only enabled if accessToken, employeeNumber, yearMonth are available AND the 'enabled' prop is true
    enabled: !!accessToken && !!employeeNumber && !!yearMonth && enabled,
  });
};