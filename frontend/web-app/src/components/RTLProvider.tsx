import React, { useEffect } from 'react';
import { useTranslation } from 'react-i18next';

export const RTLProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { i18n } = useTranslation();
  const isRTL = i18n.language === 'ar';

  useEffect(() => {
    document.documentElement.dir = isRTL ? 'rtl' : 'ltr';
    document.documentElement.lang = i18n.language;
  }, [i18n.language, isRTL]);

  return <div dir={isRTL ? 'rtl' : 'ltr'}>{children}</div>;
};
