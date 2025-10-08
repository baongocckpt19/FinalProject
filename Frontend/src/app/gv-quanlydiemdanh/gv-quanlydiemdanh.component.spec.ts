import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GvQuanlydiemdanhComponent } from './gv-quanlydiemdanh.component';

describe('GvQuanlydiemdanhComponent', () => {
  let component: GvQuanlydiemdanhComponent;
  let fixture: ComponentFixture<GvQuanlydiemdanhComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GvQuanlydiemdanhComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GvQuanlydiemdanhComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
