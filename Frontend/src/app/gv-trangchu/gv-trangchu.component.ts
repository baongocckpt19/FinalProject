import { Component } from '@angular/core';
import { Router } from '@angular/router';
@Component({
  selector: 'app-gv-trangchu',
  imports: [],
  templateUrl: './gv-trangchu.component.html',
  styleUrl: './gv-trangchu.component.scss'
})
export class GvTrangchuComponent {
  constructor(private router: Router) { }
  
  navigateToDashboard() {
    this.router.navigate(['/gv_trangchu']);
  }
}
