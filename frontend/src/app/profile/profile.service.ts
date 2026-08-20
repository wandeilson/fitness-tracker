import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export type Sex = 'MALE' | 'FEMALE';

export type ActivityLevel =
  | 'SEDENTARY'
  | 'LIGHTLY_ACTIVE'
  | 'MODERATELY_ACTIVE'
  | 'VERY_ACTIVE'
  | 'ATHLETE';

export interface ProfilePayload {
  fullName: string | null;
  age: number | null;
  weightKg: number | null;
  heightCm: number | null;
  sex: Sex | null;
  activityLevel: ActivityLevel | null;
}

export interface ProfileResponse extends ProfilePayload {
  email: string;
}

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = `${environment.apiUrl}/api/profile`;

  getProfile(): Observable<ProfileResponse> {
    return this.http.get<ProfileResponse>(this.apiUrl);
  }

  saveProfile(payload: ProfilePayload): Observable<ProfileResponse> {
    return this.http.put<ProfileResponse>(this.apiUrl, payload);
  }
}
