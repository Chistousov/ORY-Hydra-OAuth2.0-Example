import { Component, HostListener, QueryList, ViewChildren } from '@angular/core';
import { MatTooltip } from '@angular/material/tooltip';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {

  @ViewChildren(MatTooltip) allMatTooltip !: QueryList<MatTooltip>;

  @HostListener('window:keydown', ['$event']) onKeydownHandler(event: KeyboardEvent) {
    if (!event.ctrlKey) { return; }

    if (event.metaKey || event?.key === 'OS') {
      if (this.allMatTooltip) {
        this.allMatTooltip.forEach(matTooltip => matTooltip.show());
        setTimeout(() => {
          this.allMatTooltip.forEach(matTooltip => matTooltip.hide());
        }, 3000);
      }
      return false;
    }
    return;
  }

}
