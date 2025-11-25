import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SvSidebarComponent } from './sv-sidebar.component';

describe('SvSidebarComponent', () => {
  let component: SvSidebarComponent;
  let fixture: ComponentFixture<SvSidebarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SvSidebarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SvSidebarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
