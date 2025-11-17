import { Component } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { SidebarComponent } from "./sidebar/sidebar.component";
import { CommonModule } from '@angular/common';


@Component({
  selector: 'app-root',
  imports: [RouterOutlet, SidebarComponent, CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'Frontend';

  isLogInPage = false;
  isSlideshow = false;
  isAdmin = false;

  constructor(private router: Router) {
    this.router.events.subscribe(event => {
      if (event instanceof NavigationEnd) {

        this.isLogInPage = event.url === '/login';
        this.isSlideshow = event.url === '/slideshow';
        this.isAdmin = event.url === '/admin';

      }
    });
  }
}
