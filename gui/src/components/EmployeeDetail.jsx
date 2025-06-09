import { lazy } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useAtomValue } from "jotai";
import { authAtom } from "../services/atoms";
import {
  Typography,
  Button,
  Container,
  Card,
  CardContent,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  CircularProgress,
  Box,
  Table,
  TableBody,
  TableCell,
  TableRow,
  TableContainer,
  Paper,
  Stack,
} from "@mui/material";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import { toast } from "react-hot-toast";

import { useFetchByEmployeeNumber } from "../services/hooks";

const Loader = lazy(() => import("./Loader"));

const EmployeeDetail = () => {
  const { employeeNumber } = useParams();
  const auth = useAtomValue(authAtom);
  const navigate = useNavigate();

  const {
    data: employeeData,
    isLoading: isLoadingEmployee,
    isError: isErrorEmployee,
    error: employeeError,
  } = useFetchByEmployeeNumber(auth?.token, employeeNumber);

  if (isLoadingEmployee) return <Loader />;
  if (isErrorEmployee) return toast.error(employeeError?.message);

  const employee = employeeData;

  return (
    <Container sx={{ mt: 4 }} maxWidth="lg">
      <Button
        startIcon={<ArrowBackIcon />}
        variant="outlined"
        onClick={() => navigate(-1)}
        sx={{ mb: 2 }}
      >
        Back to List
      </Button>

      <Typography variant="h4" gutterBottom>
        Employee #{employee.employeeNumber} - {employee.firstName}{" "}
        {employee.lastName}
      </Typography>
      <Typography variant="body1" color="success" sx={{ mb: 3 }}>
        Authenticated User: {auth?.username}
      </Typography>

      {/* Monthly Salary Calculation Section (Admin Only) - MOVED TO TOP */}
      {isAdmin && (
        <Box sx={{ mb: 3 }}>
          {" "}
          {/* Using Box for spacing around the card */}
          <Card variant="outlined">
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Monthly Salary Calculation
              </Typography>
              <Box
                sx={{
                  display: "flex",
                  alignItems: "center",
                  gap: 2,
                  mb: 2,
                  flexWrap: "wrap",
                }}
              >
                {" "}
                {/* flexWrap for responsiveness */}
                <FormControl sx={{ minWidth: 200 }}>
                  <InputLabel id="month-select-label">Select Month</InputLabel>
                  <Select
                    labelId="month-select-label"
                    id="month-select"
                    value={selectedYearMonth}
                    label="Select Month"
                    onChange={(e) => setSelectedYearMonth(e.target.value)}
                    disabled={isLoadingCutoffs}
                  >
                    {monthlyCutoffs && monthlyCutoffs.length > 0 ? (
                      monthlyCutoffs.map((cutoff) => (
                        <MenuItem
                          key={cutoff.yearMonth}
                          value={cutoff.yearMonth}
                        >
                          {cutoff.yearMonth} ({cutoff.startDate} to{" "}
                          {cutoff.endDate})
                        </MenuItem>
                      ))
                    ) : (
                      <MenuItem value="" disabled>
                        No months available
                      </MenuItem>
                    )}
                  </Select>
                </FormControl>
                <Button
                  variant="contained"
                  onClick={handleOpenSalaryDialog}
                  disabled={!selectedYearMonth || isLoadingMonthlyNet}
                >
                  Calculate Monthly Salary & Deductions
                </Button>
              </Box>
              <Typography variant="body2" color="text.secondary">
                Choose a month to calculate the employee's gross and net salary.
              </Typography>
            </CardContent>
          </Card>
        </Box>
      )}

      {/* Main content layout using Stack instead of Grid */}
      <Stack
        direction={{ xs: "column", md: "row" }} // Stack vertically on small, horizontally on medium+
        spacing={3} // Spacing between cards
        flexWrap="wrap" // Allow cards to wrap to the next line
        useFlexGap // Enables gap property for better responsive spacing
      >
        {/* Personal Info Section */}
        <Box sx={{ flex: "1 1 100%", maxWidth: { md: "calc(50% - 12px)" } }}>
          {" "}
          {/* Mimics xs=12, md=6 */}
          <Card variant="outlined">
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Personal Information
              </Typography>
              <TableContainer component={Paper} elevation={0}>
                <Table size="small">
                  <TableBody>
                    <TableRow>
                      <TableCell sx={{ fontWeight: "bold", width: "35%" }}>
                        Name:
                      </TableCell>
                      <TableCell>
                        {employee.firstName} {employee.lastName}
                      </TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell sx={{ fontWeight: "bold" }}>
                        Birthday:
                      </TableCell>
                      <TableCell>{employee.birthday}</TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell sx={{ fontWeight: "bold" }}>
                        Address:
                      </TableCell>
                      <TableCell>{employee.address}</TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell sx={{ fontWeight: "bold" }}>
                        Phone Number:
                      </TableCell>
                      <TableCell>{employee.phoneNumber}</TableCell>
                    </TableRow>
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>
        </Box>

        {/* Government IDs Section */}
        <Box sx={{ flex: "1 1 100%", maxWidth: { md: "calc(50% - 12px)" } }}>
          {" "}
          {/* Mimics xs=12, md=6 */}
          <Card variant="outlined">
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Government IDs
              </Typography>
              <TableContainer component={Paper} elevation={0}>
                <Table size="small">
                  <TableBody>
                    <TableRow>
                      <TableCell sx={{ fontWeight: "bold", width: "35%" }}>
                        SSS:
                      </TableCell>
                      <TableCell>{employee.sss}</TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell sx={{ fontWeight: "bold" }}>
                        PhilHealth:
                      </TableCell>
                      <TableCell>{employee.philhealth}</TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell sx={{ fontWeight: "bold" }}>TIN:</TableCell>
                      <TableCell>{employee.tin}</TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell sx={{ fontWeight: "bold" }}>
                        Pag-IBIG:
                      </TableCell>
                      <TableCell>{employee.pagibig}</TableCell>
                    </TableRow>
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>
        </Box>

        {/* Compensation Section */}
        <Box sx={{ flex: "1 1 100%", maxWidth: { md: "calc(50% - 12px)" } }}>
          {" "}
          {/* Mimics xs=12, md=6 */}
          <Card variant="outlined">
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Compensation
              </Typography>
              <TableContainer component={Paper} elevation={0}>
                <Table size="small">
                  <TableBody>
                    <TableRow>
                      <TableCell sx={{ fontWeight: "bold", width: "35%" }}>
                        Basic Salary:
                      </TableCell>
                      <TableCell>
                        ₱
                        {employee.basicSalary?.toLocaleString(undefined, {
                          minimumFractionDigits: 2,
                          maximumFractionDigits: 2,
                        })}
                      </TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell sx={{ fontWeight: "bold" }}>
                        Rice Subsidy:
                      </TableCell>
                      <TableCell>
                        ₱
                        {employee.riceSubsidy?.toLocaleString(undefined, {
                          minimumFractionDigits: 2,
                          maximumFractionDigits: 2,
                        })}
                      </TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell sx={{ fontWeight: "bold" }}>
                        Phone Allowance:
                      </TableCell>
                      <TableCell>
                        ₱
                        {employee.phoneAllowance?.toLocaleString(undefined, {
                          minimumFractionDigits: 2,
                          maximumFractionDigits: 2,
                        })}
                      </TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell sx={{ fontWeight: "bold" }}>
                        Clothing Allowance:
                      </TableCell>
                      <TableCell>
                        ₱
                        {employee.clothingAllowance?.toLocaleString(undefined, {
                          minimumFractionDigits: 2,
                          maximumFractionDigits: 2,
                        })}
                      </TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell sx={{ fontWeight: "bold" }}>
                        Hourly Rate:
                      </TableCell>
                      <TableCell>
                        ₱
                        {employee.hourlyRate?.toLocaleString(undefined, {
                          minimumFractionDigits: 2,
                          maximumFractionDigits: 2,
                        })}
                      </TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell sx={{ fontWeight: "bold" }}>
                        Gross Semi-Monthly Rate:
                      </TableCell>
                      <TableCell>
                        ₱
                        {employee.grossSemiMonthlyRate?.toLocaleString(
                          undefined,
                          { minimumFractionDigits: 2, maximumFractionDigits: 2 }
                        )}
                      </TableCell>
                    </TableRow>
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>
        </Box>

        {/* Position and Status Section */}
        <Box sx={{ flex: "1 1 100%", maxWidth: { md: "calc(50% - 12px)" } }}>
          {" "}
          {/* Mimics xs=12, md=6 */}
          <Card variant="outlined">
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Position and Status
              </Typography>
              <TableContainer component={Paper} elevation={0}>
                <Table size="small">
                  <TableBody>
                    <TableRow>
                      <TableCell sx={{ fontWeight: "bold", width: "35%" }}>
                        Position:
                      </TableCell>
                      <TableCell>{employee.position}</TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell sx={{ fontWeight: "bold" }}>Status:</TableCell>
                      <TableCell>{employee.status}</TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell sx={{ fontWeight: "bold" }}>
                        Immediate Supervisor:
                      </TableCell>
                      <TableCell>{employee.immediateSupervisor}</TableCell>
                    </TableRow>
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>
        </Box>

        {/* User Section */}
        <Box sx={{ flex: "1 1 100%", maxWidth: { md: "calc(50% - 12px)" } }}>
          {" "}
          {/* Mimics xs=12, md=6 */}
          <Card variant="outlined">
            <CardContent>
              <Typography variant="h6" gutterBottom>
                User Info
              </Typography>
              <TableContainer component={Paper} elevation={0}>
                <Table size="small">
                  <TableBody>
                    <TableRow>
                      <TableCell sx={{ fontWeight: "bold", width: "35%" }}>
                        User ID:
                      </TableCell>
                      <TableCell>{employee.user?.id}</TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell sx={{ fontWeight: "bold" }}>
                        Employee ID:
                      </TableCell>
                      <TableCell>{employee.id}</TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell sx={{ fontWeight: "bold" }}>
                        Username:
                      </TableCell>
                      <TableCell>{employee.user?.username}</TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell sx={{ fontWeight: "bold" }}>Role:</TableCell>
                      <TableCell>
                        {employee.user?.is_admin === "true"
                          ? "Admin"
                          : "Regular User"}
                      </TableCell>
                    </TableRow>
                    <TableRow>
                      <TableCell sx={{ fontWeight: "bold" }}>
                        Date Joined:
                      </TableCell>
                      <TableCell>{employee.user?.createdAt}</TableCell>
                    </TableRow>
                  </TableBody>
                </Table>
              </TableContainer>
            </CardContent>
          </Card>
        </Box>
      </Stack>

      {/* Salary Details Dialog */}
      <Dialog
        open={isSalaryDialogOpen}
        onClose={handleCloseSalaryDialog}
        fullWidth
        maxWidth="sm"
      >
        <DialogTitle>
          Monthly Salary for {employee.firstName} {employee.lastName} (
          {selectedYearMonth})
        </DialogTitle>
        <DialogContent dividers>
          {isLoadingMonthlyNet ? (
            <Box
              display="flex"
              justifyContent="center"
              alignItems="center"
              height={100}
            >
              <CircularProgress />
              <Typography variant="body1" sx={{ ml: 2 }}>
                Calculating...
              </Typography>
            </Box>
          ) : isErrorMonthlyNet ? (
            <Typography color="error">
              Failed to load salary details:{" "}
              {monthlyNetError?.message || "Unknown error"}
            </Typography>
          ) : monthlyNetSalaryData ? (
            <Box sx={{ "& > div": { mb: 1 } }}>
              <Typography variant="h6" gutterBottom>
                Summary
              </Typography>
              <div>
                <Typography variant="body1">
                  <strong>Worked Hours:</strong>{" "}
                  {monthlyNetSalaryData.monthly_worked_hours?.toFixed(2) ||
                    "N/A"}
                </Typography>
              </div>
              <div>
                <Typography variant="body1">
                  <strong>Gross Salary:</strong> ₱
                  {monthlyNetSalaryData.gross_monthly_salary?.toFixed(2) ||
                    "N/A"}
                </Typography>
              </div>
              <Typography variant="h6" sx={{ mt: 2 }} gutterBottom>
                Deductions
              </Typography>
              <div>
                <Typography variant="body1">
                  <strong>SSS Deduction:</strong> ₱
                  {monthlyNetSalaryData.monthly_sss_deduction?.toFixed(2) ||
                    "N/A"}
                </Typography>
              </div>
              <div>
                <Typography variant="body1">
                  <strong>PhilHealth Deduction:</strong> ₱
                  {monthlyNetSalaryData.monthly_philhealth_deduction?.toFixed(
                    2
                  ) || "N/A"}
                </Typography>
              </div>
              <div>
                <Typography variant="body1">
                  <strong>Pag-IBIG Deduction:</strong> ₱
                  {monthlyNetSalaryData.monthly_pagibig_deduction?.toFixed(2) ||
                    "N/A"}
                </Typography>
              </div>
              <div>
                <Typography variant="body1">
                  <strong>Withholding Tax:</strong> ₱
                  {monthlyNetSalaryData.monthly_withholding_tax?.toFixed(2) ||
                    "N/A"}
                </Typography>
              </div>
              <div>
                <Typography variant="body1">
                  <strong>Total Deductions:</strong> ₱
                  {monthlyNetSalaryData.total_deductions?.toFixed(2) || "N/A"}
                </Typography>
              </div>
              <Typography variant="h5" sx={{ mt: 3, fontWeight: "bold" }}>
                Net Salary: ₱
                {monthlyNetSalaryData.net_monthly_salary?.toFixed(2) || "N/A"}
              </Typography>
            </Box>
          ) : (
            <Typography>
              Select a month and click "Calculate Monthly Salary" to view
              details.
            </Typography>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseSalaryDialog}>Close</Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default EmployeeDetail;
