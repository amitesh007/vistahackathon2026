import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'app-success',
  standalone: false,
  templateUrl: './success.component.html',
  styleUrls: ['./success.component.scss']
})
export class SuccessComponent implements OnInit {
  message: string = 'Operation completed successfully!';
  returnUrl: string = '/dashboard';

  constructor(
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Get message and return URL from query params
    this.route.queryParams.subscribe(params => {
      if (params['message']) {
        this.message = params['message'];
      }
      if (params['returnUrl']) {
        this.returnUrl = params['returnUrl'];
      }
    });
  }

  onReturn(): void {
    this.router.navigate([this.returnUrl]);
  }
}
