import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GvTrangchuComponent } from './gv-trangchu.component';

describe('GvTrangchuComponent', () => {
  let component: GvTrangchuComponent;
  let fixture: ComponentFixture<GvTrangchuComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GvTrangchuComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GvTrangchuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
