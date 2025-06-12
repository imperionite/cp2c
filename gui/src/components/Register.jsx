import React, { useState, lazy, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "react-hot-toast";
import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import * as yup from "yup";
import {
  Box,
  Button,
  Container,
  TextField,
  Typography,
  Paper,
  CircularProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Stack,
} from "@mui/material";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import PersonAddIcon from "@mui/icons-material/PersonAdd";

import { register as apiRegister } from "../services/http";
import { userKeys, employeeKeys } from "../services/queryKeyFactory";

// Lazy load the loader component
const Loader = lazy(() => import("./Loader"));

// Yup validation schema
const schema = yup.object().shape({
  username: yup.string().trim().required("Username is required."),
  password: yup
    .string()
    .min(8, "Password must be at least 8 characters long.")
    .required("Password is required."),
  confirmPassword: yup
    .string()
    .oneOf([yup.ref("password"), null], "Passwords must match")
    .required("Confirm password is required."),
});

const Register = () => {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  const [openSuccessDialog, setOpenSuccessDialog] = useState(false);
  const [mutationError, setMutationError] = useState(null); // Track error separately

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    reset,
  } = useForm({
    resolver: yupResolver(schema),
  });

  const { mutate: registerMutation, isLoading } = useMutation({
    mutationFn: (data) => apiRegister(data.credentials),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: userKeys.all });
      queryClient.invalidateQueries({ queryKey: employeeKeys.all });
      setOpenSuccessDialog(true);
      reset();
    },
    onError: (err) => {
      // Instead of calling toast.error directly in the mutation, set the error in state
      setMutationError(err.message || "Failed to register user.");
    },
  });

  // Handle form submission
  const onSubmit = (data) => {
    registerMutation({
      credentials: {
        username: data.username,
        password: data.password,
      },
    });
  };

  const handleCloseSuccessDialog = () => {
    setOpenSuccessDialog(false);
    navigate("/employees");
  };

  // UseEffect to show toast when an error occurs
  useEffect(() => {
    if (mutationError) {
      toast.error(mutationError); // Show the error toast outside of render phase
    }
  }, [mutationError]); // This will run when mutationError state is updated

  if (isLoading) return <Loader />;
  // if (isError) return toast(error.message);

  return (
    <Container maxWidth="sm" sx={{ mt: 8, mb: 4 }}>
      <Paper elevation={6} sx={{ p: 4, borderRadius: 3 }}>
        <Box
          sx={{
            display: "flex",
            flexDirection: "column",
            alignItems: "center",
          }}
        >
          <PersonAddIcon color="primary" sx={{ fontSize: 60, mb: 2 }} />
          <Typography
            variant="h4"
            component="h1"
            gutterBottom
            sx={{ fontWeight: "bold", color: "text.primary", mb: 3 }}
          >
            Register New User
          </Typography>
          <Typography
            variant="body2"
            color="text.secondary"
            align="center"
            sx={{ mb: 4 }}
          >
            Only authenticated users can register new accounts.
          </Typography>

          <Box
            component="form"
            onSubmit={handleSubmit(onSubmit)}
            sx={{ width: "100%" }}
          >
            <Stack spacing={3}>
              <TextField
                label="Username"
                name="username"
                {...register("username")}
                fullWidth
                required
                error={!!errors.username}
                helperText={errors.username?.message}
                sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
              />
              <TextField
                label="Password"
                name="password"
                type="password"
                {...register("password")}
                fullWidth
                required
                error={!!errors.password}
                helperText={errors.password?.message}
                sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
              />
              <TextField
                label="Confirm Password"
                name="confirmPassword"
                type="password"
                {...register("confirmPassword")}
                fullWidth
                required
                error={!!errors.confirmPassword}
                helperText={errors.confirmPassword?.message}
                sx={{ "& .MuiOutlinedInput-root": { borderRadius: 2 } }}
              />

              <Button
                type="submit"
                variant="contained"
                color="primary"
                fullWidth
                size="large"
                disabled={isSubmitting}
                startIcon={
                  isSubmitting ? (
                    <CircularProgress size={20} color="inherit" />
                  ) : null
                }
                sx={{
                  mt: 3,
                  py: 1.5,
                  borderRadius: 2,
                  fontWeight: "bold",
                  fontSize: "1rem",
                  textTransform: "none",
                }}
              >
                {isSubmitting ? "Registering..." : "Register User"}
              </Button>
            </Stack>
          </Box>
        </Box>
      </Paper>

      {/* Back to Employee List Button */}
      <Box sx={{ display: "flex", justifyContent: "center", mt: 4 }}>
        <Button
          startIcon={<ArrowBackIcon />}
          variant="outlined"
          onClick={() => navigate("/employees")}
          sx={{
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
      </Box>

      {/* Success Dialog */}
      <Dialog
        open={openSuccessDialog}
        onClose={handleCloseSuccessDialog}
        maxWidth="xs"
        PaperProps={{ sx: { borderRadius: 2 } }}
      >
        <DialogTitle sx={{ bgcolor: "success.main", color: "white" }}>
          Registration Successful!
        </DialogTitle>
        <DialogContent dividers sx={{ p: 3 }}>
          <Typography>
            The new user has been successfully registered.
          </Typography>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button
            onClick={handleCloseSuccessDialog}
            variant="contained"
            color="primary"
            autoFocus
            sx={{ borderRadius: 2 }}
          >
            Continue
          </Button>
        </DialogActions>
      </Dialog>
    </Container>
  );
};

export default Register;
