import Box from "@mui/material/Box";
import Container from "@mui/material/Container";
import { Link, useLocation } from "react-router-dom";

const NotFound = () => {
  const location = useLocation();
  return (
    <Container maxWidth="xl">
      <Box sx={{ bgcolor: "#cfe8fc", p: 3, mt: 4 }}>
        <header>
          <h2>MotorPH: Employee Management System</h2>
        </header>

        <section>
          <h3>HTTP 404</h3>
          <p>
            We don&apos;t have <strong>{location.pathname}</strong> route exist!
            Please go back to <Link to={"/home"}>home page</Link>
          </p>
          <p>
            This is a proof-of-concept webpage design to augment and test the
            MotorPH backend REST API project for employee management system at{" "}
            <a href="https://github.com/imperionite/cp2a">
              {" "}
              https://github.com/imperionite/cp2a
            </a>
            .
          </p>
        </section>
      </Box>
    </Container>
  );
};

export default NotFound;