import { Routes } from '@angular/router';
import { SlideshowComponent } from './slideshow/slideshow.component';
import { LoginComponent } from './login/login.component';
import { GvTrangchuComponent } from './gv-trangchu/gv-trangchu.component';
import { GvQuanlylophocComponent } from './gv-quanlylophoc/gv-quanlylophoc.component';
import { GvQuanlydiemsoComponent } from './gv-quanlydiemso/gv-quanlydiemso.component';

import { ChatbotComponent } from './chatbot/chatbot.component';
import { TrangcanhanComponent } from './trangcanhan/trangcanhan.component';
import { AdminComponent } from './admin/admin.component';
import { roleGuard } from './guards/role.guard';
import { authGuard } from './guards/auth.guard';

import { SvTrangchuComponent } from './sv-trangchu/sv-trangchu.component';
import { SvDiemsoComponent } from './sv-diemso/sv-diemso.component';

import { GvLichDayComponent } from './gv-lichday/gv-lichday.component';
import { GvQuanlyvantayComponent } from './gv-quanlyvantay/gv-quanlyvantay.component';
import { SvLichHocComponent } from './sv-lichhoc/sv-lichhoc.component';


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
        path: 'gv_lichday',
        component: GvLichDayComponent,
    },
    {
        path: 'chatbot',
        component: ChatbotComponent, canActivate: [authGuard]
    },
    {
        path: 'trangcanhan',
        component: TrangcanhanComponent,
    },
    {
        path: 'admin',
        component: AdminComponent, canMatch: [roleGuard(['Admin'])]
    },
   
    {
        path: 'sv-trangchu',
        component: SvTrangchuComponent,
    },
    {
        path: 'sv-diemso',
        component:SvDiemsoComponent,
    },
    {
        path: 'gv-quanlyvantay',
        component: GvQuanlyvantayComponent,
    },
    {
        path:'sv-lichhoc',
        component: SvLichHocComponent,
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
