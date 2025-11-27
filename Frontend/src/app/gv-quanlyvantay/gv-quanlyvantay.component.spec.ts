import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GvQuanlyvantayComponent } from './gv-quanlyvantay.component';

describe('GvQuanlyvantayComponent', () => {
  let component: GvQuanlyvantayComponent;
  let fixture: ComponentFixture<GvQuanlyvantayComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GvQuanlyvantayComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GvQuanlyvantayComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
