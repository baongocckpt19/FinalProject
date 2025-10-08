import { Routes } from '@angular/router';
import { SlideshowComponent } from './slideshow/slideshow.component';
import { LoginComponent } from './login/login.component';
import { GvTrangchuComponent } from './gv-trangchu/gv-trangchu.component';
import { GvQuanlylophocComponent } from './gv-quanlylophoc/gv-quanlylophoc.component';
import { GvQuanlydiemsoComponent } from './gv-quanlydiemso/gv-quanlydiemso.component';
import { GvQuanlydiemdanhComponent } from './gv-quanlydiemdanh/gv-quanlydiemdanh.component';
import { ChatbotComponent } from './chatbot/chatbot.component';

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
        path: 'gv_trangchu',
        component: GvTrangchuComponent,
    },
    {
        path: 'gv_quanlylophoc',
        component: GvQuanlylophocComponent,
    },
     {
        path: 'gv_quanlydiemso',
        component: GvQuanlydiemsoComponent,
    },
     {
        path: 'gv_quanlydiemdanh',
        component: GvQuanlydiemdanhComponent,
    },
     {
        path: 'chatbot',
        component: ChatbotComponent,
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
