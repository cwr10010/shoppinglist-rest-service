import { Injectable } from '@angular/core';
import { Http } from '@angular/http';

import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';

import { ShoppingListItem } from '../model/shoppinglist';

@Injectable()
export class ShoppingListItemSearchService {

    constructor(private http: Http) { }

    search(term: string): Observable<ShoppingListItem[]> {
        return this.http.get(`api/shoppinglist/?name=${term}`)
            .map(response => response.json().data as ShoppingListItem[]);
    }
}
