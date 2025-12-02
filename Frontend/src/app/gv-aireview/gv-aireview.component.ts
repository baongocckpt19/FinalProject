import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';

interface StatCard {
  icon: string;
  colorClass: 'blue' | 'green' | 'yellow' | 'red';
  trendDirection: 'up' | 'down';
  trendValue: string;
  value: string;
  label: string;
}

interface GradeBar {
  range: string;
  category: string;
  categoryClass: string;
  count: number;
  percent: number;
  barWidth: number;
  danger?: boolean;
}

interface GradeComponent {
  name: string;
  weightLabel: string;
  desc: string;
  avg: number;
  trend: number;
  highlight?: boolean;
}

interface WeeklyTrend {
  weekLabel: string;
  percentage: number;
  change: number;
}

interface ClassificationItem {
  levelClass: 'excellent' | 'good' | 'average' | 'poor' | 'critical';
  icon: string;
  name: string;
  range: string;
  count: number;
  percent: string;
}

type RiskLevel = 'high' | 'medium';
type ValueLevel = 'danger' | 'warning';

interface RiskStudent {
  initials: string;
  name: string;
  studentId: string;
  attendancePercent: number;
  avgScore: number;
  attendanceLevel: ValueLevel;
  avgLevel: ValueLevel;
  riskLevel: RiskLevel;
}

@Component({
  selector: 'app-gv-aireview',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './gv-aireview.component.html',
  styleUrl: './gv-aireview.component.scss'
})
export class GvAireviewComponent {
  mainTitle = 'Dashboard Phân tích Điểm danh';
  mainSubtitle = 'Tổng quan tình hình học tập và chuyên cần lớp học';

  // ----- Tổng quan 4 card trên cùng -----
  statsOverview: StatCard[] = [
    {
      icon: '📚',
      colorClass: 'blue',
      trendDirection: 'up',
      trendValue: '+5%',
      value: '92.5%',
      label: 'Tỷ lệ điểm danh TB'
    },
    {
      icon: '📊',
      colorClass: 'green',
      trendDirection: 'up',
      trendValue: '+0.3',
      value: '7.85',
      label: 'Điểm TB lớp'
    },
    {
      icon: '👥',
      colorClass: 'green',
      trendDirection: 'up',
      trendValue: '+12',
      value: '156',
      label: 'Tổng sinh viên'
    },
    {
      icon: '⚠️',
      colorClass: 'yellow',
      trendDirection: 'down',
      trendValue: '-3',
      value: '12',
      label: 'SV nguy cơ cao'
    }
  ];

  // ----- Phân bố điểm -----
  gradeBars: GradeBar[] = [
    {
      range: '9.0 - 10',
      category: 'Xuất sắc',
      categoryClass: 'excellent-grade',
      count: 44,
      percent: 28,
      barWidth: 28
    },
    {
      range: '8.0 - 8.9',
      category: 'Giỏi',
      categoryClass: 'good-grade',
      count: 55,
      percent: 35,
      barWidth: 35
    },
    {
      range: '7.0 - 7.9',
      category: 'Khá',
      categoryClass: 'average-grade',
      count: 34,
      percent: 22,
      barWidth: 22
    },
    {
      range: '5.5 - 6.9',
      category: 'TB Khá',
      categoryClass: 'pass-grade',
      count: 16,
      percent: 10,
      barWidth: 10
    },
    {
      range: '4.0 - 5.4',
      category: 'Trung bình',
      categoryClass: 'warning-grade',
      count: 5,
      percent: 3,
      barWidth: 4,
      danger: true
    },
    {
      range: '< 4.0',
      category: 'Yếu/Kém',
      categoryClass: 'fail-grade',
      count: 2,
      percent: 1.3,
      barWidth: 1.3,
      danger: true
    }
  ];

  // ----- Điểm thành phần -----
  gradeComponents: GradeComponent[] = [
    {
      name: 'Chuyên cần',
      weightLabel: '25%',
      desc: 'Điểm danh + tham gia',
      avg: 8.9,
      trend: +0.2
    },
    {
      name: 'Giữa kỳ',
      weightLabel: '25%',
      desc: '1 bài kiểm tra',
      avg: 7.8,
      trend: -0.1
    },
    {
      name: 'Cuối kỳ',
      weightLabel: '50%',
      desc: 'Thi viết',
      avg: 7.4,
      trend: +0.3
    },
    {
      name: 'Trung bình',
      weightLabel: '',
      desc: 'Điểm tổng kết',
      avg: 7.85,
      trend: +0.3,
      highlight: true
    }
  ];

  // ----- AI insights cho điểm -----
  gradeInsights = [
    {
      type: 'success',
      icon: '🎉',
      text: '63% lớp đạt từ 8.0 trở lên. Điểm đồ án cao nhất (8.2) cho thấy SV có khả năng thực hành tốt'
    },
    {
      type: 'warning',
      icon: '📉',
      text: 'Điểm giữa kỳ thấp hơn mong đợi (-0.1). Nên tăng cường ôn tập lý thuyết cho kỳ thi cuối'
    },
    {
      type: 'info',
      icon: '🎯',
      text: '7 SV cần hỗ trợ để đạt điểm qua môn. Tập trung vào bài tập thực hành và giữa kỳ'
    }
  ];

  // ----- Điểm danh theo tuần -----
  weeklyTrends: WeeklyTrend[] = [
    { weekLabel: 'Tuần 1', percentage: 87.5, change: -2.5 },
    { weekLabel: 'Tuần 2', percentage: 89.2, change: +1.7 },
    { weekLabel: 'Tuần 3', percentage: 91.8, change: +2.6 },
    { weekLabel: 'Tuần 4', percentage: 92.5, change: +0.7 }
  ];

  weeklyInsights = [
    {
      type: 'success',
      icon: '✅',
      text: 'Tỷ lệ điểm danh tăng đều từ 87.5% lên 92.5% (+5% tổng thể)'
    },
    {
      type: 'info',
      icon: '💡',
      text: 'Tuần 3 có mức tăng +2.6%, các biện pháp động viên đang phát huy tác dụng'
    },
    {
      type: 'warning',
      icon: '⚠️',
      text: 'Tuần 4 tăng chậm (+0.7%), cần tiếp tục động viên để đạt 95%'
    }
  ];

  // ----- Phân loại chuyên cần -----
  classifications: ClassificationItem[] = [
    {
      levelClass: 'excellent',
      icon: '🌟',
      name: 'Xuất sắc',
      range: '≥ 95%',
      count: 78,
      percent: '50.0%'
    },
    {
      levelClass: 'good',
      icon: '👍',
      name: 'Tốt',
      range: '85-94%',
      count: 45,
      percent: '28.8%'
    },
    {
      levelClass: 'average',
      icon: '📝',
      name: 'Trung bình',
      range: '75-84%',
      count: 21,
      percent: '13.5%'
    },
    {
      levelClass: 'poor',
      icon: '⚠️',
      name: 'Yếu',
      range: '60-74%',
      count: 8,
      percent: '5.1%'
    },
    {
      levelClass: 'critical',
      icon: '🚨',
      name: 'Kém',
      range: '< 60%',
      count: 4,
      percent: '2.6%'
    }
  ];

  // ----- Sinh viên nguy cơ -----
  riskAlertTitle = 'Cảnh báo can thiệp ngay';
  riskAlertDesc = 'Sinh viên vắng ≥ 20% hoặc điểm TB < 5.0';
  riskAlertCount = 12;

  studentsAtRisk: RiskStudent[] = [
    {
      initials: 'NM',
      name: 'Nguyễn Văn Minh',
      studentId: 'SV2024082',
      attendancePercent: 58,
      avgScore: 4.2,
      attendanceLevel: 'danger',
      avgLevel: 'danger',
      riskLevel: 'high'
    },
    {
      initials: 'LH',
      name: 'Lê Thị Hương',
      studentId: 'SV2024105',
      attendancePercent: 62,
      avgScore: 4.5,
      attendanceLevel: 'danger',
      avgLevel: 'danger',
      riskLevel: 'high'
    },
    {
      initials: 'PT',
      name: 'Phạm Văn Tài',
      studentId: 'SV2024127',
      attendancePercent: 68,
      avgScore: 6.1,
      attendanceLevel: 'warning',
      avgLevel: 'warning',
      riskLevel: 'medium'
    },
    {
      initials: 'HN',
      name: 'Hoàng Thị Ngọc',
      studentId: 'SV2024143',
      attendancePercent: 78,
      avgScore: 4.8,
      attendanceLevel: 'warning',
      avgLevel: 'danger',
      riskLevel: 'medium'
    },
    {
      initials: 'VD',
      name: 'Vũ Quang Dũng',
      studentId: 'SV2024158',
      attendancePercent: 55,
      avgScore: 3.9,
      attendanceLevel: 'danger',
      avgLevel: 'danger',
      riskLevel: 'high'
    }
  ];

  // helper cho template
  getTrendClass(change: number): 'positive' | 'negative' {
    return change >= 0 ? 'positive' : 'negative';
  }

  formatChange(change: number): string {
    const sign = change > 0 ? '+' : '';
    return `${sign}${change}%`;
  }

  formatNumber(num: number): string {
    return num.toString().replace('.', ',');
  }
}
