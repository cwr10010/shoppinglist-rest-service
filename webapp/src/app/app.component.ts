import { Component } from '@angular/core';

import '../assets/css/styles.css';

@Component({
  selector: 'my-app',
  template: `
    <h1>{{title}}</h1>
    <login></login>
    <nav>
    <a routerLink="/dashboard" routerLinkActive="active">Dashboard</a>
    <a routerLink="/shoppinglist" routerLinkActive="active">Shoppinglist</a>
    </nav>
    <router-outlet></router-outlet>
  `,
  styleUrls: [
    './app.component.css'
  ]
})
export class AppComponent {
  title = 'Shopping List App';
}
