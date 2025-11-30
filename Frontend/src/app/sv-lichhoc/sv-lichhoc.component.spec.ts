import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SvLichhocComponent } from './sv-lichhoc.component';

describe('SvLichhocComponent', () => {
  let component: SvLichhocComponent;
  let fixture: ComponentFixture<SvLichhocComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SvLichhocComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SvLichhocComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
