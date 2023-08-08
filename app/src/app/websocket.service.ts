import { Injectable } from "@angular/core"
import { Observable, Observer } from 'rxjs'
import { AnonymousSubject } from 'rxjs/internal/Subject'
import { Subject } from 'rxjs'
import { map } from 'rxjs/operators'
import { Env } from "./env"
import { Cmd, Cfg, Film } from "./app.root"


export interface Message {
    cfg: Cfg
    films: Film[]
    cmd: Cmd
}

@Injectable()
export class WebsocketService {

    private subject: AnonymousSubject<MessageEvent> | undefined;
    public messages: Subject<Message>;

    constructor() {
		console.log('$$$$ WebsocketService has been created')
        this.messages = <Subject<Message>>this.connect(Env.backendWebsocketUrl).pipe(
            map(
                (response: MessageEvent): Message => {
                    return JSON.parse(response.data)
                }
            )
        )
    }

    public connect(url: string): AnonymousSubject<MessageEvent> {
        if (!this.subject) {
            this.subject = this.create(url)
            console.log("Successfully connected to: " + url)
        }
        return this.subject
    }

    private create(url: string): AnonymousSubject<MessageEvent> {
        let ws = new WebSocket(url)
        let observable = new Observable((obs: Observer<MessageEvent>) => {
            ws.onmessage = obs.next.bind(obs)
            ws.onerror = obs.error.bind(obs)
            ws.onclose = obs.complete.bind(obs)
            return ws.close.bind(ws)
        })
        let observer: Observer<MessageEvent> = {
            error: () => {},
            complete: () => {},
            next: (data: Object) => {
                console.log('Message sent to websocket: ', data)
                if (ws.readyState === WebSocket.OPEN) {
                    ws.send(JSON.stringify(data))
                }
            }
        }
        return new AnonymousSubject<MessageEvent>(observer, observable)
    }
}