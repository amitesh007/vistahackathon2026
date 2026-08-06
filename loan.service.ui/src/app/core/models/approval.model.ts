export interface PendingApproval {
  id: string;
  customer: {
    name: string;
    avatar?: string;
    initials: string;
  };
  products: string[];
  task: 'Creation' | 'Amendment';
  createdDate: Date;
  status: 'pending' | 'approved' | 'rejected';
}

export interface ApprovalListResponse {
  approvals: PendingApproval[];
  total: number;
  page: number;
  pageSize: number;
}
