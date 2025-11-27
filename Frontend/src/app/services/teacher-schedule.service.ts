// teacher-schedule.service.ts
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ScheduleItem {
    scheduleId: number;
    classId: number;
    date: string;       // yyyy-MM-dd
    className: string;
    classCode: string;
    startTime: string;  // HH:mm:ss
    endTime: string;    // HH:mm:ss
    room?: string;
    studentCount: number;
    isActive: boolean;
}

@Injectable({
    providedIn: 'root'
})
export class TeacherScheduleService {
    private apiUrl = 'http://localhost:8080/api/teacher/schedules';

    constructor(private http: HttpClient) { }

    getSchedulesByMonth(year: number, month: number): Observable<ScheduleItem[]> {
        const params = new HttpParams()
            .set('year', year.toString())
            .set('month', month.toString());

        return this.http.get<ScheduleItem[]>(this.apiUrl, { params });
    }
}
