import Box from "@mui/material/Box";
import Container from "@mui/material/Container";

const Services = () => {
  return (
    <Container maxWidth="xl">
      <Box sx={{ bgcolor: "#cfe8fc", p: 3, mt: 4 }}>
        <header>
          <h2>Employee Management System</h2>
        </header>

        <section>
          <h3>Services</h3>
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

export default Services;