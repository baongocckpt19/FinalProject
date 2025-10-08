import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GvQuanlydiemsoComponent } from './gv-quanlydiemso.component';

describe('GvQuanlydiemsoComponent', () => {
  let component: GvQuanlydiemsoComponent;
  let fixture: ComponentFixture<GvQuanlydiemsoComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [GvQuanlydiemsoComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GvQuanlydiemsoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
