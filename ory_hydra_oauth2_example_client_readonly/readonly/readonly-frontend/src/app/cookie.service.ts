import { Injectable } from '@angular/core';
import { SsrCookieService } from 'ngx-cookie-service-ssr';
import * as path from 'path';

@Injectable()
export class CookieService {

  constructor(private cookieService: SsrCookieService) {}

  // возвращает куки с указанным name,
  // или undefined, если ничего не найдено
  getCookie(name: string): string | undefined {
    return this.cookieService.get(name);

    // let matches = document.cookie.match(new RegExp(
    //   "(?:^|; )" + name.replace(/([\.$?*|{}\(\)\[\]\\\/\+^])/g, '\\$1') + "=([^;]*)"
    // ));
    // return matches ? decodeURIComponent(matches[1]) : undefined;
  }

  setCookie(name: string, value: string, options: any = {} ) {

    // options = {
    //   path: '/',
    //   // при необходимости добавьте другие значения по умолчанию
    //   ...options
    // };

    // if (options.expires instanceof Date) {
    //   options.expires = options.expires.toUTCString();
    // }

    // let updatedCookie = encodeURIComponent(name) + "=" + encodeURIComponent(value);

    // for (let optionKey in options) {
    //   updatedCookie += "; " + optionKey;
    //   let optionValue = options[optionKey];
    //   if (optionValue !== true) {
    //     updatedCookie += "=" + optionValue;
    //   }
    // }

    // document.cookie = updatedCookie;

    this.cookieService.set(name, value, options?.['max-age'], options?.['path'], options?.['domain'], options?.['secure'], options?.['samesite']);
  }

  deleteCookie(name: string) {
    this.cookieService.deleteAll(name);

    // this.setCookie(name, "", {
    //   'max-age': -1
    // })
  }
}
