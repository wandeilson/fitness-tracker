import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface GoalPayload {
  calories: number;
  carbsPercent: number;
  proteinPercent: number;
  fatPercent: number;
}

export interface GoalResponse extends GoalPayload {
  carbsCalories: number;
  proteinCalories: number;
  fatCalories: number;
  carbsG: number;
  proteinG: number;
  fatG: number;
  updatedAt: string;
}

@Injectable({ providedIn: 'root' })
export class GoalsService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/goals';

  getGoal(): Observable<GoalResponse> {
    return this.http.get<GoalResponse>(this.apiUrl);
  }

  saveGoal(payload: GoalPayload): Observable<GoalResponse> {
    return this.http.put<GoalResponse>(this.apiUrl, payload);
  }
}
