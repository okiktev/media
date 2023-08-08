import { Component, HostListener } from '@angular/core';
import { timer } from 'rxjs';
import { WebsocketService } from './websocket.service';
import { SettingsDialog } from './settings.dialog';
import { FilmDialog } from './film.dialog';

declare var window: any;

export interface Film {
	name?: string
	location?: string
	hash?: string
	width?: number
	height?: number
	audioTracks?: string[]
	imgPath?: string
	imgTimestamp?: number
}

export interface Row {
	films: Film[]
}

export interface Cfg {
	fimlsPerRow?: number | 3
	imgTimestamp?: number | 30
}

export interface Cmd {
	updateFilm: boolean | false
	play: boolean | false
}

@Component({
  selector: 'app-root',
  templateUrl: './app.root.html',
  styleUrls: ['./app.root.css'],
  providers: [WebsocketService, SettingsDialog, FilmDialog],
})
export class AppRoot {

	private films: Film[] | undefined
	cfg: Cfg = {} as Cfg
	editedFilm: Film = {} as Film
	previewWidth: string = 'auto'

	constructor(private ws: WebsocketService, private settingsDlg: SettingsDialog, private filmDlg: FilmDialog) {
	    ws.messages.subscribe(msg => {
	        if (msg.cmd.updateFilm) {
				this.editedFilm = msg.films[0]
				if (this.films) {
					for (var i = 0; i < this.films.length; ++i) {
						if (this.films[i].hash === this.editedFilm.hash) {
							this.films[i] = this.editedFilm
							break
						}
					}
				}
			} else {				
		        this.films = msg.films
		        this.cfg = msg.cfg
			}
	    });

		timer(5000, 30000).subscribe(
			() => {
				this.ws.messages.next({
					cfg: {} as Cfg,
					films: [{}],
					cmd: {} as Cmd
			    })
			}
		)
	}

	onPlay(f: Film): void {
		this.ws.messages.next({
			cfg: { },
			films: [{location: f.location}],
			cmd: { play: true } as Cmd
	    })
	}

	onEdit(f: Film): void {
		this.editedFilm = f;
		this.filmDlg.show();
	}

	openSettingsDlg() {
    	this.settingsDlg.show()
  	}

	private getFilmsPerRow(): number {
		let res = this.cfg?.fimlsPerRow
		return res ? res : 3
	}

	getGrid(): Row[] {
		if (!this.films) {
			return []
		}
		let filmsPerRow = this.getFilmsPerRow()
		if (!filmsPerRow) {
			return []
		}
		let rows: Row[] = []
		let rowIdx = 0
		let count = 0
		this.films.forEach(f => {
			if (count === 0) {
				rows.push({films: []})
			}
			if (count < filmsPerRow) {
				rows[rowIdx].films.push(f)
			}
			if (count === filmsPerRow - 1) {
				++rowIdx
				count = -1	
			}
			++count
		})
		this.calculatePreviewWidth(filmsPerRow)
		return rows
	}
	
	@HostListener('window:resize', ['$event'])
    onResize() {
		let filmsPerRow = this.cfg?.fimlsPerRow
		if (filmsPerRow) {
			this.calculatePreviewWidth(filmsPerRow)
		}
    }
    
    private calculatePreviewWidth(filmsPerRow: number): void {
		this.previewWidth = (window.innerWidth - filmsPerRow * 15 - 10) / filmsPerRow + 'px'
	}
}
