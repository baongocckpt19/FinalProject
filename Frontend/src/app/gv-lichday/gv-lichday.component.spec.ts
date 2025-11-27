import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GvLichDayComponent } from './gv-lichday.component';

describe('GvLichdayComponent', () => {
  let component: GvLichDayComponent;
  let fixture: ComponentFixture<GvLichDayComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GvLichDayComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GvLichDayComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
