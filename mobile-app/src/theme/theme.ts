import { DefaultTheme } from 'react-native-paper';

export const theme = {
  ...DefaultTheme,
  colors: {
    ...DefaultTheme.colors,
    primary: '#2563eb',
    primaryContainer: '#dbeafe',
    secondary: '#64748b',
    secondaryContainer: '#f1f5f9',
    surface: '#ffffff',
    surfaceVariant: '#f8fafc',
    background: '#f8fafc',
    error: '#dc2626',
    errorContainer: '#fef2f2',
    onPrimary: '#ffffff',
    onSecondary: '#ffffff',
    onSurface: '#1e293b',
    onBackground: '#1e293b',
    onError: '#ffffff',
    outline: '#e2e8f0',
    outlineVariant: '#f1f5f9',
  },
  fonts: {
    ...DefaultTheme.fonts,
    regular: {
      fontFamily: 'System',
      fontWeight: '400' as const,
    },
    medium: {
      fontFamily: 'System',
      fontWeight: '500' as const,
    },
    light: {
      fontFamily: 'System',
      fontWeight: '300' as const,
    },
    thin: {
      fontFamily: 'System',
      fontWeight: '100' as const,
    },
  },
};

