import { of, throwError } from 'rxjs';
import { PagedList } from './paged-list';
describe('PagedList', () => {
  it('tracks pages and retains the previous data when a later page fails', async () => {
    const fetch = jasmine
      .createSpy('fetch')
      .and.returnValue(
        of({ content: [{ id: '1' }], page: 0, size: 1, totalElements: 2, last: false }),
      );
    const list = new PagedList<{ id: string }>(fetch);
    await list.load(0);
    expect(list.rows).toEqual([{ id: '1' }]);
    expect(list.last).toBeFalse();
    fetch.and.returnValue(throwError(() => new Error('offline')));
    await list.load(1);
    expect(list.page).toBe(0);
    expect(list.rows).toEqual([{ id: '1' }]);
    expect(list.error).not.toBe('');
    expect(list.loading).toBeFalse();
  });
});
