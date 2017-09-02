import { Injectable } from '@angular/core';
import { Headers, Http } from '@angular/http';

import 'rxjs/add/operator/toPromise';

import { ShoppingListItem } from '../model/shoppinglist';

@Injectable()
export class ShoppingListService {

    private shoppinglistUrl = 'api/shoppinglist';

    private headers = new Headers({'Content-Type': 'application/json'});

    constructor(private http: Http) { }

    getItems(): Promise<ShoppingListItem[]> {
        return this.http.get(this.shoppinglistUrl)
                .toPromise()
                .then(response => response.json().data as ShoppingListItem[])
                .catch(this.handleError);
    }

    getItem(id: Number): Promise<ShoppingListItem> {
        const url = `${this.shoppinglistUrl}/${id}`;
        return this.http.get(url)
                .toPromise()
                .then(response => response.json().data as ShoppingListItem)
                .catch(this.handleError);
    }

    update(item: ShoppingListItem): Promise<ShoppingListItem> {
        const url = `${this.shoppinglistUrl}/${item.id}`
        return this.http.put(url, JSON.stringify(item), { headers: this.headers })
                .toPromise()
                .then(() => item)
                .catch(this.handleError);
    }

    create(name: string, description: string, order: Number, read: boolean): Promise<ShoppingListItem> {
        return this.http.post(
                this.shoppinglistUrl,
                JSON.stringify({
                    name: name,
                    description: description,
                    order: order,
                    read: read}),
                {
                    headers: this.headers
                })
                .toPromise()
                .then(response => response.json().data as ShoppingListItem)
                .catch(this.handleError);
    }

    delete(id: string): Promise<void> {
        const url = `${this.shoppinglistUrl}/${id}`;
        return this.http.delete(url, { headers: this.headers })
                .toPromise()
                .then(() => null)
                .catch(this.handleError);
    }

    private handleError(error: any): Promise<any> {
        console.error('An error occurred', error);
        return Promise.reject(error.message || error);
    }
}
