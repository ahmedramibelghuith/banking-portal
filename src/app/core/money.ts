import type { PipeTransform } from '@angular/core';
import { Pipe } from '@angular/core';
export function minorUnits(value: string): bigint {
  if (!/^-?\d+(?:\.\d{1,2})?$/.test(value)) throw new Error('Invalid decimal money');
  const negative = value.startsWith('-');
  const [whole, fraction = ''] = (negative ? value.slice(1) : value).split('.');
  const cents = BigInt(whole) * 100n + BigInt(fraction.padEnd(2, '0'));
  return negative ? -cents : cents;
}
export function formatUsd(value: string): string {
  const cents = minorUnits(value),
    absolute = cents < 0n ? -cents : cents;
  return `${cents < 0n ? '-' : ''}$${new Intl.NumberFormat('en-US').format(absolute / 100n)}.${String(absolute % 100n).padStart(2, '0')}`;
}
@Pipe({ name: 'money', standalone: true })
export class MoneyPipe implements PipeTransform {
  transform(value: string): string {
    return formatUsd(value);
  }
}
