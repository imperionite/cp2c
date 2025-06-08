export const userKeys = {
  all: ["users"],
  lists: () => [...userKeys.all, "list"],
  list: (filters) => [...userKeys.lists(), { filters }],
  details: () => [...userKeys.all, "detail"],
  detail: (id) => [...userKeys.details(), id],
  profile: () => [...userKeys.all, "profile"],
};

export const employeeKeys = {
  all: ["employees"],
  lists: () => [...employeeKeys.all, "list"],
  list: (filters) => [...employeeKeys.lists(), { filters }],
  details: () => [...employeeKeys.all, "detail"],
  detail: (id) => [...employeeKeys.details(), id],
  partialDetails: () => [...employeeKeys.all, "partialDetails"],
  fetchByEmployeeNum: () => [...employeeKeys.all, "fetchByEmployeeNum"],
  basicInfo: () => [...employeeKeys.all, "basicInfo"],
};