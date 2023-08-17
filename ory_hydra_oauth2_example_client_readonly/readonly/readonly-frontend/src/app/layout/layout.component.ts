import { Component } from '@angular/core';
import { GenerationCookieCsrfService } from '../generation-cookie-csrf.service';
import { WindowService } from '../window.service';
import { Subject, takeUntil } from 'rxjs';
import { AuthService } from '../auth.service';
import { environment } from 'src/environments/environment';
import { SaveFutureRedirectService } from '../save-future-redirect.service';


@Component({
  selector: 'app-layout',
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss']
})
export class LayoutComponent {

  // для очистки памяти rxjs
  private readonly onDestroy = new Subject<void>();

  constructor(private generationCookieCsrfService: GenerationCookieCsrfService,
              private authService: AuthService,
              private windowService: WindowService,
              private saveFutureRedirectService: SaveFutureRedirectService
              ) {

  }

  logout() {

    this.generationCookieCsrfService.generateCookieCsrf();

    let logoutHandler: (() => void) = () => {

      //запоминаем в cookie текущий url
      this.saveFutureRedirectService.saveIfNotExist();

      this.windowService.get().location.href = `${environment.authServerBaseUrl}/oauth2/sessions/logout`;
    };

    this.authService.logout()
      .pipe(
        takeUntil(this.onDestroy)
      )
      .subscribe({
        next: logoutHandler,
        error: logoutHandler,
        complete: logoutHandler
      });


  }

  ngOnDestroy(): void {
    this.onDestroy.next();
    this.onDestroy.complete();
  }
}
