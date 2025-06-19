import { lazy, useState, useEffect, Suspense } from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import { useAtomValue } from "jotai";
import { Box, CircularProgress, Typography } from "@mui/material"; 

import { authAtom } from "../services/atoms";

const Login = lazy(() => import("./Login"));
const Employees = lazy(() => import("./Employees"));
const EmployeeDetail = lazy(() => import("./EmployeeDetail"));
const Home = lazy(() => import("./Home"));
const About = lazy(() => import("./About"));
const Services = lazy(() => import("./Services"));
const Register = lazy(() => import("./Register"));
const Contact = lazy(() => import("./Contact"));
const NotFound = lazy(() => import("./404"));

const Loader = lazy(() => import("./Loader"));

// This component protects routes, ensuring only authenticated users can access them.
const PrivateRoute = ({ children }) => {
  const auth = useAtomValue(authAtom); // Get JWT token from Jotai atom
  const [isAttemptingRehydration, setIsAttemptingRehydration] = useState(true);

  useEffect(() => {
    // This effect runs once on mount to handle Jotai's state rehydration from localStorage.
    // auth token !== undefined: Indicates Jotai has read its initial value (even if it's null).
    // localStorage.getItem('authAtom'): Fallback check if Jotai's atom hasn't fully updated yet,
    const storedAuth = localStorage.getItem("authAtom");

    if (auth !== undefined || storedAuth) {
      // If Jotai has initialized (auth is not undefined) or there's a token in localStorage,
      // attempted rehydration.
      setIsAttemptingRehydration(false);
    } else {
      // As a safeguard for very quick loads where the atom's state
      // might not immediately transition from `undefined`
      const timer = setTimeout(() => {
        setIsAttemptingRehydration(false);
      }, 300); // Give Jotai 300ms to rehydrate
      return () => clearTimeout(timer); // Cleanup the timer
    }
  }, [auth]); 

  if (isAttemptingRehydration) {
    return <Loader />;
  }

  // Once rehydration is attempted/complete, apply the actual authentication check.
  // If 'jwt.access' exists (meaning user is authenticated), render children.
  // Otherwise, redirect to the login page.
  return auth?.token ? children : <Navigate to="/login" replace />;
};

// --- GuestRoute Component (Adopted from your code) ---
// This component prevents authenticated users from accessing "guest-only" routes (like login).
const GuestRoute = ({ children }) => {
  const auth = useAtomValue(authAtom); 
  const [isAttemptingRehydration, setIsAttemptingRehydration] = useState(true);

  useEffect(() => {
    // Similar rehydration logic as PrivateRoute to prevent premature redirects.
    const storedAuth = localStorage.getItem("authAtom");
    if (auth !== undefined || storedAuth) {
      setIsAttemptingRehydration(false);
    } else {
      const timer = setTimeout(() => {
        setIsAttemptingRehydration(false);
      }, 300);
      return () => clearTimeout(timer);
    }
  }, [auth]);

  if (isAttemptingRehydration) {
    return (
      <Box
        sx={{
          display: "flex",
          justifyContent: "center",
          alignItems: "center",
          minHeight: "80vh",
        }}
      >
        <CircularProgress />
        <Typography sx={{ ml: 2 }}>Loading authentication...</Typography>
      </Box>
    );
  }

  // If authenticated, redirect to the main employee dashboard (`/employees`).
  // Otherwise, render the children component (the guest page).
  return auth?.token ? <Navigate to="/employees" replace /> : children;
};

// --- RouterList Component ---
// This is the main component that defines all your application's routes.
const RouterList = () => {
  return (
    // Suspense is necessary for lazy-loaded components
    <Suspense fallback={<Loader />}>
      <Routes>
        {/* Public routes */}
        {/* The root path '/' now uses GuestRoute. If authenticated, it redirects to /employees.
            Otherwise, it renders the Login component. */}
        <Route
          path="/"
          element={
            <GuestRoute>
              <Login />
            </GuestRoute>
          }
        />

        {/* Other public pages */}
        <Route path="/home" element={<Home />} />
        <Route path="/about" element={<About />} />
        <Route path="/services" element={<Services />} />
        <Route path="/contact" element={<Contact />} />

        {/* Guest-only routes (accessible only if NOT authenticated) */}
        {/* The /login path is explicitly handled by GuestRoute */}
        <Route
          path="/login"
          element={
            <GuestRoute>
              <Login />
            </GuestRoute>
          }
        />

        {/* Protected routes (accessible only if authenticated) */}
        {/* The /employees list page is protected by PrivateRoute */}
        <Route
          path="/employees"
          element={
            <PrivateRoute>
              <Employees />
            </PrivateRoute>
          }
        />
        {/* Individual employee detail page is also protected */}
        <Route
          path="/employees/:employeeNumber"
          element={
            <PrivateRoute>
              <EmployeeDetail />
            </PrivateRoute>
          }
        />
        {/* Registering new user is a protected route */}
        <Route
          path="/register"
          element={
            <PrivateRoute>
              <Register />
            </PrivateRoute>
          }
        />

        {/* Catch-all route for any undefined paths */}
        <Route path="*" element={<NotFound />} />
      </Routes>
    </Suspense>
  );
};

export default RouterList;
