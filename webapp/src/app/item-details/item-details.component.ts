import { Component, Input, OnInit } from '@angular/core';
import { ActivatedRoute, ParamMap } from '@angular/router';
import { Location } from '@angular/common';

import 'rxjs/add/operator/switchMap';

import { ShoppingListService } from '../services/shoppinglist.service';
import { ShoppingListItem } from '../model/shoppinglist';

@Component({
    selector: 'item-details',
    templateUrl: './item-details.component.html',
    styleUrls: [
        './item-details.component.css'
    ]
})
export class ItemDetailsComponent implements OnInit {

    @Input() item: ShoppingListItem;

    constructor(
            private shoppingListService: ShoppingListService,
            private route: ActivatedRoute,
            private location: Location) { }

    ngOnInit(): void {
        this.route.paramMap
            .switchMap((params: ParamMap) => this.shoppingListService.getItem(+params.get('id')))
            .subscribe(item => this.item = item)
    }

    goBack(): void {
        this.location.back();
    }

    save(): void {
        this.shoppingListService.update(this.item)
            .then(() => this.goBack());
    }

}
