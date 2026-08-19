import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { FoodResponse } from '../models/food.model';

@Injectable({ providedIn: 'root' })
export class FoodService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/foods';

  search(query: string): Observable<FoodResponse[]> {
    return this.http.get<FoodResponse[]>(this.apiUrl, { params: { q: query } });
  }
}
