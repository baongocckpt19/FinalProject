// src/app/services/device.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Device {
  deviceId: number;
  deviceCode: string;
  deviceName: string | null;
  room: string | null;
  isActive: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class DeviceService {
  private apiUrl = 'http://localhost:8080/api/devices';

  constructor(private http: HttpClient) { }

  /** Lấy tất cả thiết bị */
  getAllDevices(): Observable<Device[]> {
    return this.http.get<Device[]>(this.apiUrl);
  }

  /** Tạo mới thiết bị */
  createDevice(payload: {
    deviceCode: string;
    deviceName?: string | null;
    room?: string | null;
    isActive: boolean;
  }) {
    return this.http.post(this.apiUrl, payload, {
      responseType: 'text'
    });
  }

  /** Cập nhật thiết bị */
  updateDevice(id: number, payload: {
    deviceCode: string;
    deviceName?: string | null;
    room?: string | null;
    isActive: boolean;
  }) {
    return this.http.put(`${this.apiUrl}/${id}`, payload, {
      responseType: 'text'
    });
  }

  /** Xóa thiết bị */
  deleteDevice(id: number) {
    return this.http.delete(`${this.apiUrl}/${id}`, {
      responseType: 'text'
    });
  }

  /** Bật / tắt hoạt động thiết bị */
  updateActive(id: number, isActive: boolean) {
    // pattern giống class-schedule: /{id}/active?isActive=true/false
    return this.http.put(
      `${this.apiUrl}/${id}/active?isActive=${isActive}`,
      {},
      { responseType: 'text' }
    );
  }
}
