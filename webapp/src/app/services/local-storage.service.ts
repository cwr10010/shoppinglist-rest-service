import { Injectable, OnInit } from '@angular/core';
import { CookieService } from 'ngx-cookie-service';

@Injectable()
export class LocalStorageService implements OnInit {

    localStorageAvailable: boolean = false;

    constructor(private cookieService: CookieService) { }

    ngOnInit(): void {
        if (localStorage) {
            this.localStorageAvailable = true;
        }
    }

    store(key: string, value: any): void {
        if (typeof value !== 'undefined' && value != null) {
            if (typeof value === 'object') {
                value = JSON.stringify(value);
            }

            if (this.localStorageAvailable) {
                localStorage.setItem(key, value);
            }
            else {
                this.cookieService.set(key, value, 30, '/', '', true);
            }
        }
        else {
            if (this.localStorageAvailable) {
                localStorage.removeItem(key);
            } else {
                this.cookieService.delete(key, '/');
            }
        }
    }

    read(key: string): any {
        var data;
        if (this.localStorageAvailable) {
            data = localStorage.getItem(key);
        }
        else {
            data = this.cookieService.get(key);
            try {
                data = JSON.parse(data);
            } catch (e) {
                data = data;
            }
        }
        return data;
    }
}
