import { CommonModule } from '@angular/common';
import { AfterViewInit, Component } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Component({
  selector: 'app-slideshow',
  imports: [CommonModule],
  templateUrl: './slideshow.component.html',
  styleUrls: ['./slideshow.component.scss']
})
export class SlideshowComponent implements AfterViewInit {
  currentSlide = 1;
  totalSlides = 4;

  ngAfterViewInit(): void {
    this.setupEvents();
    this.updateSlide(this.currentSlide);
  }

  updateSlide(slideNumber: number): void {
    for (let i = 1; i <= this.totalSlides; i++) {
      const slide = document.getElementById(`slide${i}`);
      const slideContent = slide?.querySelector('.slide-content') as HTMLElement;

      slide?.classList.remove('active');
      if (i < slideNumber) {
        slide?.classList.add('prev');
      } else {
        slide?.classList.remove('prev');
      }

      if (slideContent) {
        slideContent.style.animation = 'none';
      }
    }

    const currentSlideElement = document.getElementById(`slide${slideNumber}`);
    const currentContent = currentSlideElement?.querySelector('.slide-content') as HTMLElement;

    setTimeout(() => {
      currentSlideElement?.classList.add('active');
      if (currentContent) {
        currentContent.style.animation = 'slideEnter 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94)';
      }
    }, 100);

    const progressBar = document.getElementById('progressBar') as HTMLElement;
    if (progressBar) {
      progressBar.style.width = `${(slideNumber / this.totalSlides) * 100}%`;
    }

    document.querySelectorAll<HTMLElement>('.indicator').forEach((indicator, index) => {
      indicator.style.transform = 'scale(1)';
      if (index + 1 === slideNumber) {
        indicator.classList.add('active');
      } else {
        indicator.classList.remove('active');
      }
    });

    this.currentSlide = slideNumber;
  }

  setupEvents(): void {
    const prevBtn = document.getElementById('prevBtn');
    const nextBtn = document.getElementById('nextBtn');
    const indicators = document.querySelectorAll<HTMLElement>('.indicator');

    prevBtn?.addEventListener('click', () => {
      const newSlide = this.currentSlide > 1 ? this.currentSlide - 1 : this.totalSlides;
      this.updateSlide(newSlide);
    });

    nextBtn?.addEventListener('click', () => {
      const newSlide = this.currentSlide < this.totalSlides ? this.currentSlide + 1 : 1;
      this.updateSlide(newSlide);
    });

    indicators.forEach(indicator => {
      indicator.addEventListener('click', () => {
        const slideNumber = parseInt(indicator.getAttribute('data-slide') || '1', 10);
        this.updateSlide(slideNumber);
      });
    });
  }
}