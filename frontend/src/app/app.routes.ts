import { Routes } from '@angular/router';
import { authGuard } from './auth/auth.guard';
import { DashboardComponent } from './dashboard/dashboard.component';
import { GoalsComponent } from './goals/goals.component';
import { LoginComponent } from './auth/login.component';
import { ProfileComponent } from './profile/profile.component';
import { RegisterComponent } from './auth/register.component';

export const routes: Routes = [
	{ path: 'login', component: LoginComponent },
	{ path: 'register', component: RegisterComponent },
	{ path: '', component: DashboardComponent, canActivate: [authGuard] },
	{ path: 'goals', component: GoalsComponent, canActivate: [authGuard] },
	{ path: 'profile', component: ProfileComponent, canActivate: [authGuard] },
	{ path: '**', redirectTo: '' }
];
