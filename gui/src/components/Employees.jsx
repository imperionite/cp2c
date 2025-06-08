import { lazy } from "react";
import { useAtomValue } from "jotai";
import { Box, Container, Typography } from "@mui/material";
import { authAtom } from "../services/atoms";
const EmployeeList = lazy(() => import("./EmployeeList"));

const Employees = () => {
  const auth = useAtomValue(authAtom);

  return (
    <Container>
      <Box sx={{ bgcolor: "#cfe8fc", p: 2, mt: 2 }}>
        <header>
          <Typography variant="h5">Employees</Typography>
          <Typography variant="body1" color="success">
            Logged-in: {auth?.username}
          </Typography>
        </header>
        <section>
          <EmployeeList />
        </section>
      </Box>
    </Container>
  );
};

export default Employees;