import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export interface ProfilePayload {
  fullName: string | null;
  age: number | null;
  weightKg: number | null;
  heightCm: number | null;
  sex: string | null;
  activityLevel: string | null;
}

export interface ProfileResponse extends ProfilePayload {
  email: string;
}

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api/profile';

  getProfile(): Observable<ProfileResponse> {
    return this.http.get<ProfileResponse>(this.apiUrl);
  }

  saveProfile(payload: ProfilePayload): Observable<ProfileResponse> {
    return this.http.put<ProfileResponse>(this.apiUrl, payload);
  }
}
