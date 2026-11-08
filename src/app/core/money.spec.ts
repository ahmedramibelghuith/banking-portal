import { FormControl } from '@angular/forms';
import { minorUnits, formatUsd } from './money';
import { accountIdValidator, moneyValidator, passwordBytesValidator } from './form-validators';
describe('Money and identifier boundaries', () => {
  it('keeps cents at the maximum database balance and below one dollar', () => {
    expect(formatUsd('99999999999999999.99')).toBe('$99,999,999,999,999,999.99');
    expect(formatUsd('0.01')).toBe('$0.01');
    expect(formatUsd('-0.01')).toBe('-$0.01');
    expect(minorUnits('0.10') + minorUnits('0.20')).toBe(30n);
  });
  it('rejects invalid money without floating-point conversion', () => {
    for (const value of ['', '0', '-1', '1.001', '1000000000000.00'])
      expect(moneyValidator(new FormControl(value))).not.toBeNull();
    expect(moneyValidator(new FormControl('999999999999.99'))).toBeNull();
  });
  it('preserves large valid IDs and rejects values outside the Java Long range', () => {
    expect(accountIdValidator(new FormControl('9007199254740993'))).toBeNull();
    expect(accountIdValidator(new FormControl('9223372036854775807'))).toBeNull();
    for (const value of ['0', '-1', '9223372036854775808', '1.2', 'x'])
      expect(accountIdValidator(new FormControl(value))).not.toBeNull();
  });
  it('checks the same UTF-8 password byte ceiling as the API', () => {
    expect(passwordBytesValidator(new FormControl('界'.repeat(25)))).toEqual({
      passwordBytes: true,
    });
    expect(passwordBytesValidator(new FormControl('界'.repeat(24)))).toBeNull();
  });
});
