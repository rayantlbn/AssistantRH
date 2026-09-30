/** Une ligne de GET /api/dashboard/job-offers (contrat figé par DashboardControllerTest côté backend). */
export interface DashboardJobOffer {
  jobOfferId: number;
  title: string;
  newCount: number;
  shortlistedCount: number;
  interviewCount: number;
  offerCount: number;
  hiredCount: number;
  rejectedCount: number;
}
