import {Injectable} from '@angular/core'
import { MatSnackBar, MatSnackBarConfig } from '@angular/material/snack-bar'
import {Subject} from 'rxjs'

@Injectable()
export class ErrorService {

  readonly actionCloseButtonName = 'CLOSE';

  readonly confOptionsSnackbar: MatSnackBarConfig<any> = {
    duration: 5000
  }; 

  constructor(private matSnackBar: MatSnackBar){}

  handle(message: string) {
    this.matSnackBar.open(message, this.actionCloseButtonName, this.confOptionsSnackbar);
  }

}