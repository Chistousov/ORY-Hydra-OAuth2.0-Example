import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Point } from './models/point.model';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable()
export class AppService {

  constructor(private http: HttpClient) {}

  getData(): Observable<Point[]> {
    return this.http.get<Point[]>(`${environment.apiUrl}/data`);
  }
}
