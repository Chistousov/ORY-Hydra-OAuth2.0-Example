import { Injectable } from '@angular/core';

@Injectable()
export class WindowService {

  get(): any{
    return window;
  }
}
