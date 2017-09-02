import { Injectable } from '@angular/core'

@Injectable()
export class LoggingService {

    loggLevel: number = INFO;

    trace(message: string, arg: any = null) {
        if (this.loggLevel === 0) {
            if (arg) {
                console.log("[TRACE]: " + message, arg);
            } else {
                console.log("[TRACE]: " + message);
            }
        }
    }

    debug(message: string, arg: any = null) {
        if (this.loggLevel <= 1) {
            if (arg) {
                console.log("[DEBUG]: " + message, arg);
            } else {
                console.log("[DEBUG]: " + message);
            }
        }
    }

    info(message: string, arg: any = null) {
        if (this.loggLevel <= 2) {
            if (arg) {
                console.log("[INFO]: " + message, arg);
            } else {
                console.log("[INFO]: " + message);
            }
        }
    }

    warn(message: string, arg: any = null) {
        if (this.loggLevel <= 3) {
            if (arg) {
                console.warn("[WARN]: " + message, arg);
            } else {
                console.warn("[WARN]: " + message);
            }
        }
    }

    error(message: string, arg: any = null) {
        if (this.loggLevel <= 4) {
            if (arg) {
                console.error("[ERROR]: " + message, arg);
            } else {
                console.error("[ERROR]: " + message);
            }
        }
    }
}

const TRACE: number = 0;
const DEBUG: number = 1;
const INFO: number = 2
const WARN: number = 3;
const ERROR: number = 4;
const OFF: number = 99;

