import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { FoodResponse } from '../models/food.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class FoodService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/foods`;

  search(query: string): Observable<FoodResponse[]> {
    return this.http.get<FoodResponse[]>(this.apiUrl, { params: { q: query } });
  }
}
