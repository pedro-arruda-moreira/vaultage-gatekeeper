import { ChangeDetectorRef } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Log } from '../services/Log';

export abstract class AbstractUpdatingComponent  {

    iframeWidth = 1;
    iframeHeight = 1;
  
    constructor(private _cdr : ChangeDetectorRef) {}

    
  refresh() {
    Log.debug('refreshing component ' + this);
    if(this._cdr) {
      this._cdr.detectChanges();
      Log.debug('refreshed component ' + this);
    } else {
      Log.debug("_cdr not present. ignoring...");
    }
  }

  getUrlParam(param: string, route: ActivatedRoute): string | null {
    let retVal: string | null = null;
    route.queryParamMap.forEach((prm) => {
      const value = prm.get(param);
      if (value != null) {
        retVal = value;
      }
    });
    return retVal;
  }
  }