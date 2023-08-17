import { Component, OnInit, ViewChild, Inject, PLATFORM_ID, OnDestroy } from '@angular/core';
import { ChartConfiguration, ChartType } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';

import { isPlatformBrowser } from "@angular/common";
import { AppService } from '../app.service';
import { Observable, Subject, first, map, takeUntil } from 'rxjs';

@Component({
  selector: 'app-line-chart',
  templateUrl: './line-chart.component.html',
  styleUrls: ['./line-chart.component.scss']
})
export class LineChartComponent implements OnInit, OnDestroy {

  // для очистки памяти rxjs
  private readonly onDestroy = new Subject<void>();

  public isBrowser: boolean = false;

  public lineChartOptions: ChartConfiguration['options'] = {
    elements: {
      line: {
        tension: 0.5,
      },
    },
    scales: {
      y: {
        position: 'left',
      }
    },
    plugins: {
      legend: { display: true },
    },
  };
  public lineChartType: ChartType = 'line';

  public lineChartData$: Observable<ChartConfiguration['data']>

  constructor(@Inject(PLATFORM_ID) private platformId: any,
    private appService: AppService
  ) {
    this.lineChartData$ = this.appService.getData()
      .pipe(
        first(),
        map(points => {
          let x: number[] = points.map((value) => value.x);
          let y: number[] = points.map((value) => value.y);

          return {
            datasets: [
              {
                data: y,
                label: 'some data',
                backgroundColor: 'rgba(255,0,0,0.3)',
                borderColor: 'red',
                pointBackgroundColor: 'rgba(148,159,177,1)',
                pointBorderColor: '#fff',
                pointHoverBackgroundColor: '#fff',
                pointHoverBorderColor: 'rgba(148,159,177,0.8)',
                fill: 'origin',
              },
            ],
            labels: x,
          }

        }),
        takeUntil(this.onDestroy)
      )

  }

  ngOnInit(): void {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  ngOnDestroy(): void {
    this.onDestroy.next();
    this.onDestroy.complete();
  }

}
