import { Component, Input } from '@angular/core'
import { Cfg, Cmd } from './app.root'
import { WebsocketService } from './websocket.service'
import { FormsModule } from '@angular/forms'
import { CommonModule } from '@angular/common'

declare var window: any

@Component({
  selector: 'settings-dialog'
  , template: `
    <div class="modal fade" id="settingsDlg" tabindex="-1" role="dialog" aria-labelledby="exampleModalCenterTitle" aria-hidden="true">
	  <div class="modal-dialog modal-dialog-centered" role="document">
	    <div class="modal-content">
	      <div class="modal-header">
	        <h5 class="modal-title" >Settings</h5>
	        <button type="button" class="close-btn" data-bs-dismiss="modal" aria-label="Close">
	        	<i class="fa fa-times" aria-hidden="true" ></i>
	        </button>
	      </div>
	      <div class="modal-body">
	        <span class="label label-default">Films per row:</span>
		    <select class="form-select" [(ngModel)]="filmsPerRowOption" [value]="cfg?.fimlsPerRow" style="background-color:lightgrey" >
		      <option *ngFor="let o of filmsPerRowOptions;" [value]="o">{{o}}</option>
			</select>
	      </div>
	      <div class="modal-footer">
	        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal" >Close</button>
	        <button type="button" class="btn btn-primary" data-bs-dismiss="modal" (click)="onSaveChanges()" >Save changes</button>
	      </div>
	    </div>
	  </div>
	</div>
	`
  , styles: [``]
  , standalone: true
  , imports: [FormsModule, CommonModule]

})


export class SettingsDialog {

	@Input() cfg: Cfg | undefined

	filmsPerRowOption: string = ''
  	filmsPerRowOptions: string[] = ['1','2','3','4','5']

	private modalDlg: any

	constructor(private ws: WebsocketService) {
	}

    show() {
		if (!this.modalDlg) {
			this.modalDlg = new window.bootstrap.Modal(
      			document.getElementById('settingsDlg')
    		)
		}
		this.modalDlg.show()
    }

    onSaveChanges() {
		this.ws.messages.next({
			cfg: { fimlsPerRow: +this.filmsPerRowOption },
			films: [{}],
			cmd: {}  as Cmd
	    })
	}

}
