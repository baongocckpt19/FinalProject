import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SvDiemdanhComponent } from './sv-diemdanh.component';

describe('SvDiemdanhComponent', () => {
  let component: SvDiemdanhComponent;
  let fixture: ComponentFixture<SvDiemdanhComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SvDiemdanhComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SvDiemdanhComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
