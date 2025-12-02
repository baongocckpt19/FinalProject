import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GvAireviewComponent } from './gv-aireview.component';

describe('GvAireviewComponent', () => {
  let component: GvAireviewComponent;
  let fixture: ComponentFixture<GvAireviewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GvAireviewComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GvAireviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
