export function formatArabicNumber(num: number): string {
  return new Intl.NumberFormat('ar-AE').format(num);
}

export function formatArabicCurrency(amount: number, currency: string): string {
  return new Intl.NumberFormat('ar-AE', {
    style: 'currency',
    currency,
  }).format(amount);
}

export function formatArabicDate(date: Date): string {
  return new Intl.DateTimeFormat('ar-AE', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  }).format(date);
}
