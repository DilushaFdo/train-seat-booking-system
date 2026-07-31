import { ComponentFixture, TestBed } from '@angular/core/testing';

import { JourneySearchComponent } from './journey-search.component';

describe('JourneySearchComponent', () => {
  let component: JourneySearchComponent;
  let fixture: ComponentFixture<JourneySearchComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [JourneySearchComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(JourneySearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
