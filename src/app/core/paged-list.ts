import type { Observable } from 'rxjs';
import { firstValueFrom } from 'rxjs';
import type { PageView } from './models';
import { errorMessage } from './api.service';
export class PagedList<T> {
  rows: T[] = [];
  page = 0;
  last = true;
  loading = false;
  error = '';
  constructor(private fetchPage: (page: number) => Observable<PageView<T>>) {}
  async load(page = this.page) {
    if (this.loading) return;
    this.loading = true;
    this.error = '';
    try {
      const result = await firstValueFrom(this.fetchPage(page));
      this.rows = result.content;
      this.page = result.page;
      this.last = result.last;
    } catch (error) {
      this.error = errorMessage(error);
    } finally {
      this.loading = false;
    }
  }
}
