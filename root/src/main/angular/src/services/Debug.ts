export class Debug {
    static enabled = false;

    static msg(msg: string) {
        if(!this.enabled) return;
        alert(msg);
    }
}