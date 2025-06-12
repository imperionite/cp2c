import React, { useState, useEffect, lazy } from "react";
import { useAtom, useAtomValue } from "jotai";
import {
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Button,
  Box,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Stack,
  InputAdornment,
  CircularProgress,
  Typography
} from "@mui/material";
import PersonAddAlt1Icon from "@mui/icons-material/PersonAddAlt1";
import { toast } from "react-hot-toast";
import { useNavigate } from "react-router-dom";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";

import { employeesAtom } from "../services/atoms";
import { useEmployeePartialDetails } from "../services/hooks";
import { authAtom } from "../services/atoms";
import { employeeSchema } from "../services/schema";
import { createEmployee as apiCreateEmployee } from "../services/http";
import { userKeys, employeeKeys } from "../services/queryKeyFactory";

const Loader = lazy(() => import("./Loader"));

const EmployeeList = () => {
  const [employees, setEmployees] = useAtom(employeesAtom);
  const auth = useAtomValue(authAtom);
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [openCreateModal, setOpenCreateModal] = useState(false);

  const {
    data: employeesData,
    isLoading,
    isError,
    error,
  } = useEmployeePartialDetails(auth?.token);

  // React Hook Form setup for create employee form
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({
    resolver: yupResolver(employeeSchema),
    defaultValues: {
      // Set default values for all fields
      employeeNumber: "",
      lastName: "",
      firstName: "",
      birthday: "",
      address: "",
      phoneNumber: "",
      sssNumber: "",
      philhealthNumber: "",
      tinNumber: "",
      pagibigNumber: "",
      status: "",
      position: "",
      immediateSupervisor: null, // Default to null for nullable field
      basicSalary: 0,
      riceSubsidy: 0,
      phoneAllowance: 0,
      clothingAllowance: 0,
      grossSemiMonthlyRate: 0,
      hourlyRate: 0,
    },
  });

  // Mutation hook for creating a new employee
  const { mutate: createEmployeeMutation, isLoading: isCreating } = useMutation(
    {
      mutationFn: (newEmployeeData) =>
        apiCreateEmployee(newEmployeeData, auth?.token),
      onSuccess: () => {
        toast.success("Employee created successfully!");
        handleCloseCreateModal(); // Close modal on success
        queryClient.invalidateQueries(employeeKeys.lists()); // Invalidate employee list to refetch
        queryClient.invalidateQueries(employeeKeys.all); // Invalidate all employees related queries
        queryClient.invalidateQueries(userKeys.all);
      },
      onError: (err) => {
        toast.error(err.message || "Failed to create employee.");
      },
    }
  );

  useEffect(() => {
    if (employeesData) {
      setEmployees(employeesData);
    }
  }, [employeesData, setEmployees]);

  if (isLoading) return <Loader />;
  if (isError) return toast.error(error.message);

  // Define columns based on isAdmin
  const columns = [
    "Employee Number",
    "First Name",
    "Last Name",
    "SSS",
    "PhilHealth",
    "TIN",
    "Pag-IBIG",
  ];

  const handleOpenCreateModal = () => {
    reset(); // Reset form when opening
    setOpenCreateModal(true);
  };

  const handleCloseCreateModal = () => {
    setOpenCreateModal(false);
  };

  const onSubmit = (data) => {
    // Convert numeric strings to numbers. Yup already handles type conversion,
    // but parseFloat can be used here for explicit control if needed.
    // For react-hook-form with yup, the values from 'data' should already be in the correct type
    // as defined in the yup schema (e.g., numbers for numeric fields).
    // Ensure `immediateSupervisor` is null if empty string before sending to backend.
    const dataToSend = {
      ...data,
      immediateSupervisor:
        data.immediateSupervisor === "" ? null : data.immediateSupervisor,
    };
    createEmployeeMutation(dataToSend);
  };

  return (
    <Box sx={{ p: 3 }}>
      {" "}
      {/* Added Box for padding */}
      <Box sx={{ display: "flex", justifyContent: "flex-end", mb: 2 }}>
        <Button
          variant="contained"
          color="primary"
          startIcon={<PersonAddAlt1Icon />}
          onClick={handleOpenCreateModal}
          sx={{ borderRadius: 2, textTransform: "none", px: 3, py: 1.5 }}
        >
          Create New Employee
        </Button>
      </Box>
      <TableContainer component={Paper} sx={{ mt: 4, maxHeight: 600 }}>
        <Table stickyHeader>
          <TableHead>
            <TableRow>
              {columns.map((column) => (
                <TableCell key={column} sx={{ fontWeight: "bold" }}>
                  {column}
                </TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {employees.length === 0 ? (
              <TableRow>
                <TableCell
                  colSpan={columns.length}
                  sx={{ textAlign: "center", py: 3 }}
                >
                  No employees found.
                </TableCell>
              </TableRow>
            ) : (
              employees.map((emp) => (
                <TableRow key={emp?.employeeNumber}>
                  <TableCell>
                    <Button
                      variant="outlined"
                      size="small"
                      onClick={(e) => {
                        e.preventDefault();
                        navigate(`/employees/${emp?.employeeNumber}`);
                      }}
                      sx={{ borderRadius: 1 }}
                    >
                      {emp.employeeNumber}
                    </Button>
                  </TableCell>
                  <TableCell>{emp?.firstName}</TableCell>
                  <TableCell>{emp?.lastName}</TableCell>
                  <TableCell>{emp?.sssNumber}</TableCell>
                  <TableCell>{emp?.philhealthNumber}</TableCell>
                  <TableCell>{emp?.tinNumber}</TableCell>
                  <TableCell>{emp?.pagibigNumber}</TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>
      {/* NEW: Create Employee Modal */}
      <Dialog
        open={openCreateModal}
        onClose={handleCloseCreateModal}
        maxWidth="md"
        fullWidth
        PaperProps={{ sx: { borderRadius: 3 } }}
      >
        <DialogTitle
          sx={{ bgcolor: "primary.main", color: "white", py: 2, px: 3 }}
        >
          <Box display="flex" alignItems="center">
            <PersonAddAlt1Icon sx={{ mr: 1 }} />
            Create New Employee Record
          </Box>
        </DialogTitle>
        <DialogContent dividers sx={{ p: 4 }}>
          <Box
            component="form"
            onSubmit={handleSubmit(onSubmit)}
            sx={{ "& .MuiTextField-root": { mb: 2 } }}
          >
            <Typography
              variant="h6"
              gutterBottom
              sx={{ mb: 2, fontWeight: "bold" }}
            >
              Personal Information
            </Typography>
            <Stack
              direction={{ xs: "column", sm: "row" }}
              spacing={2}
              sx={{ mb: 2 }}
            >
              <TextField
                label="Employee Number"
                {...register("employeeNumber")}
                error={!!errors.employeeNumber}
                helperText={errors.employeeNumber?.message}
                fullWidth
                required
                size="small"
                variant="outlined"
                sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
              />
              <TextField
                label="First Name"
                {...register("firstName")}
                error={!!errors.firstName}
                helperText={errors.firstName?.message}
                fullWidth
                required
                size="small"
                variant="outlined"
                sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
              />
              <TextField
                label="Last Name"
                {...register("lastName")}
                error={!!errors.lastName}
                helperText={errors.lastName?.message}
                fullWidth
                required
                size="small"
                variant="outlined"
                sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
              />
            </Stack>
            <TextField
              label="Birthday (MM/DD/YYYY)"
              {...register("birthday")}
              error={!!errors.birthday}
              helperText={errors.birthday?.message}
              fullWidth
              required
              size="small"
              variant="outlined"
              placeholder="e.g., 01/23/1990"
              sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
            />
            <TextField
              label="Address"
              {...register("address")}
              error={!!errors.address}
              helperText={errors.address?.message}
              fullWidth
              required
              multiline
              minRows={2}
              size="small"
              variant="outlined"
              sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
            />
            <TextField
              label="Phone Number"
              {...register("phoneNumber")}
              error={!!errors.phoneNumber}
              helperText={errors.phoneNumber?.message}
              fullWidth
              required
              size="small"
              variant="outlined"
              sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
            />

            <Typography
              variant="h6"
              gutterBottom
              sx={{ mt: 3, mb: 2, fontWeight: "bold" }}
            >
              Government IDs
            </Typography>
            <TextField
              label="SSS Number"
              {...register("sssNumber")}
              error={!!errors.sssNumber}
              helperText={errors.sssNumber?.message}
              fullWidth
              required
              size="small"
              variant="outlined"
              sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
            />
            <TextField
              label="PhilHealth Number"
              {...register("philhealthNumber")}
              error={!!errors.philhealthNumber}
              helperText={errors.philhealthNumber?.message}
              fullWidth
              required
              size="small"
              variant="outlined"
              sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
            />
            <TextField
              label="TIN Number"
              {...register("tinNumber")}
              error={!!errors.tinNumber}
              helperText={errors.tinNumber?.message}
              fullWidth
              required
              size="small"
              variant="outlined"
              sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
            />
            <TextField
              label="Pag-IBIG Number"
              {...register("pagibigNumber")}
              error={!!errors.pagibigNumber}
              helperText={errors.pagibigNumber?.message}
              fullWidth
              required
              size="small"
              variant="outlined"
              sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
            />

            <Typography
              variant="h6"
              gutterBottom
              sx={{ mt: 3, mb: 2, fontWeight: "bold" }}
            >
              Employment Details
            </Typography>
            <TextField
              label="Status"
              {...register("status")}
              error={!!errors.status}
              helperText={errors.status?.message}
              fullWidth
              required
              size="small"
              variant="outlined"
              sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
            />
            <TextField
              label="Position"
              {...register("position")}
              error={!!errors.position}
              helperText={errors.position?.message}
              fullWidth
              required
              size="small"
              variant="outlined"
              sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
            />
            <TextField
              label="Immediate Supervisor"
              {...register("immediateSupervisor")}
              error={!!errors.immediateSupervisor}
              helperText={errors.immediateSupervisor?.message}
              fullWidth
              size="small"
              variant="outlined"
              sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
            />

            <Typography
              variant="h6"
              gutterBottom
              sx={{ mt: 3, mb: 2, fontWeight: "bold" }}
            >
              Compensation
            </Typography>
            <TextField
              label="Basic Salary"
              {...register("basicSalary")}
              error={!!errors.basicSalary}
              helperText={errors.basicSalary?.message}
              fullWidth
              required
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
              {...register("riceSubsidy")}
              error={!!errors.riceSubsidy}
              helperText={errors.riceSubsidy?.message}
              fullWidth
              required
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
              {...register("phoneAllowance")}
              error={!!errors.phoneAllowance}
              helperText={errors.phoneAllowance?.message}
              fullWidth
              required
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
              {...register("clothingAllowance")}
              error={!!errors.clothingAllowance}
              helperText={errors.clothingAllowance?.message}
              fullWidth
              required
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
              label="Gross Semi-monthly Rate"
              {...register("grossSemiMonthlyRate")}
              error={!!errors.grossSemiMonthlyRate}
              helperText={errors.grossSemiMonthlyRate?.message}
              fullWidth
              required
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
              {...register("hourlyRate")}
              error={!!errors.hourlyRate}
              helperText={errors.hourlyRate?.message}
              fullWidth
              required
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
          </Box>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button
            onClick={handleCloseCreateModal}
            variant="outlined"
            color="secondary"
            sx={{ borderRadius: 2 }}
          >
            Cancel
          </Button>
          <Button
            type="submit" // This button will trigger the form submission
            variant="contained"
            color="primary"
            onClick={handleSubmit(onSubmit)} // Call handleSubmit with onSubmit
            disabled={isCreating}
            startIcon={
              isCreating ? <CircularProgress size={20} color="inherit" /> : null
            }
            sx={{ borderRadius: 2 }}
          >
            {isCreating ? "Creating..." : "Create Employee"}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default EmployeeList;
