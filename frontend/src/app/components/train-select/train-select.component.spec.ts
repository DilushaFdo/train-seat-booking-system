import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TrainSelectComponent } from './train-select.component';

describe('TrainSelectComponent', () => {
  let component: TrainSelectComponent;
  let fixture: ComponentFixture<TrainSelectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TrainSelectComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TrainSelectComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
