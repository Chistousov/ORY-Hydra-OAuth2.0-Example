import { inject } from '@angular/core';
import { CanActivateFn } from '@angular/router';
import { AuthService } from './auth.service';
import { catchError, first, map, of } from 'rxjs';
import { SaveFutureRedirectService } from './save-future-redirect.service';
import { WindowService } from './window.service';

export const authGuard: CanActivateFn = (route, state) => {

  const authService = inject(AuthService);
  const saveFutureRedirectService = inject(SaveFutureRedirectService);
  const windowService = inject(WindowService);

  console.log('!!!!');

  return authService.checkAuthenticate()
    .pipe(
      first(),
      map(() => {

        //получаем сохраненый url, удаляем и перередиректуемся
       saveFutureRedirectService.redirectAndClearIfExist();

        return true;
      }),
      catchError(error => {
        
        console.log('errrroxxxxr ', error);
        console.log('1 ', error?.error);
        console.log('2 ', error?.error?.redirect_to);

        if (error?.error?.redirect_to && error?.status === 401) {

          //запоминаем в cookie текущий url
          saveFutureRedirectService.saveIfNotExist();

          console.log('zzxcxzczxc');

          // перенаправляем напрямую приложение
          windowService.get().location.href = error.error.redirect_to;
        } else {
          windowService.get().location.href = '/error'
        }

        return of(false);
      })
    );
};
