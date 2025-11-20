// src/app/components/toast/toast.component.ts
import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { NotificationService, Toast } from '../services/notification.service';


@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './toast.component.html',
  styleUrls: ['./toast.component.scss']
})
export class ToastComponent implements OnInit, OnDestroy {
  toasts: Toast[] = [];
  private sub?: Subscription;

  constructor(private notify: NotificationService) {}

  ngOnInit(): void {
    this.sub = this.notify.toasts$.subscribe(list => {
      this.toasts = list;
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
  }

  close(id: number) {
    this.notify.remove(id);
  }
}
