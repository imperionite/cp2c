import Box from "@mui/material/Box";
import Container from "@mui/material/Container";

const Home = () => {
  return (
    <Container maxWidth="xl">
      <Box sx={{ bgcolor: "#cfe8fc", p: 3, mt: 4 }}>
        <header>
          <h2>Welcome to MotorPH: Employee Management System</h2>
        </header>

        <section>
          <h3>Home</h3>
          <p>
            This is a proof-of-concept webpage design to augment and test the
            MotorPH backend REST API project for employee management system at{" "}
            <a href="https://github.com/imperionite/cp2c">
              {" "}
              https://github.com/imperionite/cp2c
            </a>
            .
          </p>
        </section>
      </Box>
    </Container>
  );
};

export default Home;