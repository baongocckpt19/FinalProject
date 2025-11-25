import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SvTrangchuComponent } from './sv-trangchu.component';

describe('SvTrangchuComponent', () => {
  let component: SvTrangchuComponent;
  let fixture: ComponentFixture<SvTrangchuComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SvTrangchuComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SvTrangchuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
