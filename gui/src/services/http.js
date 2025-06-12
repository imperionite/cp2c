import axios from "axios";
import qs from "qs";

/***
 * Important: create a .env file at the root of this sub-project (GUI folder) and place:
 * VITE_BASE_URL=http://localhost:8080 or the port of the Spring Boot app
 ***/

const baseURL = import.meta.env.VITE_BASE_URL;
const http = axios.create({
  baseURL: baseURL,
  withCredentials: true,
  timeout: 100000,
});

// Function to get access token
const getAccessToken = () => {
  const authAtom = localStorage.getItem("authAtom");
  let auth = authAtom ? JSON.parse(authAtom) : null;
  return auth ? auth.token : null;
};

// Interceptor to add the Authorization header with the access token
http.interceptors.request.use(
  (config) => {
    const token = getAccessToken();
    if (token) {
      config.headers["Authorization"] = `Bearer ${token}`;
    }

    if (config.data) {
      if (
        typeof config.data === "object" &&
        !(config.data instanceof FormData)
      ) {
        config.headers["Content-Type"] = "application/json";
      } else if (typeof config.data === "string") {
        config.headers["Content-Type"] = "application/x-www-form-urlencoded";
      } else if (config.data instanceof FormData) {
        delete config.headers["Content-Type"];
      }
    }

    if (
      config.headers["Content-Type"] === "application/x-www-form-urlencoded"
    ) {
      config.data = qs.stringify(config.data);
    }

    return config;
  },
  (error) => Promise.reject(error)
);

// Interceptor to handle 401 errors (no refresh endpoint available)
http.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      // Session expired or unauthorized. Log out the user.
      localStorage.removeItem("authAtom");
      console.log("Session expired or unauthorized. Please log in again.");
      // Redirect to login page (optional)
      window.location.href = "/login";
      return Promise.reject(error);
    }

    return Promise.reject(error);
  }
);

// API functions
const login = async (data) => {
  try {
    const response = await http.post("/api/login", data);
    return response.data;
  } catch (error) {
    console.error("Login error", error);
    throw new Error("Failed to login!");
  }
};

const register = async (data) => {
  try {
    const response = await http.post("/api/register", data);
    return response.data;
  } catch (error) {
    console.error("Register error", error);
    throw new Error("Failed to register!");
  }
};

const createEmployee = async (data) => {
  try {
    const response = await http.post("/api/protected/employees", data);
    return response.data;
  } catch (error) {
    console.error("Create new employee error", error);
    throw new Error("Failed to create new employee!");
  }
};

const getEmployeePartialDetails = async () => {
  try {
    const response = await http.get("/api/protected/employees");
    return response.data;
  } catch (error) {
    console.error("Error fetching employee:", error);
    throw new Error("Failed to fetch employees list!");
  }
};

const getEmployeeByEmployeeNumber = async (employeeNumber) => {
  try {
    const response = await http.get(
      `/api/protected/employees/${employeeNumber}`
    );
    return response.data;
  } catch (error) {
    console.error("Error fetching employee:", error);
    throw new Error("Failed to fetch employee data");
  }
};

const updateEmployee = async (employeeNumber, data) => {
  try {
    const response = await http.patch(
      `/api/protected/employees/${employeeNumber}`,
      data
    );
    return response.data;
  } catch (error) {
    console.error(`Error updating employee ${employeeNumber}:`, error);
    throw new Error(
      error.response?.data?.message ||
        `Failed to update employee ${employeeNumber}!`
    );
  }
};

const deleteEmployee = async (employeeNumber) => {
  try {
    const response = await http.delete(
      `/api/protected/employees/${employeeNumber}`
    );
    // For 204 No Content, response.data might be empty or undefined.
    // Return a success message or simply check response.status
    if (response.status === 204) {
      return { message: `Employee ${employeeNumber} deleted successfully.` };
    }
    return response.data; // For other successful statuses that might return data
  } catch (error) {
    console.error(`Error deleting employee ${employeeNumber}:`, error);
    throw new Error(
      error.response?.data?.message ||
        `Failed to delete employee ${employeeNumber}!`
    );
  }
};

const fetchMonthlyCutoffs = async () => {
  try {
    const response = await http.get("/api/protected/monthly-cutoffs");
    return response.data;
  } catch (error) {
    console.error("Error fetching monthly cut-off periods:", error);
    throw new Error("Failed to fetch monthly cut-offs");
  }
};
const fetchEmployeeMonthlySalary = async (employeeNumber, yearMonth) => {
  try {
    const response = await http.get(
      `api/protected/employees/${employeeNumber}/salary?yearMonth=${yearMonth}`
    );
    return response.data;
  } catch (error) {
    console.error("Error fetching employee monthly salary:", error);
    throw new Error("Failed to fetch employee monthly salary");
  }
};

export {
  login,
  register,
  getAccessToken,
  getEmployeePartialDetails,
  getEmployeeByEmployeeNumber,
  updateEmployee,
  deleteEmployee,
  fetchEmployeeMonthlySalary,
  fetchMonthlyCutoffs,
  createEmployee
};
