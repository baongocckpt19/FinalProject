import { CanMatchFn, Router } from '@angular/router';
import { AccountRole } from '../model/account';
import { map, Observable, take } from 'rxjs';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export function roleGuard(roles: AccountRole[]): CanMatchFn {
  return (): Observable<boolean> => {
    const auth = inject(AuthService);
    const router = inject(Router);

    return auth.currentUser$.pipe(
      take(1),
      map(account => {
        if (account && roles.includes(account.roleName)) {
          return true;
        } 
        router.navigate(['/login']);
        return false;
      })
    );
  };
}
