import { Injectable } from '@angular/core'
import { Headers, Http } from '@angular/http';

import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';

import { LocalStorageService } from './local-storage.service';
import { LoggingService } from './logging.service';

@Injectable()
export class AuthorizationService {

    private TOKEN_KEY = "X-SLS-AUTHTOKEN";
    private AUTHORIZATION_HEADER = "Authorization";
    private authUrl = "/auth";
    private headers = new Headers({'Content-Type': 'application/json'});

    constructor(
        private http: Http,
        private localStorage: LocalStorageService,
        private log: LoggingService) {}

    authorize(name: string, password: string): void {
        this.log.info("foo");
        var token = this.http.post(
            this.authUrl,
            JSON.stringify({
                    name: name,
                    password: password}),
            {
                headers: this.headers
            }).toPromise()
            .then(response => response.json().data as Token)
            .then((token: Token) => {
                localStorage.store(this.TOKEN_KEY, token.token);
            })
            .catch(message => this.handleError(message.toString()));
    }

    refresh() {
        this.http.get(
            this.authUrl,
            {
                headers: this.getHeadersWithAuthToken()
            })
            .map(response => response.json().data as Token)
            .map((token: Token) => localStorage.setItem(this.TOKEN_KEY, token.token));
    }

    getHeadersWithAuthToken(): Headers {
        const authHeader: string = this.AUTHORIZATION_HEADER;
        const authValue: string = `Bearer ${this.getAuthToken()}`;
        return new Headers({
            'Content-Type': 'application/json',
            authHeader: authValue
        });
    }

    getAuthToken(): string {
        return localStorage.getItem(this.TOKEN_KEY);
    }

    handleError(error: any): Promise<any> {
        //console.log(this.log);
        this.log.error('An error occurred', error);
        return Promise.reject(error.message || error);
    }
}

class Token {
    token: string
}
