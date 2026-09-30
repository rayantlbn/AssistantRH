import { api } from "./api";
import type { DashboardJobOffer } from "../types/dashboard";
import type { Page } from "../types/page";

export async function getDashboardJobOffers(): Promise<DashboardJobOffer[]> {
  const { data } = await api.get<Page<DashboardJobOffer>>("/dashboard/job-offers");
  return data.content;
}
