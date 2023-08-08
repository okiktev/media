import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { AppRoot } from './app.root';
import { HttpClientModule } from '@angular/common/http';
import { SettingsDialog } from './settings.dialog';
import { FormsModule } from '@angular/forms';
import { ReactiveFormsModule } from '@angular/forms';
import { FilmDialog } from './film.dialog';


@NgModule({
  declarations: [
    AppRoot
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    SettingsDialog,
    FilmDialog,
    FormsModule,
    ReactiveFormsModule
  ],
  providers: [],
  bootstrap: [AppRoot]
})
export class AppModule { }

