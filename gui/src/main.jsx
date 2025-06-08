import React, { Suspense, lazy } from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { Provider as JotaiRoot } from "jotai";
import { ThemeProvider } from "@mui/material/styles";
import CssBaseline from "@mui/material/CssBaseline";

import App from "./App.jsx";
import theme from "./theme";

import './index.css'

const queryClient = new QueryClient();
const root = ReactDOM.createRoot(document.getElementById("root"));

const Loader = lazy(() => import("./components/Loader.jsx"));

root.render(
  <React.StrictMode>
    <Suspense fallback={<Loader />}>
      <QueryClientProvider client={queryClient}>
        <JotaiRoot>
          <BrowserRouter>
            <ThemeProvider theme={theme}>
              <CssBaseline />
              <App />
            </ThemeProvider>
          </BrowserRouter>
        </JotaiRoot>
      </QueryClientProvider>
    </Suspense>
  </React.StrictMode>
);