import type { ValidatorFn } from '@angular/forms';
import { minorUnits } from './money';
export const moneyValidator: ValidatorFn = (control) => {
  const value: unknown = control.value;
  return typeof value === 'string' &&
    /^(?:0|[1-9]\d{0,11})(?:\.\d{1,2})?$/.test(value) &&
    minorUnits(value) > 0n
    ? null
    : { money: true };
};
export const accountIdValidator: ValidatorFn = (control) => {
  const value: unknown = control.value;
  return typeof value === 'string' &&
    /^[1-9]\d{0,18}$/.test(value) &&
    BigInt(value) <= 9223372036854775807n
    ? null
    : { accountId: true };
};
export const passwordBytesValidator: ValidatorFn = (control) =>
  new TextEncoder().encode(String(control.value)).length <= 72 ? null : { passwordBytes: true };
