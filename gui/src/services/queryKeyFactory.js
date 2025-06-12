export const userKeys = {
  all: ["users"],
  lists: () => [...userKeys.all, "list"],
  details: () => [...userKeys.all, "detail"],
  detail: (id) => [...userKeys.details(), id],
};

export const employeeKeys = {
  all: ["employees"],
  lists: () => [...employeeKeys.all, "list"],
  detail: (employeeNumber) => [...employeeKeys.all, "detail", employeeNumber],
  partialDetails: () => [...employeeKeys.all, "partialDetails"],
  fetchByEmployeeNum: () => [...employeeKeys.all, "fetchByEmployeeNum"]
};

export const salaryKeys = {
  all: ["salary"],
  monthlyCutoffs: () => [...salaryKeys.all, "monthlyCutoffs"],
  employeeSalary: (employeeNumber, yearMonth) => [...salaryKeys.all, "employee", employeeNumber, yearMonth],
};
