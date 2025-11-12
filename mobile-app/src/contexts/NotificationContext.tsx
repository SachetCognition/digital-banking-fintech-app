import React, { createContext, useContext, useEffect, ReactNode } from 'react';
import PushNotification from 'react-native-push-notification';
import { Platform, PermissionsAndroid } from 'react-native';

interface NotificationContextType {
  scheduleLocalNotification: (title: string, message: string, data?: any) => void;
  cancelAllNotifications: () => void;
  requestPermissions: () => Promise<boolean>;
}

const NotificationContext = createContext<NotificationContextType | undefined>(undefined);

export const useNotification = () => {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error('useNotification must be used within a NotificationProvider');
  }
  return context;
};

interface NotificationProviderProps {
  children: ReactNode;
}

export const NotificationProvider: React.FC<NotificationProviderProps> = ({ children }) => {
  useEffect(() => {
    configurePushNotifications();
  }, []);

  const configurePushNotifications = () => {
    PushNotification.configure({
      onRegister: function (token) {
        console.log('TOKEN:', token);
        // Send token to your backend
      },
      onNotification: function (notification) {
        console.log('NOTIFICATION:', notification);
        // Handle notification
      },
      onAction: function (notification) {
        console.log('ACTION:', notification.action);
        console.log('NOTIFICATION:', notification);
      },
      onRegistrationError: function(err) {
        console.error(err.message, err);
      },
      permissions: {
        alert: true,
        badge: true,
        sound: true,
      },
      popInitialNotification: true,
      requestPermissions: Platform.OS === 'ios',
    });

    PushNotification.createChannel(
      {
        channelId: 'banking-notifications',
        channelName: 'Banking Notifications',
        channelDescription: 'Notifications for banking activities',
        playSound: true,
        soundName: 'default',
        importance: 4,
        vibrate: true,
      },
      (created) => console.log(`createChannel returned '${created}'`)
    );
  };

  const requestPermissions = async (): Promise<boolean> => {
    if (Platform.OS === 'android') {
      try {
        const granted = await PermissionsAndroid.request(
          PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS,
          {
            title: 'Notification Permission',
            message: 'This app needs notification permission to send you important updates.',
            buttonNeutral: 'Ask Me Later',
            buttonNegative: 'Cancel',
            buttonPositive: 'OK',
          }
        );
        return granted === PermissionsAndroid.RESULTS.GRANTED;
      } catch (err) {
        console.warn(err);
        return false;
      }
    }
    return true;
  };

  const scheduleLocalNotification = (title: string, message: string, data?: any) => {
    PushNotification.localNotification({
      channelId: 'banking-notifications',
      title,
      message,
      data,
      playSound: true,
      soundName: 'default',
      importance: 'high',
      priority: 'high',
    });
  };

  const cancelAllNotifications = () => {
    PushNotification.cancelAllLocalNotifications();
  };

  const value: NotificationContextType = {
    scheduleLocalNotification,
    cancelAllNotifications,
    requestPermissions,
  };

  return <NotificationContext.Provider value={value}>{children}</NotificationContext.Provider>;
};

