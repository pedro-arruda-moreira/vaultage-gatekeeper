import { Debug } from "./Debug";

export class Log {
    static debug(msg: any): void {
        if(Debug.enabled) return;
        console.log(msg);
    }
    static dir(msg: any): void {
        if(Debug.enabled) return;
        console.dir(msg);
    }
    static error(msg: any): void {
        if(Debug.enabled) return;
        console.log(msg);
        console.dir(msg);
    }
    static warn(msg: any): void {
        if(Debug.enabled) return;
        console.log(msg);
    }
}