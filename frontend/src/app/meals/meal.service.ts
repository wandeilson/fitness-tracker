import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  DailySummaryResponse,
  MealCreateRequest,
  MealItemRequest,
  MealResponse,
} from '../models/meal.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class MealService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/meals`;

  getMeals(date: string): Observable<MealResponse[]> {
    return this.http.get<MealResponse[]>(this.apiUrl, { params: { date } });
  }

  getSummary(date: string): Observable<DailySummaryResponse> {
    return this.http.get<DailySummaryResponse>(`${this.apiUrl}/summary`, {
      params: { date },
    });
  }

  createMeal(payload: MealCreateRequest): Observable<MealResponse> {
    return this.http.post<MealResponse>(this.apiUrl, payload);
  }

  addItem(mealId: number, payload: MealItemRequest): Observable<MealResponse> {
    return this.http.post<MealResponse>(`${this.apiUrl}/${mealId}/items`, payload);
  }

  updateItem(mealId: number, itemId: number, grams: number): Observable<MealResponse> {
    return this.http.put<MealResponse>(`${this.apiUrl}/${mealId}/items/${itemId}`, { grams });
  }

  deleteItem(mealId: number, itemId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${mealId}/items/${itemId}`);
  }

  deleteMeal(mealId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${mealId}`);
  }
}
