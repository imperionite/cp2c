import { useEffect, lazy } from "react";
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
} from "@mui/material";
import { toast } from "react-hot-toast";
import { employeesAtom } from "../services/atoms";
import { useEmployeePartialDetails } from "../services/hooks";
import { authAtom } from "../services/atoms";
import { useNavigate } from "react-router-dom";

const Loader = lazy(() => import("./Loader"));

const EmployeeList = () => {
  const [employees, setEmployees] = useAtom(employeesAtom);
  const auth = useAtomValue(authAtom);
  const navigate = useNavigate();

  const {
    data: employeesData,
    isLoading,
    isError,
    error,
  } = useEmployeePartialDetails(auth?.token);

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

  return (
    <>
      <TableContainer component={Paper} sx={{ mt: 4, maxHeight: 600 }}>
        <Table stickyHeader>
          <TableHead>
            <TableRow>
              {columns.map((column) => (
                <TableCell key={column}>{column}</TableCell>
              ))}
            </TableRow>
          </TableHead>
          <TableBody>
            {employees.map((emp) => (
              <TableRow key={emp.employeeNumber}>
                <TableCell>
                  <Button
                    variant="outlined"
                    size="small"
                    onClick={(e) => {
                      e.preventDefault();
                      navigate(`/employees/${emp.employeeNumber}`);
                    }}
                  >
                    {emp.employeeNumber}
                  </Button>
                </TableCell>
                <TableCell>{emp.firstName}</TableCell>
                <TableCell>{emp.lastName}</TableCell>
                <TableCell>{emp.sssNumber}</TableCell>
                <TableCell>{emp.philhealthNumber}</TableCell>
                <TableCell>{emp.tinNumber}</TableCell>
                <TableCell>{emp.pagibigNumber}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </>
  );
};

export default EmployeeList;
