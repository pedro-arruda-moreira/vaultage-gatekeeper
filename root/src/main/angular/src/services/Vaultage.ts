import { Log } from "./Log";
import * as vt from "vaultage-client";
export class Vaultage {
    static async teste() {
        let v = await vt.login(document.location.href, 'teste', 'teste');
        alert(v.getAllEntries()[0].login);
    }
}