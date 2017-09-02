import { Component, OnInit } from '@angular/core';

import { ShoppingListItem } from '../model/shoppinglist'
import { ShoppingListService } from '../services/shoppinglist.service'

@Component({
  selector: 'my-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: [
      './dashboard.component.css'
  ]
})
export class DashboardComponent {
    shoppinglist: ShoppingListItem[] = []

    constructor(private shoppingListService: ShoppingListService) { }

    ngOnInit(): void {
        this.shoppingListService.getItems().then(
                items => this.shoppinglist = items.slice(0,4)
            )
    }
}
