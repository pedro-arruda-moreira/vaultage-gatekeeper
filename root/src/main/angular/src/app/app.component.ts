import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { AbstractUpdatingComponent } from '../common/AbstractUpdatingComponent';
import { Debug } from '../services/Debug';
import { Vaultage } from '../services/Vaultage';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent extends AbstractUpdatingComponent implements OnInit {

  constructor(private cdr: ChangeDetectorRef) {
    super(cdr);
  }

  ngOnInit(): void {
    if (document.location.href.indexOf('?debug=') > -1) {
      Debug.enabled = true;
    }
    Vaultage.teste();
  }
}
