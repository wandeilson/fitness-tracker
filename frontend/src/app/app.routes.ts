import { Routes } from '@angular/router';
import { authGuard } from './auth/auth.guard';
import { LayoutComponent } from './layout/layout.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { GoalsComponent } from './goals/goals.component';
import { LoginComponent } from './auth/login.component';
import { ProfileComponent } from './profile/profile.component';
import { RegisterComponent } from './auth/register.component';
import { DailyLogPageComponent } from './meals/daily-log-page.component';

export const routes: Routes = [
	{ path: 'login', component: LoginComponent },
	{ path: 'register', component: RegisterComponent },
	{
		path: '',
		component: LayoutComponent,
		canActivate: [authGuard],
		children: [
			{ path: '', component: DashboardComponent },
			{ path: 'daily', component: DailyLogPageComponent },
			{ path: 'goals', component: GoalsComponent },
			{ path: 'profile', component: ProfileComponent }
		]
	},
	{ path: '**', redirectTo: '' }
];
