// src/app/services/notification.service.ts
import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export type ToastType = 'success' | 'error';

export interface Toast {
  id: number;
  type: ToastType;
  message?: string;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private _toasts$ = new BehaviorSubject<Toast[]>([]);
  toasts$ = this._toasts$.asObservable();

  private counter = 0;

  show(type: ToastType, message?: string, duration = 2500) {
    const toast: Toast = {
      id: ++this.counter,
      type,
      message
    };

    const current = this._toasts$.value;
    this._toasts$.next([...current, toast]);

    setTimeout(() => {
      this.remove(toast.id);
    }, duration);
  }

  success(message?: string, duration = 2500) {
    this.show('success', message, duration);
  }

  error(message?: string, duration = 2500) {
    this.show('error', message, duration);
  }

  remove(id: number) {
    this._toasts$.next(this._toasts$.value.filter(t => t.id !== id));
  }
}
