import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { MatTooltipModule } from '@angular/material/tooltip';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { TestbedHarnessEnvironment } from '@angular/cdk/testing/testbed';
import { HarnessLoader } from '@angular/cdk/testing';
import { MatTooltipHarness } from '@angular/material/tooltip/testing';
import { filter, from, merge, mergeMap } from 'rxjs';

describe('AppComponent', () => {

  let component: AppComponent;
  let fixture: ComponentFixture<AppComponent>;

  let loader: HarnessLoader;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [
        MatTooltipModule,
        NoopAnimationsModule
      ],
      declarations: [AppComponent],
    })
      .overrideTemplate(AppComponent, `<button matTooltip="Static message 1" id="one">Trigger 1</button><button matTooltip="Static message 2" id="two">Trigger 2</button>`)
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AppComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    loader = TestbedHarnessEnvironment.loader(fixture);
  });

  it('should create the app', async () => {
    expect(component).toBeTruthy();
    const tooltips = await loader.getAllHarnesses(MatTooltipHarness);

    expect(tooltips.length).toBe(2);
  });

});

