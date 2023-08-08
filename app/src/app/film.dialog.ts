import { Component, ElementRef, ViewChild, Input} from '@angular/core'
import { Cfg, Cmd, Film } from './app.root'
import { WebsocketService } from './websocket.service'
import { CommonModule } from '@angular/common'

declare var window: any


@Component({
  selector: 'film-dialog'
  , template: `
    <div class="modal fade" id="filmDlg" tabindex="-1" role="dialog" aria-labelledby="exampleModalCenterTitle1" aria-hidden="true">
	  <div class="modal-dialog modal-dialog-centered" role="document">
	    <div class="modal-content">
	      <div class="modal-header">
	        <h5 class="modal-title" >Film - {{ film?.name}}</h5>
	        <button type="button" class="close-btn" data-bs-dismiss="modal" aria-label="Close">
	        	<i class="fa fa-times" aria-hidden="true" ></i>
	        </button>
	      </div>
	      <div class="modal-body">
	      	<img [src]="'./' + film?.imgPath + '/' + film?.hash + '_' + film?.imgTimestamp + '.png'" />
	      	<div style="margin-top:10px">
		      	Current timestamp: <input #imgtsmp type="number" class="form-control" [value]="film?.imgTimestamp" style="width:100px;display:inline;background-color:lightgray;" />
				<button type="button" class="btn btn-secondary" style="background-color:transparent;border:none;color:darkgray;" (click)="onRegenerateImg()" ><i class="fa fa-refresh media-icon"></i></button><br/>
	      	</div>
			<i class="fa fa-film media-icon"></i>{{film?.width}}x{{film?.height}}<br/>
			<div *ngFor="let a of film?.audioTracks"><i class="fa fa-file-audio-o media-icon"></i>{{a}}</div><br/>
	      </div>
	      <div class="modal-footer">
	        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal" >Close</button>
	        <button type="button" class="btn btn-primary" data-bs-dismiss="modal" (click)="onSaveChanges()" >Save changes</button>
	      </div>
	    </div>
	  </div>
	</div>
	`
  , styles: [`
.modal-body img {
  width: -webkit-fill-available;
}
  `]
  , standalone: true
  , imports: [CommonModule]
})


export class FilmDialog {

	@Input() cfg: Cfg | undefined
	@Input() film: Film | undefined

	@ViewChild('imgtsmp') imgtsmp: ElementRef | undefined

	private modalDlg: any

	constructor(private ws: WebsocketService) {
	}

    show() {
		if (!this.modalDlg) {
			this.modalDlg = new window.bootstrap.Modal(
      			document.getElementById('filmDlg')
    		)
		}
		this.modalDlg.show()
    }

	onRegenerateImg() {
		if (this.imgtsmp) {			
			this.ws.messages.next({
				cfg: { imgTimestamp: +(this.imgtsmp?.nativeElement.value) },
				films: [{hash: this.film?.hash}],
				cmd: {}  as Cmd
		    })
		}
	}

    onSaveChanges() {
		console.log('$$$ saving film properties ')
	}

}
