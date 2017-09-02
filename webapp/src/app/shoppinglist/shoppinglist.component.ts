import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

import { ShoppingListService } from '../services/shoppinglist.service';
import { ShoppingListItem } from '../model/shoppinglist';

@Component({
  selector: 'my-shoppinglist',
  templateUrl: './shoppinglist.component.html',
  styleUrls: ['./shoppinglist.component.css'],
  providers: [ShoppingListService]
})
export class ShoppingListComponent implements OnInit {

    title = 'Our Shopping List';
    shoppingList: ShoppingListItem[];
    selectedItem: ShoppingListItem;

    constructor(
      private router: Router,
      private shoppingListService: ShoppingListService) { }

    ngOnInit(): void {
        this.initShoppingList();
    }

    initShoppingList(): void {
        this.shoppingListService.getItems().then(shoppingList => this.shoppingList = shoppingList);
    }

    onSelect(item: ShoppingListItem): void {
        this.selectedItem = item;
    }

    gotoDetail(): void {
      this.router.navigate(['/details', this.selectedItem.id]);
    }

    add(name: string, description: string, order: Number, read: boolean): void {
      name = name.trim()
      description = description.trim()
      if (!name || !description) { return; }
      this.shoppingListService.create(name, description, order, read)
        .then(item => {
          this.shoppingList.push(item);
          this.selectedItem = null;
          console.log(this.shoppingList);
        });
    }

    delete(item: ShoppingListItem): void {
        this.shoppingListService
            .delete(item.id)
            .then(() => {
                this.shoppingList = this.shoppingList.filter(i => i !== item);
                if (this.selectedItem === item) {
                    this.selectedItem = null;
                }
            });
    }
}
