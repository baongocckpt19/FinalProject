import { Routes } from '@angular/router';
import { SlideshowComponent } from './slideshow/slideshow.component';
import { LoginComponent } from './login/login.component';

export const routes: Routes = [

    {
        path: 'slideshow',
        component: SlideshowComponent
    },
    {
        path: 'login',
        component: LoginComponent,
    },
    {
        path: '',
        redirectTo: '/slideshow',
        pathMatch: 'full'
    },
    {
        path: '**',
        redirectTo: '/slideshow',
    }
];
