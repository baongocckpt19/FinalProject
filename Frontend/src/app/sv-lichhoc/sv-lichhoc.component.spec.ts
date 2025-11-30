import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SvLichHocComponent } from './sv-lichhoc.component';

describe('SvLichhocComponent', () => {
  let component: SvLichHocComponent;
  let fixture: ComponentFixture<SvLichHocComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SvLichHocComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SvLichHocComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
