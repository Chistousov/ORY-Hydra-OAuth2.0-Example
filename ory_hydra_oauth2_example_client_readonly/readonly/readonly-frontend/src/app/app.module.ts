import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { GenerationCookieCsrfService } from './generation-cookie-csrf.service';
import { CookieService } from './cookie.service';
import { NotFoundComponent } from './not-found/not-found.component';
import { ErrorComponent } from './error/error.component';
import { WindowService } from './window.service';

import {MatToolbarModule} from '@angular/material/toolbar';

import {MatButtonModule} from '@angular/material/button';
import { SaveFutureRedirectService } from './save-future-redirect.service';
import { AuthService } from './auth.service';
import { HttpClientModule } from '@angular/common/http';
import { LayoutComponent } from './layout/layout.component';
import { LineChartComponent } from './line-chart/line-chart.component';

import { NgChartsModule } from 'ng2-charts';
import { AppService } from './app.service';

@NgModule({
  declarations: [
    AppComponent,
    NotFoundComponent,
    ErrorComponent,
    LayoutComponent,
    LineChartComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    MatToolbarModule,
    MatButtonModule,
    HttpClientModule,
    NgChartsModule
  ],
  providers: [
    CookieService,
    GenerationCookieCsrfService,
    WindowService,
    SaveFutureRedirectService,
    AuthService,
    AppService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
