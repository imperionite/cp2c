import React, { lazy } from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import { useAtomValue } from "jotai";
import { authAtom } from "../services/atoms";

const Login = lazy(() => import("./Login"));
const Employees = lazy(() => import("./Employees"));
const Home = lazy(() => import("./Home"));
const About = lazy(() => import("./About"));
const Services = lazy(() => import("./Services"));
const Contact = lazy(() => import("./Contact"));
const EmployeeDetailsPage = lazy(() => import("./EmployeeDetail"));
const NotFound = lazy(() => import("./404"));

const RouterList = () => {
  const auth = useAtomValue(authAtom);

  return (
    <Routes>
      <Route
        path="/"
        element={auth?.token ? <Navigate to="/employees" replace /> : <Login />}
      />
      <Route
        path="/employees"
        element={auth?.token ? <Employees /> : <Navigate to="/" replace />}
      />
      <Route
        path="/employees/:employeeNumber"
        element={
          auth?.token ? <EmployeeDetailsPage /> : <Navigate to="/" replace />
        }
      />
      <Route path="/home" element={<Home />} />
      <Route path="/about" element={<About />} />
      <Route path="/services" element={<Services />} />
      <Route path="/contact" element={<Contact />} />
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
};

export default RouterList;