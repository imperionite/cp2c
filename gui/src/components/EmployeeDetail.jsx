import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useAtomValue } from "jotai";

import {
  Typography,
  Button,
  Container,
  Card,
  CardContent,
  Box,
  Table,
  TableBody,
  TableCell,
  TableRow,
  TableContainer,
  Paper,
  Stack,
  Divider,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  InputAdornment,
  CircularProgress,
  MenuItem, 
  FormControl,
  InputLabel,
  Select, 
} from "@mui/material";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import SaveIcon from "@mui/icons-material/Save";
import CancelIcon from "@mui/icons-material/Cancel";
import CalculateIcon from "@mui/icons-material/Calculate";
import { toast } from "react-hot-toast";

import { useMutation, useQueryClient } from "@tanstack/react-query";

import { authAtom } from "../services/atoms";
import {
  useFetchByEmployeeNumber,
  useFetchMonthlyCutoffs,
  useFetchEmployeeMonthlySalary,
} from "../services/hooks";
import {
  updateEmployee as apiUpdateEmployee,
  deleteEmployee as apiDeleteEmployee,
} from "../services/http";
import { userKeys, employeeKeys } from "../services/queryKeyFactory";
import Loader from "./Loader";

const EmployeeDetail = () => {
  const { employeeNumber } = useParams();
  const auth = useAtomValue(authAtom);
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  // Fetch employee data using your useFetchByEmployeeNumber hook
  const {
    data: employeeData,
    isLoading: isLoadingEmployee,
    isError: isErrorEmployee,
    error: employeeError,
  } = useFetchByEmployeeNumber(auth?.token, employeeNumber);

  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({});
  const [openUpdateConfirm, setOpenUpdateConfirm] = useState(false);
  const [openDeleteConfirm, setOpenDeleteConfirm] = useState(false);

  // Salary Calculation State
  const [openSalaryModal, setOpenSalaryModal] = useState(false);
  const [selectedYearMonth, setSelectedYearMonth] = useState("");
  // Query for monthly cutoffs (enabled when modal is open)
  const {
    data: monthlyCutoffs,
    isLoading: isLoadingCutoffs,
    isError: isErrorCutoffs,
    error: cutoffsError,
  } = useFetchMonthlyCutoffs(auth?.token, openSalaryModal);
  // Query for salary calculation (enabled when modal is open and yearMonth selected)
  const {
    data: salaryCalculationResult,
    isLoading: isLoadingSalary,
    isError: isErrorSalary,
    error: salaryError,
  } = useFetchEmployeeMonthlySalary(
    auth?.token,
    employeeNumber,
    selectedYearMonth,
    openSalaryModal && !!selectedYearMonth
  );

  // Mutation hook for updating employee
  const {
    mutate: updateEmployeeMutation,
    isLoading: isUpdating,
    isError: isUpdateError,
    error: updateError,
  } = useMutation({
    mutationFn: (data) =>
      apiUpdateEmployee(employeeNumber, data.updatedData, auth?.token), 
    onSuccess: () => {
      toast.success("Employee updated successfully!");
      setIsEditing(false); // Exit edit mode
      // invalidate the query for that specific employee
      queryClient.invalidateQueries(employeeKeys.detail(employeeNumber));
      // invalidate the employee list if needed
      queryClient.invalidateQueries(employeeKeys.lists());
    },
    onError: (error) => {
      toast.error(error?.message || "Failed to update employee.");
    },
  });

  // Mutation hook for deleting employee
  const {
    mutate: deleteEmployeeMutation,
    isLoading: isDeleting,
    isError: isDeleteError,
    error: deleteError,
  } = useMutation({
    mutationFn: () => apiDeleteEmployee(employeeNumber, auth?.token), // Pass token
    onSuccess: () => {
      toast.success("Employee deleted successfully!");
      queryClient.invalidateQueries({ queryKey: userKeys.all });
      queryClient.invalidateQueries({ queryKey: employeeKeys.all });
      navigate("/employees");
    },
    onError: (error) => {
      toast.error(error?.message || "Failed to delete employee.");
    },
  });

  // Effect to initialize form data when employeeData changes or is first loaded
  useEffect(() => {
    if (employeeData) {
      setFormData({
        lastName: employeeData.lastName || "",
        firstName: employeeData.firstName || "",
        birthday: employeeData.birthday || "",
        address: employeeData.address || "",
        phoneNumber: employeeData.phoneNumber || "",
        sssNumber: employeeData.sssNumber || "",
        philhealthNumber: employeeData.philhealthNumber || "",
        tinNumber: employeeData.tinNumber || "",
        pagibigNumber: employeeData.pagibigNumber || "",
        status: employeeData.status || "",
        position: employeeData.position || "",
        immediateSupervisor: employeeData.immediateSupervisor || "",
        // Convert numbers to strings for TextField value, if they are numeric
        basicSalary: employeeData.basicSalary?.toString() || "0",
        riceSubsidy: employeeData.riceSubsidy?.toString() || "0",
        phoneAllowance: employeeData.phoneAllowance?.toString() || "0",
        clothingAllowance: employeeData.clothingAllowance?.toString() || "0",
        grossSemiMonthlyRate:
          employeeData.grossSemiMonthlyRate?.toString() || "0",
        hourlyRate: employeeData.hourlyRate?.toString() || "0",
      });
    }
  }, [employeeData]);

  // Handle form input changes for string fields
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  // Handle numeric input changes (for BigDecimal fields)
  const handleNumericChange = (e) => {
    const { name, value } = e.target;
    // Allow empty string or valid numbers for input.
    // Conversion to actual numbers (float/BigDecimal) happens when submitting.
    if (value === "" || /^-?\d*\.?\d*$/.test(value)) {
      setFormData((prev) => ({ ...prev, [name]: value }));
    }
  };

  // Open update confirmation dialog
  const handleUpdateConfirmOpen = () => {
    setOpenUpdateConfirm(true);
  };

  // Close update confirmation dialog
  const handleUpdateConfirmClose = () => {
    setOpenUpdateConfirm(false);
  };

  // Perform update API call via React Query mutation
  const handleUpdate = () => {
    setOpenUpdateConfirm(false); // Close dialog

    // Prepare data to send to backend, converting numeric strings back to numbers
    const dataToSend = {};
    for (const key in formData) {
      const value = formData[key];
      // Convert numeric fields from string to float/number for the backend
      if (
        key === "basicSalary" ||
        key === "riceSubsidy" ||
        key === "phoneAllowance" ||
        key === "clothingAllowance" ||
        key === "grossSemiMonthlyRate" ||
        key === "hourlyRate"
      ) {
        dataToSend[key] = value === "" ? null : parseFloat(value); // Send null for empty numbers
      } else if (typeof value === "string" && value.trim() === "") {
        dataToSend[key] = null; // Send null for empty strings for other fields
      } else {
        dataToSend[key] = value;
      }
    }

    updateEmployeeMutation({ employeeNumber, updatedData: dataToSend });
  };

  // Open delete confirmation dialog
  const handleDeleteConfirmOpen = () => {
    setOpenDeleteConfirm(true);
  };

  // Close delete confirmation dialog
  const handleDeleteConfirmClose = () => {
    setOpenDeleteConfirm(false);
  };

  // Perform delete API call via React Query mutation
  const handleDelete = () => {
    setOpenDeleteConfirm(false); // Close dialog
    deleteEmployeeMutation({ employeeNumber });
  };

  // NEW Salary Modal Handlers
  const handleOpenSalaryModal = () => {
    setOpenSalaryModal(true);
    // Optionally set the latest month as default if cutoffs are already loaded
    if (monthlyCutoffs && monthlyCutoffs.length > 0) {
      // Sort cutoffs to ensure latest is at the end, then select it
      const sortedCutoffs = [...monthlyCutoffs].sort((a, b) => {
        // Assuming yearMonth is in "YYYY-MM" format, string comparison works
        return a.yearMonth.localeCompare(b.yearMonth);
      });
      setSelectedYearMonth(sortedCutoffs[sortedCutoffs.length - 1].yearMonth);
    }
  };

  const handleCloseSalaryModal = () => {
    setOpenSalaryModal(false);
    setSelectedYearMonth(""); // Reset selected month
    // salaryCalculationResult state will be cleared by query's default behavior (staleTime: 0)
    // or can be explicitly reset here if needed: setSalaryCalculationResult(null);
  };

  const handleYearMonthChange = (event) => {
    setSelectedYearMonth(event.target.value);
  };

  // Initial loading state for employee data
  if (isLoadingEmployee) return <Loader />;

  // Error state for employee data fetch (toast handled by useQuery onError)
  if (isErrorEmployee) {
    return (
      <Container maxWidth="md" sx={{ mt: 6 }}>
        <Paper
          elevation={3}
          sx={{
            p: 4,
            borderRadius: 2,
            bgcolor: "error.light",
            color: "error.contrastText",
          }}
        >
          <Typography variant="h6" component="p" gutterBottom>
            Error:{" "}
            {employeeError?.message || "Failed to load employee details."}
          </Typography>
          <Button
            startIcon={<ArrowBackIcon />}
            variant="contained"
            color="primary"
            onClick={() => navigate(-1)}
            sx={{ mt: 3 }}
          >
            Back to List
          </Button>
        </Paper>
      </Container>
    );
  }

  // If employeeData is null/undefined after loading, assume not found
  if (!employeeData) {
    return (
      <Container maxWidth="md" sx={{ mt: 6 }}>
        <Paper
          elevation={3}
          sx={{
            p: 4,
            borderRadius: 2,
            bgcolor: "warning.light",
            color: "warning.contrastText",
          }}
        >
          <Typography variant="h6" component="p" gutterBottom>
            Employee data not found for employee number: {employeeNumber}.
          </Typography>
          <Button
            startIcon={<ArrowBackIcon />}
            variant="contained"
            color="primary"
            onClick={() => navigate(-1)}
            sx={{ mt: 3 }}
          >
            Back to List
          </Button>
        </Paper>
      </Container>
    );
  }

  // Employee data is now in `formData` when editing, or `employeeData` when viewing
  const currentEmployee = isEditing ? formData : employeeData;

  // Determine if the currently authenticated user is viewing their own employee record
  const isCurrentUserEmployee = auth?.username === `user-${employeeNumber}`;

  // Helper for rendering table rows within cards
  const renderInfoRow = (label, value) => (
    <TableRow sx={{ "&:last-child td, &:last-child th": { border: 0 } }}>
      <TableCell
        component="th"
        scope="row"
        sx={{ fontWeight: "bold", width: "35%", py: 1 }}
      >
        {label}:
      </TableCell>
      <TableCell sx={{ py: 1 }}>{value || "N/A"}</TableCell>
    </TableRow>
  );

  // Helper for formatting currency values
  const formatCurrency = (value) => {
    if (value === null || value === undefined || value === "") return "N/A";
    // Ensure value is a number for toLocaleString
    const numValue = typeof value === "string" ? parseFloat(value) : value;
    if (isNaN(numValue)) return "N/A";
    return `₱${numValue.toLocaleString(undefined, {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    })}`;
  };

  // Helper for formatting hours
  const formatHours = (value) => {
    if (value === null || value === undefined) return "N/A";
    const numValue = typeof value === "string" ? parseFloat(value) : value;
    if (isNaN(numValue)) return "N/A";
    return `${numValue.toFixed(2)} hours`;
  };

  if (isUpdateError) toast.error(updateError.message);
  if (isDeleteError) toast.error(deleteError.message);
  if (isErrorCutoffs)
    toast.error(cutoffsError?.message || "Failed to load monthly cutoffs.");
  if (isErrorSalary)
    toast.error(salaryError?.message || "Failed to calculate salary.");

  return (
    <Container maxWidth="lg" sx={{ mt: 4, mb: 4 }}>
      {/* Back Button */}
      <Button
        startIcon={<ArrowBackIcon />}
        variant="outlined"
        onClick={() => navigate(-1)}
        sx={{
          mb: 4,
          borderRadius: 2,
          px: 3,
          py: 1.5,
          textTransform: "none",
          fontWeight: "medium",
          transition: "all 0.3s ease-in-out",
          "&:hover": {
            borderColor: "primary.dark",
            bgcolor: "primary.light",
            color: "primary.contrastText",
            boxShadow: 3,
            transform: "translateY(-2px)",
          },
        }}
      >
        Back to Employee List
      </Button>

      {/* Header Section */}
      <Card
        elevation={6}
        sx={{
          mb: 4,
          borderRadius: 3,
          p: 4,
          boxShadow: "0 8px 20px rgba(0,0,0,0.1)",
          borderLeft: "6px solid",
          borderColor: "primary.main",
          transition: "transform 0.3s ease-in-out",
          "&:hover": {
            transform: "translateY(-5px)",
          },
        }}
      >
        <CardContent sx={{ p: 0, "&:last-child": { pb: 0 } }}>
          <Box
            display="flex"
            justifyContent="space-between"
            alignItems="center"
            mb={1}
          >
            <Typography
              variant="h4"
              component="h1"
              sx={{ fontWeight: "bold", color: "text.primary", mb: 1 }}
            >
              Employee #{currentEmployee.employeeNumber} -{" "}
              {currentEmployee.firstName} {currentEmployee.lastName}
            </Typography>
            {!isEditing && (
              <Stack direction="row" spacing={1}>
                <Button
                  variant="contained"
                  color="primary"
                  startIcon={<EditIcon />}
                  onClick={() => setIsEditing(true)}
                  sx={{ borderRadius: 2, textTransform: "none", px: 2, py: 1 }}
                >
                  Edit
                </Button>
                <Button
                  variant="outlined"
                  color="error"
                  startIcon={<DeleteIcon />}
                  onClick={handleDeleteConfirmOpen}
                  sx={{ borderRadius: 2, textTransform: "none", px: 2, py: 1 }}
                  disabled={isDeleting}
                >
                  {isDeleting ? (
                    <CircularProgress size={24} color="inherit" />
                  ) : (
                    "Delete"
                  )}
                </Button>
                {/* NEW Calculate Salary Button */}
                <Button
                  variant="outlined"
                  color="success"
                  startIcon={<CalculateIcon />}
                  onClick={handleOpenSalaryModal}
                  sx={{ borderRadius: 2, textTransform: "none", px: 2, py: 1 }}
                >
                  Calculate Salary
                </Button>
              </Stack>
            )}
          </Box>
          <Typography
            variant="body1"
            color="success.main"
            sx={{ fontWeight: "medium", letterSpacing: 0.5 }}
          >
            Authenticated as:{" "}
            <Typography
              component="span"
              variant="body1"
              sx={{ fontWeight: "bold", color: "success.dark" }}
            >
              {auth?.username}
            </Typography>
          </Typography>
          {isCurrentUserEmployee && (
            <Typography
              variant="body2"
              color="info.main"
              sx={{ mt: 1, fontStyle: "italic" }}
            >
              You are viewing your own detailed employee record.
            </Typography>
          )}
        </CardContent>
      </Card>

      {/* Employee Details Sections (Conditional Rendering for Edit/View) */}
      <Stack
        component="form" // Wrap in form to allow submission
        onSubmit={(e) => {
          e.preventDefault();
          handleUpdateConfirmOpen();
        }} // Handle submit for update
        direction={{ xs: "column", md: "row" }}
        spacing={3}
        flexWrap="wrap"
        useFlexGap
      >
        {/* Personal Information Section */}
        <Box sx={{ flex: "1 1 100%", maxWidth: { md: "calc(50% - 12px)" } }}>
          <Card elevation={3} sx={{ borderRadius: 3, p: 3 }}>
            <CardContent sx={{ p: 0, "&:last-child": { pb: 0 } }}>
              <Typography
                variant="h6"
                sx={{ fontWeight: "bold", color: "text.primary", mb: 2 }}
              >
                Personal Information
              </Typography>
              {isEditing ? (
                <Stack spacing={2}>
                  <TextField
                    label="First Name"
                    name="firstName"
                    value={formData.firstName}
                    onChange={handleChange}
                    fullWidth
                    required
                    size="small"
                    variant="outlined"
                    sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
                  />
                  <TextField
                    label="Last Name"
                    name="lastName"
                    value={formData.lastName}
                    onChange={handleChange}
                    fullWidth
                    required
                    size="small"
                    variant="outlined"
                    sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
                  />
                  <TextField
                    label="Birthday (MM/DD/YYYY)"
                    name="birthday"
                    value={formData.birthday}
                    onChange={handleChange}
                    fullWidth
                    size="small"
                    variant="outlined"
                    placeholder="e.g., 01/23/1990"
                    sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
                  />
                  <TextField
                    label="Address"
                    name="address"
                    value={formData.address}
                    onChange={handleChange}
                    fullWidth
                    multiline
                    minRows={2}
                    size="small"
                    variant="outlined"
                    sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
                  />
                  <TextField
                    label="Phone Number"
                    name="phoneNumber"
                    value={formData.phoneNumber}
                    onChange={handleChange}
                    fullWidth
                    size="small"
                    variant="outlined"
                    sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
                  />
                </Stack>
              ) : (
                <TableContainer component={Paper} elevation={0}>
                  <Table size="small">
                    <TableBody>
                      <TableRow>
                        <TableCell sx={{ fontWeight: "bold", width: "35%" }}>
                          Full Name:
                        </TableCell>
                        <TableCell>
                          {currentEmployee.firstName} {currentEmployee.lastName}
                        </TableCell>
                      </TableRow>
                      {renderInfoRow("Birthday", currentEmployee.birthday)}
                      {renderInfoRow("Address", currentEmployee.address)}
                      {renderInfoRow(
                        "Phone Number",
                        currentEmployee.phoneNumber
                      )}
                    </TableBody>
                  </Table>
                </TableContainer>
              )}
            </CardContent>
          </Card>
        </Box>

        {/* Government IDs Section */}
        <Box sx={{ flex: "1 1 100%", maxWidth: { md: "calc(50% - 12px)" } }}>
          <Card elevation={3} sx={{ borderRadius: 3, p: 3 }}>
            <CardContent sx={{ p: 0, "&:last-child": { pb: 0 } }}>
              <Typography
                variant="h6"
                sx={{ fontWeight: "bold", color: "text.primary", mb: 2 }}
              >
                Government IDs
              </Typography>
              {isEditing ? (
                <Stack spacing={2}>
                  <TextField
                    label="SSS Number"
                    name="sssNumber"
                    value={formData.sssNumber}
                    onChange={handleChange}
                    fullWidth
                    size="small"
                    variant="outlined"
                    sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
                  />
                  <TextField
                    label="PhilHealth Number"
                    name="philhealthNumber"
                    value={formData.philhealthNumber}
                    onChange={handleChange}
                    fullWidth
                    size="small"
                    variant="outlined"
                    sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
                  />
                  <TextField
                    label="TIN Number"
                    name="tinNumber"
                    value={formData.tinNumber}
                    onChange={handleChange}
                    fullWidth
                    size="small"
                    variant="outlined"
                    sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
                  />
                  <TextField
                    label="Pag-IBIG Number"
                    name="pagibigNumber"
                    value={formData.pagibigNumber}
                    onChange={handleChange}
                    fullWidth
                    size="small"
                    variant="outlined"
                    sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
                  />
                </Stack>
              ) : (
                <TableContainer component={Paper} elevation={0}>
                  <Table size="small">
                    <TableBody>
                      {renderInfoRow("SSS", currentEmployee.sssNumber)}
                      {renderInfoRow(
                        "PhilHealth",
                        currentEmployee.philhealthNumber
                      )}
                      {renderInfoRow("TIN", currentEmployee.tinNumber)}
                      {renderInfoRow("Pag-IBIG", currentEmployee.pagibigNumber)}
                    </TableBody>
                  </Table>
                </TableContainer>
              )}
            </CardContent>
          </Card>
        </Box>

        {/* Compensation Section */}
        <Box sx={{ flex: "1 1 100%", maxWidth: { md: "calc(50% - 12px)" } }}>
          <Card elevation={3} sx={{ borderRadius: 3, p: 3 }}>
            <CardContent sx={{ p: 0, "&:last-child": { pb: 0 } }}>
              <Typography
                variant="h6"
                sx={{ fontWeight: "bold", color: "text.primary", mb: 2 }}
              >
                Compensation
              </Typography>
              {isEditing ? (
                <Stack spacing={2}>
                  <TextField
                    label="Basic Salary"
                    name="basicSalary"
                    value={formData.basicSalary}
                    onChange={handleNumericChange}
                    fullWidth
                    type="number"
                    size="small"
                    variant="outlined"
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">₱</InputAdornment>
                      ),
                      inputProps: { step: "0.01" },
                    }}
                    sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
                  />
                  <TextField
                    label="Rice Subsidy"
                    name="riceSubsidy"
                    value={formData.riceSubsidy}
                    onChange={handleNumericChange}
                    fullWidth
                    type="number"
                    size="small"
                    variant="outlined"
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">₱</InputAdornment>
                      ),
                      inputProps: { step: "0.01" },
                    }}
                    sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
                  />
                  <TextField
                    label="Phone Allowance"
                    name="phoneAllowance"
                    value={formData.phoneAllowance}
                    onChange={handleNumericChange}
                    fullWidth
                    type="number"
                    size="small"
                    variant="outlined"
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">₱</InputAdornment>
                      ),
                      inputProps: { step: "0.01" },
                    }}
                    sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
                  />
                  <TextField
                    label="Clothing Allowance"
                    name="clothingAllowance"
                    value={formData.clothingAllowance}
                    onChange={handleNumericChange}
                    fullWidth
                    type="number"
                    size="small"
                    variant="outlined"
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">₱</InputAdornment>
                      ),
                      inputProps: { step: "0.01" },
                    }}
                    sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
                  />
                  <TextField
                    label="Hourly Rate"
                    name="hourlyRate"
                    value={formData.hourlyRate}
                    onChange={handleNumericChange}
                    fullWidth
                    type="number"
                    size="small"
                    variant="outlined"
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">₱</InputAdornment>
                      ),
                      inputProps: { step: "0.01" },
                    }}
                    sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
                  />
                  <TextField
                    label="Gross Semi-Monthly Rate"
                    name="grossSemiMonthlyRate"
                    value={formData.grossSemiMonthlyRate}
                    onChange={handleNumericChange}
                    fullWidth
                    type="number"
                    size="small"
                    variant="outlined"
                    InputProps={{
                      startAdornment: (
                        <InputAdornment position="start">₱</InputAdornment>
                      ),
                      inputProps: { step: "0.01" },
                    }}
                    sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
                  />
                </Stack>
              ) : (
                <TableContainer component={Paper} elevation={0}>
                  <Table size="small">
                    <TableBody>
                      {renderInfoRow(
                        "Basic Salary",
                        formatCurrency(currentEmployee.basicSalary)
                      )}
                      {renderInfoRow(
                        "Rice Subsidy",
                        formatCurrency(currentEmployee.riceSubsidy)
                      )}
                      {renderInfoRow(
                        "Phone Allowance",
                        formatCurrency(currentEmployee.phoneAllowance)
                      )}
                      {renderInfoRow(
                        "Clothing Allowance",
                        formatCurrency(currentEmployee.clothingAllowance)
                      )}
                      {renderInfoRow(
                        "Hourly Rate",
                        formatCurrency(currentEmployee.hourlyRate)
                      )}
                      {renderInfoRow(
                        "Gross Semi-Monthly Rate",
                        formatCurrency(currentEmployee.grossSemiMonthlyRate)
                      )}
                    </TableBody>
                  </Table>
                </TableContainer>
              )}
            </CardContent>
          </Card>
        </Box>

        {/* Position and Status Section */}
        <Box sx={{ flex: "1 1 100%", maxWidth: { md: "calc(50% - 12px)" } }}>
          <Card elevation={3} sx={{ borderRadius: 3, p: 3 }}>
            <CardContent sx={{ p: 0, "&:last-child": { pb: 0 } }}>
              <Typography
                variant="h6"
                sx={{ fontWeight: "bold", color: "text.primary", mb: 2 }}
              >
                Employment Details
              </Typography>
              {isEditing ? (
                <Stack spacing={2}>
                  <TextField
                    label="Status"
                    name="status"
                    value={formData.status}
                    onChange={handleChange}
                    fullWidth
                    size="small"
                    variant="outlined"
                    sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
                  />
                  <TextField
                    label="Position"
                    name="position"
                    value={formData.position}
                    onChange={handleChange}
                    fullWidth
                    size="small"
                    variant="outlined"
                    sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
                  />
                  <TextField
                    label="Immediate Supervisor"
                    name="immediateSupervisor"
                    value={formData.immediateSupervisor}
                    onChange={handleChange}
                    fullWidth
                    size="small"
                    variant="outlined"
                    sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
                  />
                </Stack>
              ) : (
                <TableContainer component={Paper} elevation={0}>
                  <Table size="small">
                    <TableBody>
                      {renderInfoRow("Status", currentEmployee.status)}
                      {renderInfoRow("Position", currentEmployee.position)}
                      {renderInfoRow(
                        "Immediate Supervisor",
                        currentEmployee.immediateSupervisor
                      )}
                    </TableBody>
                  </Table>
                </TableContainer>
              )}
            </CardContent>
          </Card>
        </Box>

        {/* User Account Info Section (Conditional based on authenticated user) */}
        {isCurrentUserEmployee && (
          <Box sx={{ flex: "1 1 100%" }}>
            {" "}
            {/* Takes full width */}
            <Card elevation={3} sx={{ borderRadius: 3, p: 3 }}>
              <CardContent sx={{ p: 0, "&:last-child": { pb: 0 } }}>
                <Typography
                  variant="h6"
                  sx={{ fontWeight: "bold", color: "text.primary", mb: 2 }}
                >
                  Your User Account Information
                </Typography>
                <TableContainer component={Paper} elevation={0}>
                  <Table size="small">
                    <TableBody>
                      {renderInfoRow("Your User ID", auth?.userId)}
                      {renderInfoRow(
                        "Associated Employee Number",
                        currentEmployee.employeeNumber
                      )}
                      {renderInfoRow("Your Username", auth?.username)}
                    </TableBody>
                  </Table>
                </TableContainer>
              </CardContent>
            </Card>
          </Box>
        )}
      </Stack>

      {/* Action Buttons (Save/Cancel in Edit Mode) */}
      {isEditing && (
        <Box
          sx={{
            display: "flex",
            justifyContent: "flex-end",
            gap: 2,
            mt: 4,
            mb: 4,
          }}
        >
          <Button
            variant="outlined"
            color="secondary"
            startIcon={<CancelIcon />}
            onClick={() => setIsEditing(false)}
            sx={{ borderRadius: 2, textTransform: "none", px: 3, py: 1.5 }}
          >
            Cancel
          </Button>
          <Button
            variant="contained"
            color="primary"
            startIcon={
              isUpdating ? (
                <CircularProgress size={20} color="inherit" />
              ) : (
                <SaveIcon />
              )
            }
            onClick={handleUpdateConfirmOpen}
            sx={{ borderRadius: 2, textTransform: "none", px: 3, py: 1.5 }}
            disabled={isUpdating}
            type="submit" // Associate with form
          >
            {isUpdating ? "Saving..." : "Save Changes"}
          </Button>
        </Box>
      )}

      {/* Divider */}
      <Divider sx={{ my: 3 }} />

      {/* Update Confirmation Dialog */}
      <Dialog
        open={openUpdateConfirm}
        onClose={handleUpdateConfirmClose}
        maxWidth="xs"
        PaperProps={{ sx: { borderRadius: 2 } }}
      >
        <DialogTitle sx={{ bgcolor: "primary.main", color: "white" }}>
          Confirm Update
        </DialogTitle>
        <DialogContent dividers sx={{ p: 3 }}>
          <Typography>
            Are you sure you want to update this employee's information?
          </Typography>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button
            onClick={handleUpdateConfirmClose}
            color="secondary"
            variant="outlined"
            sx={{ borderRadius: 1 }}
          >
            Cancel
          </Button>
          <Button
            onClick={handleUpdate}
            color="primary"
            variant="contained"
            sx={{ borderRadius: 1 }}
            disabled={isUpdating}
          >
            {isUpdating ? <CircularProgress size={24} /> : "Update"}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog
        open={openDeleteConfirm}
        onClose={handleDeleteConfirmClose}
        maxWidth="xs"
        PaperProps={{ sx: { borderRadius: 2 } }}
      >
        <DialogTitle sx={{ bgcolor: "error.main", color: "white" }}>
          Confirm Delete
        </DialogTitle>
        <DialogContent dividers sx={{ p: 3 }}>
          <Typography>
            Are you sure you want to permanently delete this employee? This
            action cannot be undone.
          </Typography>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button
            onClick={handleDeleteConfirmClose}
            color="secondary"
            variant="outlined"
            sx={{ borderRadius: 1 }}
          >
            Cancel
          </Button>
          <Button
            onClick={handleDelete}
            color="error"
            variant="contained"
            sx={{ borderRadius: 1 }}
            disabled={isDeleting}
          >
            {isDeleting ? <CircularProgress size={24} /> : "Delete"}
          </Button>
        </DialogActions>
      </Dialog>

      {/* NEW Salary Calculation Modal */}
      <Dialog
        open={openSalaryModal}
        onClose={handleCloseSalaryModal}
        maxWidth="sm"
        fullWidth
        PaperProps={{ sx: { borderRadius: 2 } }}
      >
        <DialogTitle sx={{ bgcolor: "success.main", color: "white" }}>
          Calculate Monthly Salary for Employee #{employeeNumber}
        </DialogTitle>
        <DialogContent dividers sx={{ p: 3 }}>
          <FormControl fullWidth sx={{ mb: 3 }}>
            <InputLabel id="year-month-select-label">Select Month</InputLabel>
            <Select
              labelId="year-month-select-label"
              value={selectedYearMonth}
              label="Select Month"
              onChange={handleYearMonthChange}
              disabled={isLoadingCutoffs}
              sx={{ borderRadius: 2 }}
            >
              {isLoadingCutoffs ? (
                <MenuItem disabled>
                  <CircularProgress size={20} /> Loading Months...
                </MenuItem>
              ) : isErrorCutoffs ? (
                <MenuItem disabled>Error loading months</MenuItem>
              ) : monthlyCutoffs && monthlyCutoffs.length > 0 ? (
                // Sort months from oldest to newest for chronological display
                [...monthlyCutoffs]
                  .sort((a, b) => a.yearMonth.localeCompare(b.yearMonth))
                  .map((cutoff) => (
                    <MenuItem key={cutoff.yearMonth} value={cutoff.yearMonth}>
                      {cutoff.yearMonth} ({cutoff.startDate} - {cutoff.endDate})
                    </MenuItem>
                  ))
              ) : (
                <MenuItem disabled>No attendance data available</MenuItem>
              )}
            </Select>
          </FormControl>

          {isLoadingSalary && (
            <Box
              display="flex"
              justifyContent="center"
              alignItems="center"
              minHeight={200}
            >
              <CircularProgress />
              <Typography variant="h6" sx={{ ml: 2 }}>
                Calculating Salary...
              </Typography>
            </Box>
          )}

          {!isLoadingSalary && salaryError && (
            <Box
              sx={{
                p: 2,
                bgcolor: "error.light",
                borderRadius: 2,
                color: "error.contrastText",
              }}
            >
              <Typography variant="body1">
                Error calculating salary: {salaryError.message}
              </Typography>
            </Box>
          )}

          {!isLoadingSalary && !salaryError && salaryCalculationResult && (
            <Box>
              <Typography variant="h6" gutterBottom sx={{ fontWeight: "bold" }}>
                Calculation Result:
              </Typography>
              <TableContainer
                component={Paper}
                elevation={1}
                sx={{ borderRadius: 2 }}
              >
                <Table size="small">
                  <TableBody>
                    {renderInfoRow(
                      "Worked Hours",
                      formatHours(salaryCalculationResult.monthlyWorkedHours)
                    )}
                    <TableRow>
                      <TableCell
                        colSpan={2}
                        sx={{
                          fontWeight: "bold",
                          bgcolor: "action.hover",
                          py: 1,
                        }}
                      >
                        <Typography
                          variant="subtitle1"
                          sx={{ fontWeight: "bold" }}
                        >
                          Gross Salary
                        </Typography>
                      </TableCell>
                    </TableRow>
                    {renderInfoRow(
                      "Gross Monthly Salary",
                      formatCurrency(salaryCalculationResult.grossMonthlySalary)
                    )}
                    <TableRow>
                      <TableCell
                        colSpan={2}
                        sx={{
                          fontWeight: "bold",
                          bgcolor: "action.hover",
                          py: 1,
                        }}
                      >
                        <Typography
                          variant="subtitle1"
                          sx={{ fontWeight: "bold" }}
                        >
                          Deductions
                        </Typography>
                      </TableCell>
                    </TableRow>
                    {renderInfoRow(
                      "SSS Deduction",
                      formatCurrency(
                        salaryCalculationResult.monthlySssDeduction
                      )
                    )}
                    {renderInfoRow(
                      "PhilHealth Deduction",
                      formatCurrency(
                        salaryCalculationResult.monthlyPhilhealthDeduction
                      )
                    )}
                    {renderInfoRow(
                      "Pag-IBIG Deduction",
                      formatCurrency(
                        salaryCalculationResult.monthlyPagibigDeduction
                      )
                    )}
                    {renderInfoRow(
                      "Withholding Tax",
                      formatCurrency(
                        salaryCalculationResult.monthlyWithholdingTax
                      )
                    )}
                    <TableRow>
                      <TableCell
                        colSpan={2}
                        sx={{
                          fontWeight: "bold",
                          bgcolor: "warning.light",
                          py: 1,
                        }}
                      >
                        <Typography
                          variant="subtitle1"
                          sx={{
                            fontWeight: "bold",
                            color: "warning.contrastText",
                          }}
                        >
                          Total Deductions
                        </Typography>
                      </TableCell>
                    </TableRow>
                    {renderInfoRow(
                      "Total Deductions",
                      formatCurrency(salaryCalculationResult.totalDeductions)
                    )}
                    <TableRow>
                      <TableCell
                        colSpan={2}
                        sx={{
                          fontWeight: "bold",
                          bgcolor: "success.light",
                          py: 1,
                        }}
                      >
                        <Typography
                          variant="subtitle1"
                          sx={{
                            fontWeight: "bold",
                            color: "success.contrastText",
                          }}
                        >
                          Net Salary
                        </Typography>
                      </TableCell>
                    </TableRow>
                    {renderInfoRow(
                      "Net Monthly Salary",
                      formatCurrency(salaryCalculationResult.netMonthlySalary)
                    )}
                  </TableBody>
                </Table>
              </TableContainer>
            </Box>
          )}
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button
            onClick={handleCloseSalaryModal}
            color="primary"
            variant="outlined"
            sx={{ borderRadius: 1 }}
          >
            Close
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default EmployeeDetail;
