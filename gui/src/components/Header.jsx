import { useState } from "react";
import { useAtomValue } from "jotai";
import { useResetAtom } from "jotai/utils";
import { toast } from "react-hot-toast";
import { useQueryClient } from "@tanstack/react-query";
import { useNavigate, Link } from "react-router-dom";
import {
  AppBar,
  Toolbar,
  IconButton,
  Typography,
  Button,
  Box,
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
} from "@mui/material";
import MenuIcon from "@mui/icons-material/Menu";

import { authAtom } from "../services/atoms";

function Header() {
  const [drawerOpen, setDrawerOpen] = useState(false);
  const auth = useAtomValue(authAtom);
  const resetAuth = useResetAtom(authAtom);
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  const isAuthenticated = auth?.token && auth.token !== "";

  const handleLogout = () => {
    resetAuth();
    queryClient.clear();
    localStorage.clear();
    navigate("/");
    toast.success("Successfully logout!");
  };

  const toggleDrawer = (open) => (event) => {
    if (
      event.type === "keydown" &&
      (event.key === "Tab" || event.key === "Shift")
    ) {
      return;
    }
    setDrawerOpen(open);
  };

  const navItemsUnauth = ["Home", "About", "Services", "Contact", "Login"];
  const navItemsAuth = ["Home", "Employees", "Services", "About", "Logout"];

  const navRoutes = {
    Home: "/home",
    About: "/about",
    Services: "/services",
    Contact: "/contact",
    Login: "/",
    Employees: '/employees'
    // Account: "/account",
  };

  const navItems = isAuthenticated ? navItemsAuth : navItemsUnauth;

  // Render the logout button with the logout handler
  const renderNavButton = (item) => {
    if (item === "Logout") {
      return (
        <Button
          key={item}
          color="inherit"
          onClick={handleLogout}
          sx={{ ml: 1 }}
        >
          {item}
        </Button>
      );
    }
    return (
      <Button
        key={item}
        color="inherit"
        component={Link}
        to={navRoutes[item]}
        sx={{ ml: 1 }}
      >
        {item}
      </Button>
    );
  };

  // For mobile drawer, also handle logout
  const renderMobileNavItem = (item) => {
    if (item === "Logout") {
      return (
        <ListItem key={item} disablePadding>
          <ListItemButton
            sx={{ textAlign: "center" }}
            onClick={() => {
              handleLogout();
              toggleDrawer(false)();
            }}
          >
            <ListItemText primary={item} />
          </ListItemButton>
        </ListItem>
      );
    }
    return (
      <ListItem key={item} disablePadding>
        <ListItemButton
          sx={{ textAlign: "center" }}
          component={Link}
          to={navRoutes[item]}
          onClick={toggleDrawer(false)}
        >
          <ListItemText primary={item} />
        </ListItemButton>
      </ListItem>
    );
  };

  return (
    <Box sx={{ flexGrow: 1 }}>
      <AppBar position="static">
        <Toolbar>
          <IconButton
            size="large"
            edge="start"
            color="inherit"
            aria-label="menu"
            sx={{ mr: 2, display: { sm: "none" } }}
            onClick={toggleDrawer(true)}
          >
            <MenuIcon />
          </IconButton>
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            MotorPH: Employee Management System
          </Typography>
          <Box
            sx={{ display: { xs: "none", sm: "flex" }, alignItems: "center" }}
          >
            {navItems.map((item) => renderNavButton(item))}
          </Box>
        </Toolbar>
      </AppBar>
      <Drawer
        open={drawerOpen}
        onClose={toggleDrawer(false)}
        anchor="left"
        sx={{
          display: { xs: "block", sm: "none" },
          "& .MuiDrawer-paper": { boxSizing: "border-box", width: 240 },
        }}
      >
        <List>{navItems.map((item) => renderMobileNavItem(item))}</List>
      </Drawer>
    </Box>
  );
}

export default Header;