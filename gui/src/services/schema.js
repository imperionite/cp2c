import * as yup from "yup";

export const employeeSchema = yup.object().shape({
  employeeNumber: yup
    .string()
    .required("Employee Number is required")
    .matches(/^\d+$/, "Employee Number must be numeric"),
  lastName: yup.string().required("Last Name is required"),
  firstName: yup.string().required("First Name is required"),
  birthday: yup
    .string()
    .matches(
      /^(0[1-9]|1[0-2])\/(0[1-9]|[1-2][0-9]|3[0-1])\/\d{4}$/,
      "Birthday must be in MM/DD/YYYY format (e.g., 01/23/1990)"
    )
    .required("Birthday is required"),
  address: yup.string().required("Address is required"),
  phoneNumber: yup.string().required("Phone Number is required"),
  sssNumber: yup.string().required("SSS Number is required"),
  philhealthNumber: yup.string().required("PhilHealth Number is required"),
  tinNumber: yup.string().required("TIN Number is required"),
  pagibigNumber: yup.string().required("Pag-IBIG Number is required"),
  status: yup.string().required("Status is required"),
  position: yup.string().required("Position is required"),
  // Allow immediateSupervisor to be null or empty string if not applicable
  immediateSupervisor: yup
    .string()
    .nullable()
    .transform((value, originalValue) =>
      String(originalValue).trim() === "" ? null : value
    ),
  basicSalary: yup
    .number()
    .typeError("Basic Salary must be a number")
    .positive("Basic Salary must be positive")
    .required("Basic Salary is required"),
  riceSubsidy: yup
    .number()
    .typeError("Rice Subsidy must be a number")
    .min(0, "Rice Subsidy cannot be negative")
    .required("Rice Subsidy is required"),
  phoneAllowance: yup
    .number()
    .typeError("Phone Allowance must be a number")
    .min(0, "Phone Allowance cannot be negative")
    .required("Phone Allowance is required"),
  clothingAllowance: yup
    .number()
    .typeError("Clothing Allowance must be a number")
    .min(0, "Clothing Allowance cannot be negative")
    .required("Clothing Allowance is required"),
  grossSemiMonthlyRate: yup
    .number()
    .typeError("Gross Semi-monthly Rate must be a number")
    .min(0, "Gross Semi-monthly Rate cannot be negative")
    .required("Gross Semi-monthly Rate is required"),
  hourlyRate: yup
    .number()
    .typeError("Hourly Rate must be a number")
    .min(0, "Hourly Rate cannot be negative")
    .required("Hourly Rate is required"),
});


