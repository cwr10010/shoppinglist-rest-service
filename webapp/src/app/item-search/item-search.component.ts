import { Component, OnInit } from '@angular/core';
import { Router }            from '@angular/router';

import { Observable }        from 'rxjs/Observable';
import { Subject }           from 'rxjs/Subject';

// Observable class extensions
import 'rxjs/add/observable/of';

// Observable operators
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/distinctUntilChanged';

import { ShoppingListItemSearchService } from '../services/item-search.service';
import { ShoppingListItem } from '../model/shoppinglist';

@Component({
    selector: 'item-search',
    templateUrl: './item-search.component.html',
    styleUrls: [
        './item-search.component.css'
    ],
    providers: [ ShoppingListItemSearchService ]
})
export class ShoppingListItemSearchComponent implements OnInit {

    shoppinglist: Observable<ShoppingListItem[]>;
    private searchTerms = new Subject<string>();

    constructor(private itemSearchService: ShoppingListItemSearchService, private router: Router) { }

    search(term: string): void {
        this.searchTerms.next(term);
    }

    ngOnInit(): void {
        this .shoppinglist = this.searchTerms
            .debounceTime(300)
            .distinctUntilChanged()
            .switchMap(term => term ? this.itemSearchService.search(term) : Observable.of<ShoppingListItem[]>([]))
            .catch(error => {
                console.log(error);
                return Observable.of<ShoppingListItem[]>([]);
            })
    }

    gotoDetails(item: ShoppingListItem): void {
        let link = ['/details', item.id];
        this.router.navigate(link);
    }
}
