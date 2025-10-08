import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GvQuanlylophocComponent } from './gv-quanlylophoc.component';

describe('GvQuanlylophocComponent', () => {
  let component: GvQuanlylophocComponent;
  let fixture: ComponentFixture<GvQuanlylophocComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GvQuanlylophocComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GvQuanlylophocComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
