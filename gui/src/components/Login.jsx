import { lazy } from "react";
import {
  Box,
  Button,
  TextField,
  Typography,
  Link,
  CircularProgress,
} from "@mui/material";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useSetAtom } from "jotai";
import { useForm } from "react-hook-form";
import { yupResolver } from "@hookform/resolvers/yup";
import { useNavigate } from "react-router-dom";
import { toast } from "react-hot-toast";
import { sanitize } from "isomorphic-dompurify";
import * as yup from "yup";

import { login } from "../services/http";
import { userKeys, employeeKeys } from "../services/queryKeyFactory";
import { authAtom } from "../services/atoms";
const Loader = lazy(() => import("./Loader"));

const Login = () => {
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const setAuth = useSetAtom(authAtom);

  // Validation schema
  const schema = yup
    .object({
      username: yup.string().required("Enter your registered username"),
      password: yup.string().required("Password is required"),
    })
    .required();

  // Mutation hook for login
  const mutation = useMutation({
    mutationFn: login,
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: userKeys.all });
      queryClient.invalidateQueries({ queryKey: employeeKeys.all });

      setAuth({
        userId: data?.userId,
        username: data?.username,
        token: data?.token,
      });

      toast.success(data?.message);
      reset();
      navigate("/employees");
    },
    onError: (error) => {
      const errorMessage =
        error.response?.data?.detail ||
        error.message ||
        "Login failed. Please try again.";
      toast.error(errorMessage);
    },
  });

  // React Hook Form setup
  const {
    register,
    reset,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: yupResolver(schema),
    mode: "all",
    defaultValues: {
      username: "",
      password: "",
    },
  });

  const onSubmit = (input) => {
    const sanitizedData = {
      username: sanitize(input.username),
      password: input.password,
    };
    mutation.mutate(sanitizedData);
  };

  if (mutation.isLoading) return <Loader />;
  // if (mutation.isError) toast.error(mutation?.failureReason?.response?.data?.error);

  return (
    <Box
      sx={{
        minHeight: "100vh", // full viewport height
        display: "flex",
        flexDirection: "column",
        justifyContent: "center", // vertical center
        alignItems: "center", // horizontal center
        bgcolor: "background.default",
        px: 2,
      }}
    >
      <Typography variant="h6" component="h6" gutterBottom>
        <Link href={"/home"} underline="none">
          MotorPH By Imperionite
        </Link>
      </Typography>
      <Box
        sx={{
          maxWidth: 400,
          width: "100%",
          p: 3,
          bgcolor: "background.paper",
          borderRadius: 2,
          boxShadow: 3,
          mt: 2,
        }}
      >
        <Typography variant="h4" align="center" gutterBottom>
          Login
        </Typography>

        <form onSubmit={handleSubmit(onSubmit)} noValidate>
          <TextField
            label="Username"
            variant="outlined"
            fullWidth
            margin="normal"
            {...register("username")}
            error={!!errors.username}
            helperText={errors.username ? errors.username.message : ""}
            autoComplete="current-username"
          />

          <TextField
            label="Password"
            type="password"
            variant="outlined"
            fullWidth
            margin="normal"
            {...register("password")}
            error={!!errors.password}
            helperText={errors.password ? errors.password.message : ""}
            autoComplete="current-password"
          />

          <Button
            type="submit"
            fullWidth
            variant="contained"
            sx={{ mt: 2 }}
            disabled={mutation.isLoading}
          >
            {mutation.isLoading ? (
              <CircularProgress size={24} color="inherit" />
            ) : (
              "Login"
            )}
          </Button>
        </form>
      </Box>
    </Box>
  );
};

export default Login;
