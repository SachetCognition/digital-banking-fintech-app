import React from 'react';
import { useTranslation } from 'react-i18next';

export function toHijriString(date: Date): string {
  const formatter = new Intl.DateTimeFormat('ar-SA-u-ca-islamic', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  });
  return formatter.format(date);
}

interface HijriDateDisplayProps {
  date?: Date;
  className?: string;
}

const HijriDateDisplay: React.FC<HijriDateDisplayProps> = ({ date = new Date(), className }) => {
  const { i18n } = useTranslation();
  const isArabic = i18n.language === 'ar';

  const hijriDate = toHijriString(date);
  const gregorianDate = new Intl.DateTimeFormat(isArabic ? 'ar-AE' : 'en-US', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  }).format(date);

  return (
    <div className={className}>
      <div>{hijriDate}</div>
      <div>{gregorianDate}</div>
    </div>
  );
};

export default HijriDateDisplay;
