import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { NotFoundComponent } from './not-found/not-found.component';
import { CookieService } from './cookie.service';
import { GenerationCookieCsrfService } from './generation-cookie-csrf.service';
import { ErrorService } from './error/error.service';

import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';

@NgModule({
  declarations: [
    AppComponent,
    NotFoundComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MatTooltipModule,
    MatSnackBarModule
  ],
  providers: [
    CookieService,
    GenerationCookieCsrfService,
    ErrorService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
