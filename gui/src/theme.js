// theme.js or theme.ts
import { createTheme } from '@mui/material/styles';

const theme = createTheme({
  palette: {
    mode: 'light',
    background: {
      default: '#ffffff', // White background for app
      paper: '#ffffff',   // Paper is also white
    },
    text: {
      primary: '#212121',   // Strong readable black text
      secondary: '#616161', // Slightly softer for secondary
      disabled: '#9e9e9e',  // Light grey for disabled
    },
    primary: {
      main: '#455a64',       // Neutral blue-grey (e.g., buttons, links)
      contrastText: '#ffffff',
    },
    secondary: {
      main: '#607d8b',       // Another muted neutral for accents
      contrastText: '#ffffff',
    },
    divider: '#e0e0e0',      // Light grey divider, less visually intrusive
    action: {
      hover: '#f5f5f5',
      selected: '#eeeeee',
      disabled: '#cccccc',
      disabledBackground: '#f2f2f2',
    },
  },

  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
    fontSize: 14,
    h6: {
      fontWeight: 500,
    },
    body1: {
      fontSize: '0.95rem',
    },
    body2: {
      fontSize: '0.875rem',
      color: '#424242',
    },
    button: {
      textTransform: 'none',
      fontWeight: 500,
    },
  },

  components: {
    MuiAppBar: {
      styleOverrides: {
        root: {
          backgroundColor: '#ffffff',
          color: '#212121',
          boxShadow: '0 1px 2px rgba(0,0,0,0.05)', // very subtle shadow
        },
      },
    },
    MuiButton: {
      defaultProps: {
        size: 'medium',
        disableElevation: true,
      },
      styleOverrides: {
        root: {
          borderRadius: 6,
        },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          boxShadow: 'none', // Remove aggressive shadows
          border: '1px solid #e0e0e0',
        },
      },
    },
    MuiToolbar: {
      styleOverrides: {
        regular: {
          minHeight: 56,
        },
      },
    },
  },
});

export default theme;