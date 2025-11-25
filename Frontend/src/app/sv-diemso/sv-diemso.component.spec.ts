import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SvDiemsoComponent } from './sv-diemso.component';

describe('SvDiemsoComponent', () => {
  let component: SvDiemsoComponent;
  let fixture: ComponentFixture<SvDiemsoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SvDiemsoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SvDiemsoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
