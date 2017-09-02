import { Component, Input, OnInit } from '@angular/core';

import { AuthorizationService } from '../services/authorization.service';
import { User } from '../model/user';

@Component({
    selector: 'login',
    templateUrl: './login.component.html',
    styleUrls: [
        './login.component.css'
    ]
})
export class LoginComponent implements OnInit {

    @Input() user: User;

    constructor(private authorizationService: AuthorizationService) {}

    ngOnInit(): void {

    }

    doLogin(username: string, password: string): void {
        console.log(`doLogin(${username}, ${password})`);
        this.authorizationService.authorize(username, password);
    }

    isLoggedin(): boolean {
        if (this.authorizationService.getAuthToken()) {
            return true;
        } else {
            return false;
        }
    }
}
