import { NgModule } from '@angular/core';
import { BrowserModule }  from '@angular/platform-browser';
import { FormsModule }   from '@angular/forms';
import { HttpModule } from '@angular/http';

import { CookieService } from 'ngx-cookie-service';

// Mock Services
import { InMemoryWebApiModule } from 'angular-in-memory-web-api';

import { InMemoryDataService }  from './services/in-memory-data.service';
import { ShoppingListService } from './services/shoppinglist.service';
import { ShoppingListItemSearchService } from './services/item-search.service';
import { LocalStorageService } from './services/local-storage.service';
import { AuthorizationService } from './services/authorization.service';
import { LoggingService } from './services/logging.service';

import { LoginComponent } from './login/login.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { ItemDetailsComponent } from './item-details/item-details.component';
import { ShoppingListComponent } from './shoppinglist/shoppinglist.component';
import { ShoppingListItemSearchComponent } from './item-search/item-search.component';

import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';

@NgModule({
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    InMemoryWebApiModule.forRoot(InMemoryDataService),
    AppRoutingModule
  ],
  declarations: [
    AppComponent,
    LoginComponent,
    DashboardComponent,
    ItemDetailsComponent,
    ShoppingListComponent,
    ShoppingListItemSearchComponent
  ],
  providers: [
      ShoppingListService,
      ShoppingListItemSearchService,
      CookieService,
      LocalStorageService,
      AuthorizationService,
      LoggingService
  ],
  bootstrap: [ AppComponent ]
})
export class AppModule { }
