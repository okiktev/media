import { TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { AppRoot } from './app.root';

describe('AppRoot', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [RouterTestingModule],
    declarations: [AppRoot]
  }));

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppRoot);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

});
