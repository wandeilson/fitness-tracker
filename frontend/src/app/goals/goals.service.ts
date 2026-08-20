import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

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
  validFrom: string;
  validUntil: string | null;
  updatedAt: string;
}

@Injectable({ providedIn: 'root' })
export class GoalsService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/goals`;

  getGoal(): Observable<GoalResponse> {
    return this.http.get<GoalResponse>(this.apiUrl);
  }

  saveGoal(payload: GoalPayload): Observable<GoalResponse> {
    return this.http.put<GoalResponse>(this.apiUrl, payload);
  }
}
